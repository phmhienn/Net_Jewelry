# Chạy project với Docker Compose

## 1. Chuẩn bị

Cài Docker Desktop và bật Linux containers. Không cần Java/Maven/Node/MySQL trên máy để chạy qua Docker.

Mở PowerShell:

```powershell
Set-Location 'C:\Users\Penguin\Downloads\jewelry-store\backend'
docker version
docker compose version
```

`docker version` cần có cả Client và Server. Các lệnh dưới dùng `docker compose`.

## 2. Chọn database đúng phiên bản

Code hiện theo **database.sql 25 bảng** (tai_khoan/vai_tro). DB 19 bảng trước đây không tương thích.

- Nếu chạy một DB Docker mới: dùng `docker/init.sql` đã chuẩn bị sẵn. File này cùng cấu trúc/dữ liệu mặc định với database.sql nhưng bỏ DROP DATABASE, CREATE DATABASE và USE. Giữ file gốc làm nguồn thiết kế.
- Nếu DB 25 bảng của bạn đã có dữ liệu trên MySQL khác: dùng hướng dẫn kết nối backend bên ngoài Docker ở README.md hoặc điều chỉnh DB_URL/user/password của service backend trong Compose; không import lại SQL.
- Nếu volume Docker cũ chứa schema 19 bảng: giữ volume cũ và chạy một project Docker mới bằng cách đổi `-p jewelry-store` thành `-p jewelry-v25`. Không dùng `down -v` để chuyển phiên bản.

Khi bạn thay database.sql sau này, chạy `python scripts/prepare_database.py` để cập nhật docker/init.sql (Python chỉ cần cho bước tạo lại file, không cần khi chạy Docker). Script này không kết nối hay thay đổi DB. Với database đã có dữ liệu cần migration riêng.

## 3. Tạo .env

File nằm cùng thư mục với compose.yaml. Đoạn sau tạo secret ngẫu nhiên và không ghi đè file đang có:

```powershell
if (Test-Path .env) { throw 'Da co .env; hay sua file hien tai.' }
function New-LocalSecret {
    $secretBytes = New-Object byte[] 32
    $generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $generator.GetBytes($secretBytes)
    $generator.Dispose()
    [Convert]::ToBase64String($secretBytes)
}
$composeEnv = Get-Content .env.example -Raw
$composeEnv = $composeEnv -replace '(?m)^DB_PASSWORD=.*$', ('DB_PASSWORD=' + (New-LocalSecret))
$composeEnv = $composeEnv -replace '(?m)^MYSQL_ROOT_PASSWORD=.*$', ('MYSQL_ROOT_PASSWORD=' + (New-LocalSecret))
$composeEnv = $composeEnv -replace '(?m)^JWT_SECRET=.*$', ('JWT_SECRET=' + (New-LocalSecret))
[IO.File]::WriteAllText((Join-Path (Get-Location) '.env'), $composeEnv)
```

Giữ secret đã tạo, không commit .env. Nếu terminal có DB_PASSWORD/JWT_SECRET cũ trong biến môi trường, mở terminal mới để tránh chúng ghi đè .env.

Để tạo quản lý đầu tiên trên DB mới, sửa các dòng sau trong .env trước khi khởi động:

```dotenv
BOOTSTRAP_MANAGER_ENABLED=true
ADMIN_NAME=Ho ten quan ly
ADMIN_EMAIL=your-email@example.com
ADMIN_USERNAME=your-manager-username
ADMIN_PASSWORD=your-own-password
FRONTEND_PORT=5173
BACKEND_PORT=8080
MYSQL_PORT=3308
```

Dùng email/mật khẩu của bạn; mật khẩu ít nhất 8 ký tự. Bootstrap chỉ tạo khi chưa có tài khoản nhân viên/quản lý. Tài khoản demo chỉ xuất hiện nếu bạn chủ động import `demo-data.sql` ở phần dưới.

## 4. Khởi động

```powershell
docker compose -p jewelry-store config --quiet
docker compose -p jewelry-store up -d --build --wait --wait-timeout 300
docker compose -p jewelry-store ps
```

