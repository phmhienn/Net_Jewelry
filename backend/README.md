# Jewelry Store Backend

Spring Boot 4 / Java 21 / Maven / MySQL 8. REST API theo kiến trúc Controller → Service → Repository. React nằm ở ../frontend.

Package gốc: `com.example.jewelrystore`. Xem [ARCHITECTURE.md](ARCHITECTURE.md) cho cây thư mục mới, trách nhiệm từng lớp và ma trận phân quyền. Cả ba vai trò dùng `POST /api/auth/login` với `{identifier,password}`, trong đó identifier là email hoặc tên đăng nhập; website dùng chung `/login` và `/account`.

Schema chính là database.sql gồm 25 bảng. Xem DATABASE_NOTES.md để biết thay đổi từ bản cũ và các ràng buộc nghiệp vụ. Không có JSP/Thymeleaf, ngân hàng thật, đa chi nhánh hay mobile app.

## Chạy với Docker

Đọc [RUNNING.md](../RUNNING.md). Compose khởi động MySQL, Spring Boot và React/Nginx. Docker dùng docker/init.sql không có lệnh DROP DATABASE. Không dùng seed/compose.seed của schema 19 bảng.

## Chạy backend trực tiếp với database của bạn

Yêu cầu Java 21, Maven và MySQL 8.0.16+ đã có đúng schema 25 bảng. Không chạy database.sql trên dữ liệu cần giữ vì file gốc có DROP DATABASE.

Mở PowerShell trong thư mục chứa pom.xml:

```powershell
$env:DB_URL='jdbc:mysql://127.0.0.1:3306/jewelry_store?connectionTimeZone=UTC'
$env:DB_USERNAME='your-db-user'
$env:DB_PASSWORD='your-db-password'
$env:JWT_SECRET='your-base64-secret-at-least-32-random-bytes'
$env:DDL_AUTO='validate'
$env:CORS_ORIGINS='http://127.0.0.1:5173'
mvn spring-boot:run
```

Thay các giá trị theo môi trường. Cách tạo JWT_SECRET ở RUNNING.md. Maven/Java không tự đọc .env của Docker; đặt biến môi trường trong terminal/IDE. Không hard-code mật khẩu vào application.properties.

Nếu dùng DB mới, cần vai trò KHACH_HANG, NHAN_VIEN, QUAN_LY từ SQL. Bootstrap quản lý qua BOOTSTRAP_MANAGER_ENABLED và ADMIN_NAME/ADMIN_EMAIL/ADMIN_USERNAME/ADMIN_PASSWORD; chỉ chạy khi chưa có nhân viên/quản lý. Khách đăng ký qua API hoặc React.

## API và quyền

Swagger: /swagger-ui.html. Schema JSON: /v3/api-docs. Hợp đồng đầy đủ: API.md, openapi.json; bộ request: postman/JewelryStore.postman_collection.json. JWT gửi qua Authorization: Bearer token.

- Public: catalog, giá vàng, đánh giá công khai, banner đang hiển thị, nội dung công khai, đăng ký/đăng nhập.
- KHACH_HANG: giỏ, chọn mua, mã giảm giá, đặt COD, đơn của mình, địa chỉ, đánh giá đã mua và yêu thích.
- NHAN_VIEN: sản phẩm/ảnh/biến thể, kho, xử lý đơn/giao hàng, xác nhận thanh toán, giao dịch, phản hồi/ẩn đánh giá.
- QUAN_LY: tất cả quyền nhân viên và quản lý tài khoản, danh mục, thương hiệu, giá vàng, mã giảm giá, nội dung, báo cáo.

Không expose entity/mật khẩu. Tiền dùng BigDecimal; ghi đơn/tồn kho/coupon/thanh toán trong transaction. Các thao tác đồng thời dùng khóa bi quan; request đặt hàng có Idempotency-Key.

Thanh toán tạo mới chỉ COD. BANK_TRANSFER và ONLINE được giữ để đọc dữ liệu đã có; không có API simulate. Không có seeder tự tạo sản phẩm/tài khoản/đơn hàng.

## Kiểm tra

```powershell
mvn test
mvn spotless:check
mvn package
```

Test mặc định dùng H2 riêng. Muốn kiểm tra chính xác MySQL, tạo DB mới tên bắt đầu jewelry_test, import docker/init.sql rồi chạy:

```powershell
mvn test '-Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/jewelry_test?connectionTimeZone=UTC' '-Dspring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver' '-Dspring.datasource.username=your-test-user' '-Dspring.datasource.password=your-test-password' '-Dspring.jpa.hibernate.ddl-auto=validate'
```

Test xóa dữ liệu trong DB kiểm tra ở đầu mỗi ca; tuyệt đối không trỏ đến DB đang dùng. verify-data.sql là script chỉ đọc cho DB thực tế. Sau khi chỉnh database.sql, tạo lại docker/init.sql bằng python scripts/prepare_database.py, đối chiếu entity rồi kiểm tra trên MySQL.


