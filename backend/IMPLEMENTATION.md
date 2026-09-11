# Kết quả triển khai — 10/09/2026

## Cập nhật cấu trúc và đăng nhập chung — 10/09/2026

- Package mới `com.example.jewelrystore`; 25 Entity, 22 Service interface và 22 ServiceImpl. Controller không gọi Repository trực tiếp.
- Tách cấu hình JWT/CORS, Product/Category/Brand service, User/Customer/Address service và mapper theo domain. Cây thư mục và lý do giữ tên Entity tiếng Việt ở ARCHITECTURE.md.
- `/api/auth/login` nhận identifier là email hoặc username cho KHACH_HANG/NHAN_VIEN/QUAN_LY. Email/username cũ là alias; endpoint staff/login deprecated.
- `/account` dùng chung: hồ sơ cho cả ba vai trò; mua hàng/địa chỉ cho khách; xử lý đơn cho nhân viên/quản lý; thống kê chỉ quản lý. Backend giữ giới hạn quyền tương ứng.
- `mvn spotless:apply clean package`: **38 test đạt** trên H2 (36 integration + 2 context/mapping).
- `npm test`: **23 test đạt**; TypeScript/Vite production build thành công.
- Docker stack đang dùng `jewelry-store`: backend/frontend build lại thành công, MySQL/backend/frontend đều healthy. Backend vượt schema validation trên MySQL 8.0.44. Không reset/import hoặc sửa dữ liệu MySQL.
- HTTP qua Nginx: `/login`, `/account`, `/api/products` trả 200. OpenAPI thực tế có LoginRequest.identifier/password. Postman cập nhật 125 endpoint, 127 request gồm ví dụ đăng nhập chung cho ba vai trò.
- Database có một khách và một quản lý, chưa có nhân viên. Định danh quản lý DB khác ADMIN_USERNAME/ADMIN_EMAIL trong .env, nên thông tin bootstrap không đăng nhập được. Không thay mật khẩu/tài khoản để kiểm thử. Hai định danh × ba vai trò được xác nhận trong bộ integration test độc lập, không khẳng định đã đăng nhập quản lý thành công trên dữ liệu thật.
- Không thực hiện kiểm thử tương tác bằng trình duyệt hoặc xuất bản Sites. Bản chạy và API của project này dùng Docker local theo RUNNING.md.

Các kết quả dưới đây thuộc bước đối chiếu SQL trước refactor:

Project đã đồng bộ theo database.sql 25 bảng. Bản mô tả chi tiết, những điểm SQL cần lưu ý và giới hạn nghiệp vụ nằm trong DATABASE_NOTES.md. Cách chạy: ../RUNNING.md.

## Kiểm tra đã thực hiện

| Phạm vi | Kết quả |
|---|---|
| Maven test trên H2 mặc định | 34 test, 0 thất bại, 0 lỗi |
| Maven test trên MySQL 8.0.44 | 34 test, 0 thất bại, 0 lỗi |
| Mapping SQL/JPA | Import đúng 25 bảng, chạy Hibernate ddl-auto=validate thành công |
| Java build | Compile, format và đóng gói JAR thành công |
| Frontend | 18 test Vitest thành công, TypeScript strict và Vite production build thành công |
| SQL đối chiếu | 18 điều kiện, tất cả violations = 0 trên DB kiểm tra và Docker |
| Docker Compose | Build Linux images thành công; MySQL, backend, frontend đều healthy |
| HTTP qua Nginx | Trang chủ và SPA route trả 200, REST API proxy đúng |
| DB mới | 5 danh mục mặc định; 0 sản phẩm, banner, nội dung; giỏ và wishlist mới rỗng |
| Auth qua Docker | Đăng ký/đăng nhập thành công, quyền KHACH_HANG từ DB |
| Gỡ thử nghiệm thanh toán | Endpoint simulate trả 404 sau đăng nhập; frontend không có nút/nhãn giả lập |
| API documentation | 125 operation và 126 request Postman, có đăng nhập quản lý riêng |

## Các lỗi được phát hiện và sửa trong quá trình kiểm tra

- Mapping cũ khach_hang/nhan_vien và trạng thái đơn tiếng Anh không còn tương thích SQL mới.
- Quota mã giảm giá bị đọc cũ khi hai đơn dùng đồng thời: đã chuyển sang đọc trực tiếp dưới khóa bi quan, test cạnh tranh đã qua.
- Tổng số khách từng tính cả nhân viên sau khi hợp nhất bảng: đã lọc theo vai trò KHACH_HANG.
- Bộ lọc giá từng dùng giá gốc sản phẩm dù giỏ dùng giá biến thể: đã thống nhất theo giá bán hiển thị.
- Đơn nhập SQL với địa chỉ dạng văn bản từng bị lỗi parse JSON: đã hỗ trợ đọc cả hai dạng.
- Cấu hình Docker trước đây mount SQL có DROP DATABASE và seed cũ: đã dùng init.sql tách riêng, bỏ seed/ảnh thử khỏi luồng chạy.

## Phạm vi môi trường

Kiểm tra dùng database riêng jewelry_test_alignment ở 127.0.0.1:3317 và project Docker riêng jewelry-alignment-25 (5185/8085/3319). Không reset hoặc import vào database đang dùng của người dùng. Không tự chạy migration từ 19 sang 25 bảng; cần đối chiếu dữ liệu trước khi chuyển.

Sau kiểm tra, đã dừng MySQL riêng và dọn container/volume của jewelry-alignment-25. Các cổng kiểm tra không phải địa chỉ chạy thường ngày; khởi động database của bạn theo RUNNING.md.

Không thực hiện kiểm tra bằng trình duyệt/screenshot hoặc triển khai lên website hosted. Tại bước đối chiếu SQL, giao diện quản trị React chưa có. Lần cập nhật ở trên đã bổ sung tab xử lý đơn và thống kê; các thao tác quản trị khác tiếp tục dùng Swagger/Postman. Không có cổng thanh toán ngân hàng thật; checkout tạo mới chỉ COD.
