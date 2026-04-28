# Yokara Automation — Priority Workflow (P0 / P1 / P2)

> Quy trình tiếp nhận, phân loại và xử lý task automation cho Yokara mobile.
> Áp dụng cho cả developer + Claude Code khi nhận yêu cầu.

---

## 1. Ba mức ưu tiên

| Mức | Định nghĩa | Ví dụ | SLA mục tiêu |
|---|---|---|---|
| **P0** | Chặn CI / chặn release / repo bị vỡ | Pipeline đỏ, profile `hc-box` crash, suite không compile, secret leak, file build bị commit gây bloat | Trong ngày |
| **P1** | Ảnh hưởng coverage hoặc độ ổn định, không chặn | Thiếu test cho feature đang release, locator vỡ do app cập nhật, flaky test trên CI, thay `Thread.sleep` bằng explicit wait | 3–5 ngày |
| **P2** | Cải thiện chất lượng / DX, không gấp | Refactor Jenkinsfile, gộp script `dump_*.py`, viết doc, tách shared library | Theo capacity |

**Quy tắc**: khi còn P0 → không bắt đầu P2. P1 và P2 có thể chạy song song nếu không xung đột file.

---

## 2. Vòng đời 1 task

```
[Intake] → [Triage] → [Plan] → [Implement] → [Verify] → [PR] → [CI green] → [Merge]
```

### 2.1. Intake — nhận yêu cầu
- Yêu cầu phải trả lời được 3 câu:
  1. **Phạm vi**: Android / iOS / cả hai? Feature nào (Trực Tuyến, Hát, Tin Nhắn, Tôi, Trang Chủ)?
  2. **Tiêu chí xong**: test pass cục bộ? Pass trên Jenkins `192.168.10.130:8080`? Cần Allure report?
  3. **Bằng chứng UI**: đã có dump XML / screenshot / Inspector session chưa? Nếu chưa → request user dump trước (xem `scripts/dump_*.py`).

> Nếu không trả lời được 3 câu → **dừng**, hỏi lại user. Không tự suy diễn.

### 2.2. Triage — gán mức P
- Áp bảng mục 1.
- Nếu lưỡng lự giữa P0/P1: hỏi user. Nếu giữa P1/P2: chọn P1 (an toàn hơn).
- Ghi mức P vào title PR / commit message: `[P1] add navigation smoke test`.

### 2.3. Plan — chỉ với task ≥ P1 hoặc đụng > 3 file
- Liệt kê file sẽ sửa, file sẽ tạo, page object sẽ thêm.
- Liệt kê locator dự kiến (theo thứ tự `accessibility id → id → xpath`).
- Nêu rõ rủi ro: phụ thuộc thứ tự test? Cần state đăng nhập trước? Phụ thuộc data device?
- Plan ngắn gọn — không thành document riêng trừ khi user yêu cầu.

### 2.4. Implement — quy tắc bắt buộc

**Locator** (xem [.cursor/skills/appium-locator-priority/SKILL.md](../.cursor/skills/appium-locator-priority/SKILL.md))
- `accessibility id` → `id` → `xpath` (cuối cùng).
- Không đoán; phải có dump XML hoặc xác nhận từ user.
- Tách `@AndroidFindBy` / `@iOSXCUITFindBy` cho mỗi platform.

**Wait**
- Dùng `WaitUtils` / `WebDriverWait`. Không `Thread.sleep` mới.
- Timeout mặc định ở `BaseScr` là 20s — chỉ override khi có lý do.

**Page Object**
- Một màn hình = một class trong `pages/<feature>/`.
- Không assert trong PO. PO chỉ trả về trạng thái / dữ liệu.
- Chỉ mở public các method test cần — phần còn lại để `private`.

**Flow**
- Logic đa bước (login, vào phòng, …) đặt ở `flows/`. Test chỉ orchestrate.
- Mỗi flow tự quyết định khi nào cần re-login, dismiss popup, retry.

**Test**
- Class trong `tests/`, một feature = một class.
- Mọi action bọc `StepUtils.step(...)` để Allure đẹp.
- Tên unique-by-time cho data tạo mới (xem `RoomTest.testCreatePrivateRoom` — dùng timestamp 36-base).

### 2.5. Verify — trước khi mở PR
Checklist tối thiểu:
- [ ] `mvn clean test` chạy ít nhất một lần trên 1 device thật (Android **hoặc** iOS).
- [ ] Test mới chạy được khi cắm cả 2 platform cùng lúc (xem `testng-multidevice.xml`).
- [ ] Không thêm dependency/plugin mới mà chưa thống nhất.
- [ ] Không commit `target/`, `allure-results/`, `.idea/`, `.venv/`, `.DS_Store`.
- [ ] Locator mới đã có dump XML kèm theo (đặt vào `scripts/xml_dumps/` nếu hữu ích lâu dài).
- [ ] Update [docs/README-technical.md](README-technical.md) nếu thay đổi cách chạy.

