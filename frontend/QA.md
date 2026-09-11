# QA frontend

## Trạng thái hiện tại

Frontend đã được kiểm tra với backend Spring Boot và MySQL chạy qua Docker Compose.

## Kiểm tra tự động

```text
npm test: 11/11 passed
npm run build: passed
```

## Kiểm tra API thật qua `http://127.0.0.1:5173/api`

Các endpoint đã gọi thành công:

- Public products
- Products filter theo category ID
- Categories
- Brands
- Materials
- Customer cart
- Customer wishlist
- Admin products
- Admin orders
- Inventory
- Admin reviews
- Admin payments
- Admin coupons
- Admin banners
- Admin contents
- Admin dashboard statistics

## Các lỗi đã sửa

- Bỏ toàn bộ dữ liệu demo frontend và không fallback sang mock data.
- Sửa login dùng email hoặc tên đăng nhập cho khách hàng, nhân viên, quản lý.
- Xóa token cũ trước khi login để tránh request login bị dính quyền cũ.
- Sửa URL danh mục từ `category=Nhẫn` sang `categoryId=1` để backend không trả 400.
- Sửa dropdown danh mục tự đóng sau khi chọn.
- Thêm icon sản phẩm yêu thích trong tài khoản.
- Sửa quản lý đơn hàng có tìm mã đơn/ngày, chuyển trạng thái, hủy đơn chưa xác nhận và xem chi tiết sổ xuống.
- Sửa quản lý kho có tìm kiếm SKU/tên sản phẩm và filter sắp hết hàng.
- Sửa xử lý đánh giá chỉ còn phản hồi, bỏ nút ẩn đánh giá trong modal.
- Sửa form thêm/sửa sản phẩm dùng dropdown danh mục/thương hiệu từ DB thay vì nhập ID thủ công.

## Cần chú ý khi test thủ công

- Sau khi rebuild Docker, nên refresh mạnh trình duyệt bằng `Ctrl + F5`.
- Nếu login vẫn báo lỗi cũ, xóa storage trình duyệt:

```js
localStorage.clear()
sessionStorage.clear()
location.reload()
```

- Tài khoản demo `demo@netjewelry.vn` không còn dùng.