Dùng cùng `-p jewelry-store` trong các lệnh sau. Nếu port đang được Vite/Java/stack Docker khác sử dụng, dừng đúng ứng dụng đó hoặc đổi FRONTEND_PORT/BACKEND_PORT/MYSQL_PORT trong .env. Nếu bạn đang dùng project name khác, kiểm tra bằng `docker ps` rồi dùng đúng project name đó khi rebuild.

| Thành phần | Địa chỉ mặc định |
|---|---|
| Website React/Nginx | http://127.0.0.1:5173 |
| Backend | http://127.0.0.1:8080 |
| Swagger | http://127.0.0.1:8080/swagger-ui.html |
| MySQL trên máy | 127.0.0.1:3308 |

Backend dùng mysql:3306 trong mạng Docker và DDL_AUTO=validate. Nginx proxy /api và /uploads sang backend. MySQL và uploads lưu trong named volumes độc lập với MySQL cài trên Windows.

MySQL chỉ import init.sql khi volume mới; up/restart không cập nhật schema hay chèn lại dữ liệu. Sau lần bootstrap đầu, đặt BOOTSTRAP_MANAGER_ENABLED=false, xóa ADMIN_PASSWORD rồi chạy `docker compose -p jewelry-store up -d backend` để cập nhật environment.

## 5. Nhập dữ liệu và sử dụng

1. Cả khách hàng, nhân viên và quản lý dùng trang http://127.0.0.1:5173/login. Nhập email hoặc tên đăng nhập và mật khẩu. Với Swagger/Postman, dùng POST /api/auth/login, body {"identifier":"email-hoac-ten-dang-nhap","password":"mat-khau-cua-ban"}.
2. Lấy accessToken và Authorize bằng Bearer token.
3. Kiểm tra danh mục mặc định, tạo thương hiệu → sản phẩm → biến thể → nhập tồn kho. Thêm URL ảnh hoặc upload qua API.
4. Quản lý có thể thêm mã giảm giá, banner và nội dung giới thiệu/chính sách/FAQ/liên hệ theo [API.md](backend/API.md).
5. Khách đăng ký trên website → thêm giỏ → chọn dòng cần mua → áp mã nếu có → đặt COD.
6. Nhân viên xử lý đơn qua API: CHO_XAC_NHAN → DA_XAC_NHAN → DANG_XU_LY → DANG_GIAO_HANG → HOAN_THANH. Khi hoàn tất, ghi nhận tiền COD. Khách không tự xác nhận thanh toán.

React có khu vực **Vận hành** tại http://127.0.0.1:5173/management cho nhân viên và quản lý. Nhân viên xử lý đơn, sản phẩm, kho, đánh giá và thanh toán; quản lý có thêm danh mục, thương hiệu, giá vàng, khách hàng, nhân viên, mã giảm giá, banner và nội dung trang. Không có sản phẩm/nội dung trong DB thì website hiển thị trạng thái trống. Không có lựa chọn thanh toán giả lập hoặc dữ liệu dự phòng.

### Dữ liệu kiểm tra tùy chọn

File [demo-data.sql](backend/demo-data.sql) thêm dữ liệu kiểm tra cho toàn bộ luồng chính: ba tài khoản, sản phẩm, biến thể, tồn kho, giỏ hàng, mã giảm giá, đơn đã hoàn thành, thanh toán, giao hàng, đánh giá, banner và trang thông tin. File không có `DROP DATABASE`, `DROP TABLE` hoặc thay đổi schema; các bản ghi của nó dùng tiền tố `DEMO`/`demo_` và có thể import lại.

Chỉ chạy lệnh này khi bạn muốn thêm dữ liệu kiểm tra vào database Docker đang chạy:

```powershell
Get-Content -Raw -Encoding utf8 .\demo-data.sql |
docker compose -p jewelry-store exec -T mysql sh -c 'exec mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"'
```

Nếu stack của bạn mang tên khác, thay `jewelry-store` bằng đúng project name đang chạy. Sau import, đăng nhập tại `/login` bằng `demo_customer`, `demo_staff` hoặc `demo_manager`; mật khẩu của ba tài khoản là `password`. Đây chỉ là tài khoản local để kiểm tra, không dùng cho môi trường thật.

