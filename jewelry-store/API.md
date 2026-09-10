# REST API — schema 25 bảng

Base URL: http://localhost:8080. Success: `{success:true,message,data}`. Lỗi: `{timestamp,status,message,path,fieldErrors?}`. Không trả entity/password. Phân trang bắt đầu 0, size 1–100.

JWT: Authorization: Bearer token. Vai trò DB: KHACH_HANG, NHAN_VIEN, QUAN_LY. Quản lý có quyền của nhân viên; dữ liệu cá nhân luôn kiểm tra chủ sở hữu. CUSTOMER của bản cũ đã được thay bằng KHACH_HANG.

## Hợp đồng cần chú ý

- Login: POST /api/auth/login {identifier,password}; identifier nhận email hoặc tên đăng nhập cho cả ba vai trò. API vẫn nhận email/username làm alias để tương thích. /api/auth/staff/login đã deprecated. GET/PUT /api/account/profile và avatar dành cho cả ba vai trò, chỉ tài khoản đang đăng nhập.
- Register: name/email/password/username; username tùy chọn đối với client cũ, server sinh mã duy nhất nếu bỏ trống. Frontend mới yêu cầu nhập tên đăng nhập. Không nhận role từ khách.
- Products: keyword/category/brand/material/minPrice/maxPrice/sort/page/size. Giá lọc/sắp xếp là giá thấp nhất của biến thể đang bán, dự phòng giá sản phẩm. salePrice là giá khuyến mãi, null nghĩa không có; biến thể có status DANG_BAN/NGUNG_BAN.
- Cart: thêm {variantId,quantity}; sửa {quantity}; PATCH selection nhận {selected:true/false}. Giá, tổng và tồn do server đọc DB.
- Coupon quote: POST /api/coupons/quote {code}. Tổng dựa trên các dòng được chọn trong giỏ server; client không gửi tổng tiền. Quote không giữ lượt mã.
- Order: {address:{name,phone,city,district,ward,street},payment:"COD",note?,couponCode?}, header Idempotency-Key 8–100 ký tự chữ/số/gạch. Retry cùng key/body trả cùng đơn. Chỉ xóa dòng đã mua khỏi giỏ. Đơn trả code, discount, couponCode, payment, delivery và snapshot dòng hàng.
- Trạng thái đơn: CHO_XAC_NHAN → DA_XAC_NHAN → DANG_XU_LY → DANG_GIAO_HANG → HOAN_THANH; có thể DA_HUY trước giao và trước thu tiền. Không đổi trạng thái tùy ý.
- Payment: PENDING/CONFIRMED/FAILED. Tạo đơn mới chỉ COD; hoàn tất đơn ghi thu COD. Không có endpoint simulate. BANK_TRANSFER/ONLINE chỉ được giữ cho dữ liệu đã có.
- Delivery: nhân viên PUT /api/admin/orders/{id}/delivery {carrier,trackingCode,expectedAt?,note?}; trạng thái từ luồng đơn. Khách đọc delivery trong chi tiết đơn của mình.
- Inventory: import/export dùng lượng tăng/giảm; adjust dùng tồn vật lý mới; PATCH threshold dùng {threshold}. Lịch sử có accountId. Không cho tồn dưới số đang giữ.
- Review: chỉ sau mua hoàn tất, stars 1–5; lưu don_hang_id. Ảnh multipart dùng field file; tối đa 5MB; URL JSON HTTPS hoặc đường dẫn uploads do backend cấp.
- Wishlist: GET phân trang; GET /{id} trả boolean; PUT/DELETE /{id} thêm/xóa theo productId của tài khoản hiện tại.
- Banner: public chỉ HIEN_THI và trong khoảng lịch; link phải là đường dẫn trong website khi ghi qua API. Nội dung hỗ trợ FAQ/CHINH_SACH/GIOI_THIEU/LIEN_HE/TRANG_CHU và slug duy nhất. Frontend render văn bản thuần.
- Thống kê: from/to là ngày UTC gồm hai đầu; doanh thu theo ngày hoàn tất, sau giảm giá và chưa có ship. Theo sản phẩm/danh mục phân bổ giảm theo tỷ lệ dòng. Chi tiêu khách gồm ship. Dashboard đếm ngày đặt và chỉ tính tài khoản KHACH_HANG vào số khách.

## Endpoint và quyền

