# Kết quả kiểm tra frontend

## Build và kiểm thử tự động

- TypeScript strict và Vite production build thành công ở từng phase.
- 15 kiểm thử Vitest: tổng tiền/số lượng, ngưỡng phí vận chuyển, tìm kiếm không dấu, kết hợp bộ lọc, phân trang, empty result, validation địa chỉ/email, API không tự fallback sang demo, payload giỏ hàng, idempotency đơn hàng và session HTTP 401.
- REST service tests dùng mock Axios. Chưa kiểm thử với REST server thật vì project Spring Boot chưa có controller.

## Kiểm tra trình duyệt

- Home, catalog, chi tiết và tài khoản: đo ở 320, 768, 1024, 1440px. Không tràn ngang; không có computed box-shadow, text-shadow hoặc gradient trên các trang được đo.
- Cart và checkout: kiểm tra thêm ở 320px, không tràn ngang.
- Ảnh hero, danh mục và sản phẩm nổi bật tải thành công.
- Tìm kiếm `nhan` trả về 3 sản phẩm nhẫn. Bộ lọc mobile mở trong drawer, Escape đóng và trả focus về nút Bộ lọc.
- Thumbnail đổi trạng thái và ảnh đang xem. Thêm giỏ có thông báo; tăng nhẫn từ 1 lên 2 cập nhật tổng từ 3.100.000đ lên 4.350.000đ, giảm trả về đúng tổng.
- Checkout trống hiển thị 6 lỗi trường bắt buộc; đơn demo hợp lệ tạo thành công, giỏ được xóa và đơn xuất hiện trong lịch sử tài khoản demo.
- Đăng nhập demo, lưu địa chỉ và điền lại địa chỉ vào checkout hoạt động. Đã kiểm tra trực quan số điện thoại và email; trường nhập vẫn giữ giá trị người dùng khi phiên đăng nhập khôi phục bất đồng bộ.
- WebMCP `navigate_product_search`: input hợp lệ điều hướng đúng; input sai kiểu bị từ chối. Công cụ không tạo đơn hoặc thanh toán.
- Animation CSS nằm trong 180–300ms (loading dùng chu kỳ riêng); có media query `prefers-reduced-motion: reduce` tắt transition/animation và hover transform. Chưa thay đổi cài đặt motion của hệ điều hành để kiểm thử thực tế.
- Native dialog, label/alt, focus-visible, skip link, live region và validation đã được kiểm tra qua UI/DOM. Chưa có audit WCAG toàn diện hoặc kiểm thử screen reader chuyên dụng.

## Giới hạn trước vận hành thật

- Backend chưa có endpoint: auth, cart, order và địa chỉ hiện chạy demo theo biến môi trường. Không thu tiền/giao hàng thật.
- Tài khoản đăng ký demo chỉ tồn tại trong phiên; mật khẩu không được lưu. Wishlist lưu trên thiết bị.
- Thay ảnh minh họa bằng ảnh đúng SKU, xác nhận giá, chính sách, tồn kho, email liên hệ và tích hợp thanh toán thực tế nếu cần.
- Backend phải tính lại giá/tồn kho/phí, phân quyền đơn hàng, quản lý cookie/CSRF/CORS và idempotency theo contract trong README.
