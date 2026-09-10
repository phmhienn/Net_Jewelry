# Đồng bộ project với database.sql — 10/09/2026

## Nguồn thiết kế

`database.sql` hiện có **25 bảng** và là nguồn thiết kế chính. File này được giữ nguyên trong lần chỉnh sửa này. Không tiếp tục dùng mapping/seed của bản 19 bảng.

Các bảng: vai_tro, tai_khoan, dia_chi, danh_muc, thuong_hieu, gia_vang, san_pham, hinh_anh_san_pham, bien_the_san_pham, ton_kho, lich_su_kho, gio_hang, chi_tiet_gio_hang, yeu_thich, ma_giam_gia, don_hang, chi_tiet_don_hang, su_dung_ma_giam_gia, thanh_toan, giao_dich, giao_hang, danh_gia, hinh_anh_danh_gia, banner, noi_dung_trang.

## Kế hoạch và các phần đã sửa

1. Entity/Repository: mapping đủ 25 bảng, đúng cột, kiểu tiền, độ dài, enum, quan hệ, unique và CHECK. Đổi mặc định Hibernate sang `validate`.
2. Tài khoản: thay KhachHang/NhanVien và hai repository cũ bằng TaiKhoan/VaiTro. Email và username duy nhất toàn hệ thống. Vai trò KHACH_HANG, NHAN_VIEN, QUAN_LY lấy từ DB. Đăng ký không nhận quyền từ client. JWT dùng account:id; token cũ cần đăng nhập lại. Khóa/đổi quyền/đổi mật khẩu làm mất hiệu lực token.
3. Catalog: giá khuyến mãi ở sản phẩm và biến thể, trạng thái thương hiệu/biến thể, thứ tự ảnh. Giá biến thể là giá dùng khi mua; khuyến mãi sản phẩm không tự áp sang biến thể. Danh sách lọc/sắp xếp theo giá thấp nhất của biến thể đang bán, dự phòng giá sản phẩm khi chưa có biến thể.
4. Giỏ hàng: lưu da_chon trong DB; thêm mới được chọn mặc định. Khách có thể bỏ/chọn từng dòng. Chỉ các dòng được chọn đi vào đơn; dòng khác được giữ lại.
5. Đơn hàng: mã đơn duy nhất, người nhận, nhân viên xử lý, tiền giảm, mã giảm giá và ngày xác nhận/hoàn tất/hủy. Chi tiết chốt tên/SKU/size/màu/giá; sửa catalog không đổi thông tin đã mua. Đọc được cả địa chỉ dạng văn bản trong SQL và snapshot JSON đã có.
6. Mã giảm giá: quản lý CRUD (xóa là ngừng áp dụng), quote từ giỏ DB, kiểm tra thời gian/giá trị/tối thiểu/quota. Khóa bản ghi trước khi đọc quota để tránh hai đơn cùng dùng lượt cuối. Sử dụng mã, đơn, thanh toán và tồn kho nằm trong cùng transaction. Hủy đơn giải phóng quota một lần; giữ lịch sử sử dụng.
7. Giao hàng: tạo bản ghi cùng đơn; nhân viên cập nhật đơn vị vận chuyển/mã vận đơn/ngày dự kiến. Trạng thái giao đồng bộ từ trạng thái đơn, không cho khách tự đánh dấu giao thành công.
8. Yêu thích: lưu bằng khóa ghép tai_khoan_id/san_pham_id; phân trang, thêm/xóa, cách ly theo tài khoản. Có trang yêu thích và nút trên chi tiết sản phẩm.
9. Nội dung: API quản lý banner và noi_dung_trang chỉ cho QUAN_LY. Banner theo lịch và thứ tự. Trang giới thiệu/chính sách/FAQ/liên hệ lấy DB; trống thì báo chưa có nội dung. Văn bản được React escape, không render HTML tùy ý.
10. Kho/đánh giá/thống kê: dùng ngưỡng cảnh báo từ DB; lịch sử ghi tài khoản thao tác; đánh giá gắn với đơn đã hoàn tất; đếm khách theo vai trò; doanh thu trừ tiền giảm và không tính ship. Báo cáo theo sản phẩm/danh mục phân bổ tiền giảm theo tỷ lệ giá trị dòng hàng.
11. Frontend/Docker/tài liệu: bỏ điều khiển mô phỏng thanh toán; cập nhật các service/type và API. Bỏ seed 19 bảng khỏi luồng chạy; Docker không chép ảnh thử vào uploads.

## Thanh toán sau điều chỉnh

- Website chỉ cho đặt **COD**. Khách không thể tự xác nhận tiền đã thu. Nhân viên hoàn tất đơn thì hệ thống ghi thanh toán và giao dịch COD.
- Đã xóa `/api/payments/{id}/simulate`, SimulationRequest, cờ PAYMENT_SIMULATION_ENABLED, lựa chọn online/chuyển khoản giả lập và nút thành công/thất bại.
- Giữ BANK_TRANSFER/ONLINE trong enum để đọc đúng bản ghi đang có trong schema, nhưng API từ chối tạo đơn mới bằng hai phương thức này. Không tích hợp ngân hàng/cổng thanh toán thật. Không đổi tên “giả lập” thành “thật” để che giấu việc chưa tích hợp.
- Nhân viên vẫn có thể xác nhận bản ghi thanh toán không phải COD đã có sau khi đối soát; quyền này không dành cho khách hàng.

