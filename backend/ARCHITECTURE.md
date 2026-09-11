# Cấu trúc backend và đăng nhập chung

Backend Spring Boot monolith dùng Controller → Service → Repository → MySQL. Package gốc là `com.example.jewelrystore`, theo mẫu cấu trúc yêu cầu. React gọi REST API, không dùng JSP/Thymeleaf.

## Các package

```text
src/main/java/com/example/jewelrystore/
├── JewelryStoreApplication.java
├── config/
│   ├── SecurityConfig.java       # Filter chain, phân quyền, PasswordEncoder
│   ├── CorsConfig.java           # Origin/method/header từ cấu hình
│   ├── JwtConfig.java            # Khóa, encoder, decoder, issuer
│   ├── WebConfig.java            # Tài nguyên upload
│   └── ...                      # Bootstrap và OpenAPI
├── controller/
│   ├── AuthController.java
│   ├── ProductController.java
│   ├── CategoryController.java
│   ├── BrandController.java
│   ├── OrderController.java
│   ├── UserController.java       # Hồ sơ của tài khoản đang đăng nhập
│   ├── CustomerController.java   # Quản lý khách hàng
│   └── ...                      # Giữ các module nghiệp vụ hiện có
├── service/                     # Interface của từng nghiệp vụ
│   ├── AuthService.java
│   ├── ProductService.java
│   ├── CategoryService.java
│   ├── BrandService.java
│   ├── UserService.java
│   ├── CustomerService.java
│   ├── AddressService.java
│   └── impl/                    # Các lớp tương ứng *ServiceImpl
├── repository/                  # JPA repository theo entity/bảng
├── entity/                      # 25 bảng trong database.sql
│   ├── TaiKhoan.java
│   ├── VaiTro.java
│   ├── SanPham.java
│   ├── DonHang.java
│   └── ...
├── dto/
│   ├── request/                 # Jakarta Validation
│   └── response/                # ApiResponse, PageResponse, DTO nghiệp vụ
├── mapper/                      # ProductMapper, OrderMapper, UserMapper...
├── security/
│   ├── JwtTokenProvider.java
│   ├── CustomUserDetailsService.java
│   ├── Actor.java
│   └── CurrentActor.java
├── exception/                   # GlobalExceptionHandler và lỗi nghiệp vụ
└── util/                        # Checks và Pages dùng chung
```

Các module giỏ hàng, kho, giá vàng, thanh toán COD, giao dịch, đánh giá, địa chỉ, nhân viên, thống kê, mã giảm giá, nội dung và yêu thích vẫn được giữ đầy đủ. Tất cả lớp business service triển khai interface tại `service/impl/`.

## Điều chỉnh từ mẫu để phù hợp database

- Giữ tên Entity/Repository tiếng Việt: `TaiKhoan`/`TaiKhoanRepository`, `SanPham`/`SanPhamRepository`... để đối chiếu trực tiếp bảng SQL. Không tạo thêm User/Customer entity cho cùng bảng `tai_khoan`; phân biệt bằng `vai_tro`.
- Dùng `BearerTokenAuthenticationFilter` có sẵn của Spring Security Resource Server. Không tạo thêm `JwtAuthenticationFilter` thủ công để tránh kiểm tra JWT hai lần. `JwtConfig` kiểm tra chữ ký/issuer/hạn dùng; `JwtTokenProvider.authenticate` kiểm tra trạng thái, token version và vai trò từ DB mỗi request.
- `CustomUserDetailsService` là nơi tra cứu tài khoản theo email/tên đăng nhập; `AuthServiceImpl` kiểm tra BCrypt và phát JWT.
- Giữ `Checks` và `Pages` vì đã có nơi sử dụng. Không tạo DateUtils/ValidationUtils rỗng chỉ để giống tên trong ví dụ.
- Tách `CatalogService` thành Product/Category/Brand, `AccountService` thành User/Customer/Address; xóa hai service tổng hợp cũ. Tách `StoreMapper` thành mapper từng nhóm nghiệp vụ.
- Không thay đổi bảng, cột, khóa ngoại, dữ liệu hay trạng thái trong SQL. Giữ mapping `danh_gia.so_sao` là JDBC TINYINT và `ddl-auto=validate`.

## Hợp đồng đăng nhập

```http
POST /api/auth/login
Content-Type: application/json

{"identifier":"email-hoac-ten-dang-nhap","password":"mat-khau-cua-ban"}
```

`identifier` được trim và tra cứu không phân biệt hoa/thường. Email có ký tự `@`; tên đăng nhập theo validation hiện có không chứa `@`. Password giữ nguyên, không trim hoặc chuyển hoa/thường. Mọi vai trò dùng cùng endpoint, không gửi/chọn role từ client.

Response giữ cấu trúc `{success,message,data:{accessToken,tokenType,expiresIn,user}}`; `user.role` là KHACH_HANG/NHAN_VIEN/QUAN_LY từ DB. Không trả password/hash.

Body cũ `{email,password}` hoặc `{username,password}` vẫn được chấp nhận qua JsonAlias. Endpoint `/api/auth/staff/login` được đánh dấu deprecated, giữ giới hạn nhân viên/quản lý để tương thích client cũ; website mới không gọi endpoint đó.

Tài khoản bị khóa, không tồn tại hoặc sai mật khẩu trả 401 với thông báo chung. Logout/đổi mật khẩu vô hiệu hóa JWT qua token version. Register công khai chỉ tạo KHACH_HANG.

## Trang dùng chung và quyền

| Chức năng | Khách hàng | Nhân viên | Quản lý |
|---|---|---|---|
| `/login`, `/account`, cập nhật hồ sơ của mình | Có | Có | Có |
| Mua hàng, giỏ, yêu thích, địa chỉ, đơn đã mua | Có | Không | Không |
| Tab xử lý đơn, API quản lý sản phẩm/kho/thanh toán/đánh giá | Không | Có | Có |
| Tab thống kê, API quản lý nhân viên/khách/danh mục/thương hiệu/nội dung | Không | Không | Có |

Frontend giữ role trong User, chỉ hiển thị tab hợp lệ và chặn route mua hàng đối với tài khoản nhân viên/quản lý. Backend là nơi quyết định quyền cuối cùng: sửa URL, request hoặc local state không cấp thêm quyền. Hồ sơ tự sửa không nhận ID/role; luôn lấy tài khoản từ JWT. Các tab quản lý hiện bổ sung xử lý đơn và thống kê; không thay thế toàn bộ bộ API quản trị qua Swagger/Postman.

## Chạy và kiểm tra

- Main class mới: `com.example.jewelrystore.JewelryStoreApplication`.
- Maven: `mvn clean package` để loại bỏ class của package cũ.
- Docker: dùng đúng tên compose project đang chạy và `up -d --build backend frontend` (xem `../RUNNING.md`). Không cần reset database.
- Test Spring Boot dùng H2 riêng, không ghi vào MySQL đang chạy. Bộ test bao gồm hai định danh × ba vai trò, khóa/sai mật khẩu, giới hạn quyền, sửa hồ sơ đúng chủ thể và hồi quy nghiệp vụ.
- Nguồn trước refactor được lưu cục bộ trong `../.artifacts/before-structure-refactor.zip` (không chứa .env).
