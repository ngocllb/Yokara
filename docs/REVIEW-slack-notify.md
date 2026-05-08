# Slack notify — Allure failed tests

> Trạng thái: **APPLIED**. Script đã commit và Jenkinsfile đã patch.
> Đã smoke-test với build #126 thật → output sạch, fail step + 3 attachment + device info đầy đủ.

## 1. Cách chạy

Tự động: cuối mỗi build (post.always) → Jenkins gửi tóm tắt vào Slack channel `C0B2EMXH70E` qua proxy `https://timetracking.ikara.co/slack/send`.

Manual / dry-run (đứng ở repo root):
```bash
JENKINS_USER=admin JENKINS_PASS=admin python3 scripts/notify_slack_failures.py \
    --build-url "http://localhost:8080/job/YOKARA%20Appium/126/" \
    --build-number 126 \
    --channel TEST \
    --dry-run
```

## 2. Yêu cầu **một lần** trên Jenkins

Tạo Jenkins credential cho script đọc Allure plugin:

- Manage Jenkins → Credentials → System → Global → **Add Credentials**
- Kind: **Username with password**
- ID: `jenkins-allure-readonly` (đúng chính xác — đã hard-code trong [Jenkinsfile:815](../Jenkinsfile#L815))
- Username / Password: tài khoản **read-only** đủ quyền xem `/allure/data/...` (admin/admin cũng được nếu Jenkins local)

Nếu chưa tạo credential này → stage Slack notify sẽ skip (nhờ `catchError` ở [Jenkinsfile:813](../Jenkinsfile#L813)), build vẫn xanh.

## 3. Mẫu message thật (từ build #126)

```
🚨 *Build #126* — 1 test fail/broken
🔗 Allure: http://localhost:8080/job/YOKARA%20Appium/126/allure/#suites

1. 💥 *testCreatePrivateRoom*  —  📱 ios-0AE0401E  (udid 00008110-00165D890AE0401E, port 4710)
   ↳ Step fail: Tìm và vào phòng vừa tạo từ Search
      reason: Expected condition failed: waiting for element to be clickable: By.xpath: //XCUIElementTypeStaticText[@name='Khám phá']/...
   📎 Screenshot: http://localhost:8080/job/YOKARA%20Appium/126/allure/data/attachments/c334de6931817aec.png
   📎 Page source: http://localhost:8080/job/YOKARA%20Appium/126/allure/data/attachments/4f8c786e1c524bb6.txt
   📎 Log: http://localhost:8080/job/YOKARA%20Appium/126/allure/data/attachments/effbecc4be264f92.txt
```

Quy ước icon:
- `❌` — test status `failed` (assertion fail)
- `💥` — test status `broken` (Appium / WebDriver lỗi runtime)

## 4. Logic script

[scripts/notify_slack_failures.py](../scripts/notify_slack_failures.py):

1. Đọc `${BUILD_URL}allure/data/suites.json` → walk cây → leaf nào có `status ∈ {failed, broken}` thì giữ.
2. Filter loại bỏ leaf `Device Summary - *` (synthetic do Jenkinsfile inject ở [Jenkinsfile:487-563](../Jenkinsfile#L487-L563)).
3. Với mỗi leaf còn lại, fetch `${BUILD_URL}allure/data/test-cases/<uid>.json` rút:
   - **Tên test** — đã trim suffix `[ios-... · A:... W:... M:...]`.
   - **Device info** từ labels: `device`, `device.udid`, `device.appiumPort`.
   - **Failed steps** — đệ quy `testStage.steps[]`, lấy step status fail/broken (cả nested).
   - **Attachments** — của step fail/broken + top-level testStage attachments. Auto-label theo MIME type:
     - `image/*` → "Screenshot"
     - `name` chứa "page source" → "Page source"
     - `text/*` → "Log"
     - còn lại → "File"
4. Sort theo `device.branch` → `name` cho ổn định.
5. Build message: tối đa 8 test / msg, 3 step / test, 3 attachment / test, reason 220 ký tự (giữ URL ≤ 7000 char).
6. URL-encode + GET đến proxy Slack.
7. **Mọi exception đều nuốt** — script luôn return 0 để không vỡ build.

## 5. Behavior matrix

| Tình huống | Hành vi |
|---|---|
| Build PASS hết | Im lặng (set `SLACK_NOTIFY_ON_PASS=true` để vẫn gửi msg "✅ 0 fail") |
| 1 test broken/failed | Gửi block đầy đủ (tên + step + attachments + device) |
| 9+ test fail | Gửi 8 đầu + dòng "… và N test fail khác — xem Allure" |
| Allure plugin chưa kịp render `/allure/data/...` | Retry 5 lần × 3s, sau đó bỏ qua |
| Credential `jenkins-allure-readonly` chưa tạo | `catchError` skip stage, build vẫn xanh |
| Slack proxy lỗi / timeout | Log + bỏ qua, build vẫn xanh |
| `BUILD_URL` rỗng (chạy ngoài Jenkins) | Bỏ qua sớm |

## 6. File thay đổi

| File | Thay đổi |
|---|---|
| [scripts/notify_slack_failures.py](../scripts/notify_slack_failures.py) | **MỚI** — 230 dòng, chỉ dùng Python stdlib (`urllib`, `base64`, `json`). |
| [Jenkinsfile:813-827](../Jenkinsfile#L813-L827) | Thêm block `catchError` + `withCredentials` + `sh` sau `allure([...])` trong `post.always`. |

## 7. Cách verify trước khi merge

1. **Local dry-run** với build có sẵn:
   ```bash
   JENKINS_USER=admin JENKINS_PASS=admin python3 scripts/notify_slack_failures.py \
       --build-url "http://localhost:8080/job/YOKARA%20Appium/126/" \
       --build-number 126 --channel TEST --dry-run
   ```
   Đã verified ✅ — 1 test broken, 3 attachment links đúng định dạng.

2. **Tạo credential `jenkins-allure-readonly`** trên Jenkins (mục 2).

3. **Trigger build mới** → kiểm tra:
   - Stage `Declarative: Post Actions` không fail.
   - Log thấy `[slack] message preview` rồi `[slack] HTTP 200`.
   - Slack channel `C0B2EMXH70E` nhận được tin nhắn.

4. (Optional) Test fallback: tạm rename credential ID để ép thiếu → build vẫn xanh, log `Slack notify failed` ở stage post.

## 8. Tunable (env var)

Có thể chỉnh hành vi mà không sửa code:

| Env var | Default | Công dụng |
|---|---|---|
| `SLACK_CHANNEL` | `C0B2EMXH70E` | Đổi channel đích |
| `SLACK_NOTIFY_ON_PASS` | (off) | Set `true` để gửi msg cả khi build all-pass |
| `JENKINS_USER` / `JENKINS_PASS` | từ credential | Override basic auth |

Nếu cần đổi channel cố định cho cả pipeline, thêm `SLACK_CHANNEL = '...'` vào block `environment {}` đầu Jenkinsfile và pass `--channel "$SLACK_CHANNEL"` vào script.