## 6. Kiểm tra dữ liệu

```powershell
docker compose -p jewelry-store exec mysql mysql -u jewelry -p jewelry_store
```

Nhập DB_PASSWORD khi được hỏi, sau đó:

```sql
SHOW TABLES;
SELECT COUNT(*) FROM san_pham;
SOURCE /checks/verify-data.sql;
EXIT;
```

Các dòng violations cần bằng 0. Xem [DATABASE_NOTES.md](backend/DATABASE_NOTES.md) để biết các thay đổi và giới hạn của schema.

## 7. Quản lý stack

```powershell
docker compose -p jewelry-store logs -f backend
docker compose -p jewelry-store logs --tail=100 mysql
docker compose -p jewelry-store stop
docker compose -p jewelry-store start
docker compose -p jewelry-store up -d --build
docker compose -p jewelry-store down
```

`down` giữ DB/ảnh. `down -v` xóa volume DB và ảnh, không dùng nếu cần giữ dữ liệu. Sửa code cần build lại; sửa .env cần up -d, không chỉ restart.

Email quên mật khẩu mặc định tắt. Để kiểm tra email local, đặt RESET_MAIL_ENABLED=true và chạy `docker compose -p jewelry-store --profile mail up -d`; mở http://127.0.0.1:8025. Mailpit là công cụ phát triển, không xuất hiện trên website và không gửi email thật.

## 8. Xử lý lỗi

| Lỗi | Cách xử lý |
|---|---|
| Không thấy Docker Server | Mở Docker Desktop, đợi Engine chạy |
| Thiếu secret | Kiểm tra .env trong thư mục compose.yaml |
| Schema-validation thiếu tai_khoan/vai_tro | Đang kết nối DB 19 bảng; cần đúng DB 25 bảng hoặc migration, không bật update để tự sửa |
| Access denied sau đổi mật khẩu | Mật khẩu volume cũ không tự đổi theo .env; dùng đúng mật khẩu hoặc đổi user trong MySQL |
| MySQL unhealthy | Xem log import, kiểm tra init.sql và volume đang dùng |
| Frontend 502 | Kiểm tra backend healthy và lỗi kết nối/schema |
| Chưa có sản phẩm | Nhập sản phẩm/biến thể/tồn kho; đảm bảo sản phẩm, thương hiệu, danh mục và biến thể đang hoạt động |
| 401 sau nâng cấp | Đăng nhập lại; cấu trúc JWT đã đổi sang tài khoản chung |

Kết quả kiểm tra cuối cùng và phạm vi chưa kiểm tra được ghi ở [IMPLEMENTATION.md](backend/IMPLEMENTATION.md).

## Cấu trúc backend và đăng nhập chung

- Source Java: `backend/src/main/java/com/example/jewelrystore/`.
- Main class: `com.example.jewelrystore.JewelryStoreApplication`. Nếu IDE giữ run configuration cũ `com.pnj.jewelry_store`, chọn lại main class này và Reload Maven.
- Service interfaces ở `service/`, triển khai ở `service/impl/`. Chi tiết xem `backend/ARCHITECTURE.md`.
- Cả ba vai trò dùng `/login` và `/account`; quyền được lấy từ DB. Khách tự đăng ký luôn có vai trò KHACH_HANG, không tự chọn vai trò.
- Khách: hồ sơ, đơn đã mua, địa chỉ, yêu thích. Nhân viên: hồ sơ và xử lý đơn. Quản lý: thêm thống kê cửa hàng. Các API quản lý khác vẫn dùng được qua Swagger/Postman theo phân quyền.
- Đổi package hoặc code Java cần build lại image, không chỉ restart. Với stack hiện tại có tên `jewelry-store`, chạy trong thư mục chứa compose.yaml:

```powershell
docker compose -p jewelry-store up -d --build backend frontend
```

Dùng đúng project đang chạy (kiểm tra bằng `docker compose ls`); nếu bạn đã chọn `jewelry-v25` thì giữ tên đó. Không xóa volume hoặc import lại SQL khi cập nhật code.



