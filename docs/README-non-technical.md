# Yokara — Giải thích cho người chưa chuyên sâu kỹ thuật

Dành cho PM, BA, vận hành, hoặc thành viên mới chưa quen automation.

## Dự án này làm gì?

Đây là **bộ kiểm thử tự động trên điện thoại** cho app Yokara: mô phỏng thao tác người dùng (bấm, nhập, chuyển màn) và kiểm tra kết quả. Chạy được trên **Android** và **iPhone** khi máy tính nhận điện thoại qua cáp USB.

## Ba phần dễ hình dung

1. **Máy tính** cài công cụ — đóng vai “bộ não điều khiển”.
2. **Điện thoại** cắm USB — là “màn hình thật” để kiểm tra.
3. **Kịch bản test** — là “kịch bản” mô tả từng bước (team automation đã viết trong dự án).

## Hai cách chạy phổ biến

| Cách | Ai thường dùng | Ý nghĩa |
|------|----------------|--------|
| **Trên máy nhân viên** | Dev / QA gần thiết bị | Cắm điện thoại, chạy trên máy mình để kiểm tra nhanh. |
| **Trên Jenkins (máy chủ)** | Team / CI | Máy chủ tự chạy theo lịch hoặc sau khi có bản build mới, và xuất báo cáo. |

**Lưu ý:** Nếu **cùng một máy** vừa là máy chủ Jenkins vừa là máy bạn làm việc, không nên vừa chạy test tay vừa để Jenkins chạy **cùng lúc trên cùng một điện thoại** — dễ lỗi vì một lúc chỉ nên một luồng điều khiển.

## Báo cáo (Allure) là gì?

Sau khi chạy, hệ thống có thể tạo **báo cáo trực quan**: test nào đạt, test nào không, kèm ảnh màn hình hoặc log. Giúp nhìn nhanh **chỗ nào lỗi** mà không cần đọc log kỹ thuật chi tiết.

## Bạn cần biết gì nếu không chỉnh code?

- **Cấu hình chung** thường nằm trong thư mục `config/` — thường do người phụ trách môi trường chỉnh.
- **Jenkins** là nơi bấm chạy tự động; kết quả và báo cáo nằm trong job tương ứng.
- Khi báo lỗi / hỗ trợ, nên cung cấp: **loại máy, phiên bản app, thời điểm chạy, ảnh màn hình lỗi** — team kỹ thuật đối chiếu với báo cáo và log.

## Một vài từ khóa (để nói chuyện với team kỹ thuật)

- **Appium:** phần mềm giúp máy tính điều khiển điện thoại.
- **TestNG:** khung tổ chức các “bài kiểm tra” tự động.
- **Jenkins:** máy chủ để chạy kiểm thử theo lịch hoặc sau khi có thay đổi.

## Tài liệu chi tiết hơn

Xem [README-technical.md](./README-technical.md) nếu bạn cần hướng dẫn cài đặt và chạy đầy đủ cho người có nền kỹ thuật.
