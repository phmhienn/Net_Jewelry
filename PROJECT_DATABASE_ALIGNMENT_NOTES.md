# Ghi chú chỉnh sửa theo database.sql

## Phạm vi

Các chỉnh sửa trong lần này bám theo `backend/database.sql` hiện tại, không đổi cấu trúc package/backend/frontend và không tạo lại module đang có.

## Thay đổi đã thực hiện

1. Địa chỉ giao hàng
   - `database.sql` khai báo `dia_chi.phuong_xa` là `NOT NULL`.
   - Frontend đã đổi `Phường / Xã` thành trường bắt buộc ở checkout và trang tài khoản.
   - Validation địa chỉ hiện báo lỗi riêng cho `Quận / Huyện` và `Phường / Xã`.

2. Danh mục và thương hiệu trên storefront
   - `danh_muc.trang_thai` và `thuong_hieu.trang_thai` dùng `HOAT_DONG` / `NGUNG_HOAT_DONG`.
   - Frontend chỉ hiển thị danh mục/thương hiệu đang hoạt động cho menu, bộ lọc và form sản phẩm.

3. Footer và cấu hình cửa hàng
   - Backend hiện chỉ trả phí vận chuyển và ngưỡng miễn phí vận chuyển, chưa có bảng cấu hình email cửa hàng.
   - Frontend không còn dùng email demo `hello@netjewelry.example` làm email liên hệ.
   - Nếu backend chưa có email, footer hiển thị `Email liên hệ chưa cấu hình`.

4. Quản lý danh mục/thương hiệu
   - Bỏ cột ID nội bộ khỏi bảng quản lý vì không cần thiết cho người vận hành.
   - Form danh mục không còn bắt nhập `ID danh mục cha` thủ công; đã đổi sang dropdown chọn danh mục cha từ dữ liệu DB.
   - Tách nút `Thêm danh mục` và `Thêm thương hiệu` để thao tác rõ ràng hơn.
   - Bảng thương hiệu bỏ cột logo URL dài, vẫn giữ trường logo trong form thêm/sửa vì DB có cột `logo`.

5. Quản lý banner/nội dung
   - Tách nút `Thêm banner` và `Thêm nội dung` thay vì mở một modal rồi bắt chọn loại.
   - Bảng banner bỏ cột ảnh URL dài, vẫn giữ trường ảnh trong form vì DB có cột `hinh_anh` bắt buộc.

6. Quản lý thanh toán
   - Bảng thanh toán bỏ cột ID nội bộ.
   - Bổ sung cột thời gian thanh toán để khớp ý nghĩa cột `thoi_gian_thanh_toan` trong DB.

7. Dữ liệu demo/fallback
   - Xóa file frontend cấu hình brand cũ không còn được import, có email `.example` dễ gây hiểu nhầm.
   - Website tiếp tục lấy dữ liệu qua REST API; nơi nào DB rỗng sẽ hiển thị trạng thái trống thay vì dựng danh sách sản phẩm giả.

## Những phần giữ nguyên

- Không đổi `database.sql`.
- Không đổi cấu trúc package hoặc kiến trúc code hiện tại.
- Không bỏ các module `ma_giam_gia`, `banner`, `noi_dung_trang`, `yeu_thich`, `giao_hang` vì các bảng này tồn tại trong `database.sql`.
- Không thêm phương thức thanh toán mới ngoài enum hiện có. Chuyển khoản QR/VietQR/SePay vẫn dùng `BANK_TRANSFER` để khớp DB.

## Lưu ý tiếp theo

- Nếu muốn footer lấy email/tên thương hiệu hoàn toàn từ DB, cần bổ sung bảng cấu hình cửa hàng hoặc mở rộng `noi_dung_trang` theo thiết kế riêng. Lần này chưa làm vì yêu cầu không thay đổi cấu trúc database.
- Cột `ONLINE` vẫn tồn tại trong enum DB của `thanh_toan` và `giao_dich`, nhưng frontend không hiển thị thanh toán online giả lập.