## Những điểm chưa hợp lý trong SQL được xử lý tại tầng service

Giữ nguyên thiết kế DB; không tự thêm bảng/cột:

- File gốc có DROP DATABASE và USE cố định. `scripts/prepare_database.py` tạo `docker/init.sql` cùng schema/index/dữ liệu mặc định, bỏ riêng lệnh reset/chọn database. Docker dùng bản này trên volume mới. Không chạy file gốc trên DB có dữ liệu cần giữ.
- CHECK cho giá khuyến mãi cho phép 0, nhưng đơn giá giỏ/chi tiết đơn và thanh toán bắt buộc >0. Service từ chối mua biến thể có giá thực thu bằng 0 và đơn có tổng thanh toán bằng 0; không tự sửa giá.
- SQL chưa chặn phần trăm >100, giam_toi_da <=0, quota đã dùng vượt tổng, ngưỡng kho âm và thời gian banner đảo chiều. Request/service kiểm tra những điều kiện tương ứng; verify-data.sql giúp tìm bản ghi nhập trực tiếp không hợp lệ.
- FK tai_khoan_id không giới hạn vai trò. Service phân biệt tài khoản khách/nhân viên và kiểm tra quyền/chủ sở hữu ở từng nghiệp vụ.
- SQL không bắt buộc mỗi tài khoản chỉ có một địa chỉ mặc định hoặc mỗi sản phẩm chỉ có một ảnh chính. Service khóa chủ thể khi cập nhật để duy trì điều kiện này.
- SQL cho phép đánh giá không gắn đơn. API tạo mới yêu cầu mua hàng hoàn tất và lưu don_hang_id; bản ghi cũ không có đơn cần được đối chiếu riêng.
- Thiết kế chưa có hoàn tiền và không có trạng thái hủy riêng cho giao_hang. Đơn đã thu tiền không hủy trực tiếp; đơn hủy trước giao được ghi giao hàng THAT_BAI kèm ghi chú. Không tự thêm enum ngoài SQL.

## Dữ liệu và chuyển phiên bản

- Lần này không reset/import vào database đang dùng của bạn. Kiểm tra thực hiện trên MySQL riêng ở 127.0.0.1:3317, database jewelry_test_alignment.
- File SQL chỉ có role và danh mục mặc định; không tự tạo sản phẩm, tài khoản hoặc đơn hàng demo. Tài khoản quản lý đầu tiên tạo bằng bootstrap có cấu hình; khách đăng ký bình thường.
- Seed/Compose override 19 bảng cũ được chuyển vào `.artifacts/legacy-schema-19` để lưu lịch sử, không dùng với bản này.
- Database cũ 19 bảng cần migration có đối chiếu ID và vai trò trước khi chuyển dữ liệu. Không dùng ddl-auto=update để tự hợp nhất hai bảng tài khoản. Với DB 25 bảng đã có dữ liệu, chỉ cấu hình kết nối và dùng validate; không import init.sql lần nữa.
- Hướng dẫn chạy mới ở `../RUNNING.md`; API đầy đủ ở API.md, openapi.json và thư mục postman.

## Kiểm tra

### Refactor package và đăng nhập chung

Backend đã chuyển sang `com.example.jewelrystore`, tách Service/Impl và mapper theo nghiệp vụ. Tên Entity và mapping SQL vẫn giữ theo 25 bảng; không có migration hoặc reset dữ liệu cho lần refactor này. Tra cứu email/tên đăng nhập dùng repository IgnoreCase; role và trạng thái vẫn đọc từ `tai_khoan`/`vai_tro`. Xem `ARCHITECTURE.md`.

### Mapping số sao và bản build Docker

`danh_gia.so_sao` giữ nguyên `TINYINT` trong SQL. Entity `DanhGia.stars` dùng `byte` và `@JdbcTypeCode(SqlTypes.TINYINT)` để Hibernate kiểm tra đúng kiểu JDBC. DTO vẫn nhận số nguyên với validation 1–5. Test `reviewStarsUsesTinyintJdbcMapping` kiểm tra mapping Hibernate thực tế.

Nếu log vẫn báo “expecting INTEGER” sau khi sửa source, cần build lại image backend; chỉ restart container sẽ tiếp tục chạy JAR cũ. Chạy trong thư mục chứa `compose.yaml`, dùng đúng tên project đang chạy (xem `docker compose ls`), ví dụ với project `jewelry-store`:

```powershell
docker compose -p jewelry-store up -d --build --no-deps backend
```

Giữ `DDL_AUTO=validate`; không đổi cột sang INT và không xóa volume MySQL để xử lý lỗi mapping này.

`verify-data.sql` có 18 phép đối chiếu chỉ đọc. Giá trị violations phải bằng 0; lỗi có thể do dữ liệu nhập SQL trực tiếp nên cần xử lý theo bản ghi, không xóa cả DB.

Các test tích hợp bao gồm schema validation, register/login, quyền giữa các loại tài khoản, catalog, giỏ/selection, idempotency, rollback, giữ/xuất/giải phóng tồn, chuyển trạng thái, COD, đánh giá, địa chỉ, wishlist, coupon hết hạn và quota đồng thời, nội dung ẩn/lịch banner, snapshot, giao hàng và thống kê. Kết quả lần chạy cuối được ghi trong IMPLEMENTATION.md.