### 2.6. Khi bị chặn (Blocked Protocol)
Nếu trong lúc implement gặp 1 trong các tình huống sau → **dừng và báo cáo**, không tự sáng tạo:
- Không tìm thấy element sau khi đã thử accessibility id + id.
- App điều hướng khác mô tả testcase (ví dụ pop-up lạ, A/B variant chưa biết).
- Build app khác version / khác môi trường staging.
- Device disconnect / Appium server lỗi không phục hồi sau 1 retry.

**Format báo cáo:**
```
[BLOCKED]
Màn hình:        <feature>/<screen>
Bước:            <bước cụ thể đang thực hiện>
Lỗi quan sát:    <log / exception 1-2 dòng>
Đã thử:          <locator nào, wait bao nhiêu, retry mấy lần>
Cần hỗ trợ:      <dump lại XML? cấp build? xác nhận thiết kế?>
```

### 2.7. PR & commit
- Branch: `feat/<short>`, `fix/<short>`, `chore/<short>`.
- Commit message: `<type>(<scope>): <imperative>` — ví dụ `feat(tests): add navigation smoke for bottom tabs`.
- Tham chiếu mức P trong title PR: `[P1] feat(tests): …`.
- PR description nêu: phạm vi platform, device đã chạy thử, link Allure (nếu có).

### 2.8. CI gate
- Pipeline Jenkins (`192.168.10.130:8080`) phải xanh ở **ít nhất 1 device Android + 1 device iOS** đang cắm trên agent.
- Nếu CI đỏ vì lỗi flaky / device bận: rerun **tối đa 2 lần**. Đỏ lần 3 → coi là regression, không merge.

---

## 3. Pattern theo loại task

### 3.1. Thêm test case mới
1. Confirm spec + dump XML từ user.
2. Thêm/cập nhật Page Object trong `pages/<feature>/`.
3. Nếu cần orchestration đa màn hình → thêm Flow trong `flows/`.
4. Viết test class trong `tests/` (hoặc method mới trong class hiện có nếu cùng feature).
5. Đăng ký test class trong `testng*.xml` phù hợp + cập nhật [tools/GenerateHcBoxSuite.java](../src/test/java/tools/GenerateHcBoxSuite.java) nếu cần chạy ở `hc-box`.
6. Verify cục bộ → PR.

### 3.2. Sửa locator vỡ
1. Yêu cầu user dump lại XML hiện tại của màn hình.
2. So sánh với locator cũ — xác định attribute đã thay đổi.
3. Áp dụng thứ tự ưu tiên (accessibility id → id → xpath).
4. Nếu locator vỡ là xpath dài → cơ hội refactor sang accessibility id (nếu dev đã gắn).
5. Chạy lại đúng test bị fail trước khi PR.

### 3.3. Thêm thiết bị / platform mới vào pool
1. Cập nhật [config/config.properties](../config/config.properties) (port base, app id, signing).
2. Verify [core/DeviceManager.java](../src/test/java/core/DeviceManager.java) detect được (chạy `dump_current.py` hoặc test riêng).
3. Verify Jenkins agent có sẵn `adb` / `idevice_id` / Xcode đúng version.
4. Chạy thử profile `hc-box` để confirm sinh suite đúng.

### 3.4. Hotfix P0 trên CI
1. Reproduce cục bộ trước (nếu được).
2. Fix tối thiểu, **không** kèm refactor.
3. PR riêng, title `[P0] fix(ci): …`.
4. Merge ngay khi 1 device pass — backfill coverage ở PR P1 sau.

---

## 4. Backlog hiện tại (tham chiếu)

Bám theo phần "Improvement Plan" gần nhất, tóm tắt:

**P0**
- Sửa [tools/GenerateHcBoxSuite.java:54-55](../src/test/java/tools/GenerateHcBoxSuite.java#L54-L55) đang reference class không tồn tại.
- Dọn `.idea/`, `.venv/`, `.DS_Store`, `allure-results/` đã bị commit.
- Bỏ hardcode `/Users/quhuy/...` ở [Jenkinsfile:17-33](../Jenkinsfile#L17-L33).

**P1**
- Mở rộng test: navigation smoke (5 tab), login matrix, Hát + Tin Nhắn smoke.
- Thay `Thread.sleep` ở `BottomNav.java`, `CuaToiScr.java` bằng explicit wait.
- Hardening locator: PhongScr, LoginPhoneScr, AccountScr, BottomNav (xpath text/sibling-index).

**P2**
- Trích Jenkinsfile thành shared library.
- Gộp 8 script `dump_*.py` thành `dump_screen.py --screen=<name>`.
- Quyết định số phận `scripts/generate_pom.py`.
- Chuẩn hoá Page Factory annotation (`@AndroidFindBy` / `@iOSXCUITFindBy`) cho toàn bộ PO.
- Viết `.env.example` + doc env var `YOKARA_*`.

---

## 5. Tài liệu liên quan

- [CLAUDE.md](../CLAUDE.md) — rule cho Claude Code khi sinh code.
- [.cursor/skills/appium-locator-priority/SKILL.md](../.cursor/skills/appium-locator-priority/SKILL.md) — chuẩn locator chi tiết.
- [docs/README-technical.md](README-technical.md) — setup kỹ thuật.
- [docs/README-non-technical.md](README-non-technical.md) — hướng dẫn cho QA không tech.