| Method | Endpoint | Quyền |
|---|---|---|
| GET | `/api/wishlist/{id}` | KHACH_HANG |
| PUT | `/api/wishlist/{id}` | KHACH_HANG |
| DELETE | `/api/wishlist/{id}` | KHACH_HANG |
| PUT | `/api/variants/{id}` | NHAN_VIEN, QUAN_LY |
| DELETE | `/api/variants/{id}` | NHAN_VIEN, QUAN_LY |
| PUT | `/api/reviews/{id}` | KHACH_HANG |
| DELETE | `/api/reviews/{id}` | KHACH_HANG |
| GET | `/api/products/{id}` | Public |
| PUT | `/api/products/{id}` | NHAN_VIEN, QUAN_LY |
| DELETE | `/api/products/{id}` | NHAN_VIEN, QUAN_LY |
| PUT | `/api/product-images/{id}/primary` | NHAN_VIEN, QUAN_LY |
| PUT | `/api/gold-prices/{id}` | QUAN_LY |
| DELETE | `/api/gold-prices/{id}` | QUAN_LY |
| GET | `/api/categories/{id}` | Public |
| PUT | `/api/categories/{id}` | QUAN_LY |
| DELETE | `/api/categories/{id}` | QUAN_LY |
| PUT | `/api/cart/items/{id}` | KHACH_HANG |
| DELETE | `/api/cart/items/{id}` | KHACH_HANG |
| GET | `/api/brands/{id}` | Public |
| PUT | `/api/brands/{id}` | QUAN_LY |
| DELETE | `/api/brands/{id}` | QUAN_LY |
| PUT | `/api/auth/password` | KHACH_HANG, NHAN_VIEN, QUAN_LY |
| GET | `/api/admin/staff/{id}` | QUAN_LY |
| PUT | `/api/admin/staff/{id}` | QUAN_LY |
| PUT | `/api/admin/reviews/{id}/reply` | NHAN_VIEN, QUAN_LY |
| PUT | `/api/admin/orders/{id}/delivery` | NHAN_VIEN, QUAN_LY |
| GET | `/api/admin/customers/{id}` | QUAN_LY |
| PUT | `/api/admin/customers/{id}` | QUAN_LY |
| PUT | `/api/admin/customers/{customerId}/addresses/{id}` | QUAN_LY |
| DELETE | `/api/admin/customers/{customerId}/addresses/{id}` | QUAN_LY |
| PUT | `/api/admin/customers/{customerId}/addresses/{id}/default` | QUAN_LY |
| PUT | `/api/admin/coupons/{id}` | QUAN_LY |
| DELETE | `/api/admin/coupons/{id}` | QUAN_LY |
| PUT | `/api/admin/contents/{id}` | QUAN_LY |
| DELETE | `/api/admin/contents/{id}` | QUAN_LY |
| PUT | `/api/admin/banners/{id}` | QUAN_LY |
| DELETE | `/api/admin/banners/{id}` | QUAN_LY |
| PUT | `/api/addresses/{id}` | KHACH_HANG |
| DELETE | `/api/addresses/{id}` | KHACH_HANG |
| PUT | `/api/addresses/{id}/default` | KHACH_HANG |
| GET | `/api/account/profile` | KHACH_HANG, NHAN_VIEN, QUAN_LY |
| PUT | `/api/account/profile` | KHACH_HANG, NHAN_VIEN, QUAN_LY |
| PUT | `/api/account/avatar` | KHACH_HANG, NHAN_VIEN, QUAN_LY |
| DELETE | `/api/account/avatar` | KHACH_HANG, NHAN_VIEN, QUAN_LY |
| POST | `/api/reviews/{id}/images` | KHACH_HANG |
| POST | `/api/reviews/{id}/images/upload` | KHACH_HANG |
| GET | `/api/products` | Public |
| POST | `/api/products` | NHAN_VIEN, QUAN_LY |
| GET | `/api/products/{productId}/reviews` | Public |
| POST | `/api/products/{productId}/reviews` | KHACH_HANG |
| GET | `/api/products/{id}/variants` | Public |
| POST | `/api/products/{id}/variants` | NHAN_VIEN, QUAN_LY |
| GET | `/api/products/{id}/images` | Public |
| POST | `/api/products/{id}/images` | NHAN_VIEN, QUAN_LY |
| POST | `/api/products/{id}/images/upload` | NHAN_VIEN, QUAN_LY |
| GET | `/api/orders` | KHACH_HANG |
| POST | `/api/orders` | KHACH_HANG |
| POST | `/api/orders/{id}/cancel` | KHACH_HANG |
| POST | `/api/inventory/import` | NHAN_VIEN, QUAN_LY |
| POST | `/api/inventory/export` | NHAN_VIEN, QUAN_LY |
| POST | `/api/inventory/adjust` | NHAN_VIEN, QUAN_LY |
| GET | `/api/gold-prices` | Public |
| POST | `/api/gold-prices` | QUAN_LY |
| POST | `/api/coupons/quote` | KHACH_HANG |
| GET | `/api/categories` | Public |
| POST | `/api/categories` | QUAN_LY |
| POST | `/api/cart/items` | KHACH_HANG |
| GET | `/api/brands` | Public |
| POST | `/api/brands` | QUAN_LY |
| POST | `/api/auth/staff/login` | Public |
| POST | `/api/auth/reset-password` | Public |
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/logout` | KHACH_HANG, NHAN_VIEN, QUAN_LY |
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/forgot-password` | Public |
| GET | `/api/admin/staff` | QUAN_LY |
| POST | `/api/admin/staff` | QUAN_LY |
| POST | `/api/admin/payments/{id}/confirm` | NHAN_VIEN, QUAN_LY |
| GET | `/api/admin/customers/{customerId}/addresses` | QUAN_LY |
| POST | `/api/admin/customers/{customerId}/addresses` | QUAN_LY |
| GET | `/api/admin/coupons` | QUAN_LY |
| POST | `/api/admin/coupons` | QUAN_LY |
| GET | `/api/admin/contents` | QUAN_LY |
| POST | `/api/admin/contents` | QUAN_LY |
| GET | `/api/admin/banners` | QUAN_LY |
| POST | `/api/admin/banners` | QUAN_LY |
| GET | `/api/addresses` | KHACH_HANG |
| POST | `/api/addresses` | KHACH_HANG |
| POST | `/api/account/avatar/upload` | KHACH_HANG, NHAN_VIEN, QUAN_LY |
| PATCH | `/api/inventory/{variantId}/threshold` | NHAN_VIEN, QUAN_LY |
| PATCH | `/api/cart/items/{id}/selection` | KHACH_HANG |
| PATCH | `/api/admin/staff/{id}/status` | QUAN_LY |
| PATCH | `/api/admin/staff/{id}/role` | QUAN_LY |
| PATCH | `/api/admin/reviews/{id}/status` | NHAN_VIEN, QUAN_LY |
| PATCH | `/api/admin/orders/{id}/status` | NHAN_VIEN, QUAN_LY |
| PATCH | `/api/admin/customers/{id}/status` | QUAN_LY |
| GET | `/api/wishlist` | KHACH_HANG |
| GET | `/api/storefront/settings` | Public |
| GET | `/api/products/materials` | Public |
| GET | `/api/payments/{id}` | KHACH_HANG |
| GET | `/api/inventory` | NHAN_VIEN, QUAN_LY |
| GET | `/api/inventory/{variantId}` | NHAN_VIEN, QUAN_LY |
| GET | `/api/inventory/{variantId}/history` | NHAN_VIEN, QUAN_LY |
| GET | `/api/gold-prices/current` | Public |
| GET | `/api/contents` | Public |
| GET | `/api/contents/{slug}` | Public |
| GET | `/api/cart` | KHACH_HANG |
| DELETE | `/api/cart` | KHACH_HANG |
| GET | `/api/banners` | Public |
| GET | `/api/auth/me` | KHACH_HANG, NHAN_VIEN, QUAN_LY |
| GET | `/api/admin/transactions` | NHAN_VIEN, QUAN_LY |
| GET | `/api/admin/statistics/revenue` | QUAN_LY |
| GET | `/api/admin/statistics/dashboard` | QUAN_LY |
| GET | `/api/admin/statistics/customers` | QUAN_LY |
| GET | `/api/admin/reviews` | NHAN_VIEN, QUAN_LY |
| GET | `/api/admin/products` | NHAN_VIEN, QUAN_LY |
| GET | `/api/admin/products/{id}` | NHAN_VIEN, QUAN_LY |
| GET | `/api/admin/payments` | NHAN_VIEN, QUAN_LY |
| GET | `/api/admin/orders` | NHAN_VIEN, QUAN_LY |
| GET | `/api/admin/orders/{id}` | NHAN_VIEN, QUAN_LY |
| GET | `/api/orders/{id}` | KHACH_HANG |
| GET | `/api/admin/customers` | QUAN_LY |
| DELETE | `/api/review-images/{id}` | KHACH_HANG |
| DELETE | `/api/product-images/{id}` | NHAN_VIEN, QUAN_LY |
| DELETE | `/api/admin/reviews/{id}` | NHAN_VIEN, QUAN_LY |

## Status và request mẫu

200 thành công; 201 tạo mới ở các endpoint đã khai báo; 400 request/nghiệp vụ; 401 chưa đăng nhập; 403 không đủ quyền; 404 không có dữ liệu; 409 trùng dữ liệu/tồn kho/chuyển trạng thái; 413 file quá lớn; 500 lỗi nội bộ đã ẩn; 503 email chưa cấu hình.

Schema và status từng operation ở openapi.json hoặc /swagger-ui.html. Import collection + environment trong postman; điền giá trị rỗng bằng tài khoản/ID thật của bạn. Ví dụ request không tự tạo tài khoản và không phải dữ liệu dự phòng của website. starts_at/ends_at dùng ISO-8601 UTC. Chọn trạng thái kế tiếp hợp lệ cho đơn, không gửi ngẫu nhiên enum đầu tiên.

Tạo lại tài liệu: chạy mvn test (xuất target/openapi.json), rồi python scripts/generate_api_docs.py. Script chỉ ghi tài liệu, không gọi API và không thay đổi DB.
