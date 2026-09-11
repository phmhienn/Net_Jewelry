# NÉT Jewelry — Frontend

Frontend React + TypeScript + Vite cho website bán trang sức. Ứng dụng hiện dùng REST API từ Spring Boot backend trong thư mục `../backend`, không còn dùng dữ liệu demo trong trình duyệt.

## Chạy bằng Docker Compose

Cách khuyến nghị cho đồ án là chạy từ thư mục backend vì file compose nằm ở đó:

```sh
cd ../backend
docker compose -p jewelry-store up -d --build
```

Sau khi chạy:

- Frontend: http://127.0.0.1:5173
- Backend API: http://127.0.0.1:8080/api
- MySQL: 127.0.0.1:3308

## Chạy frontend riêng khi phát triển

```sh
cd frontend
npm install
npm run dev
```

File `.env` có thể dùng:

```dotenv
VITE_API_BASE_URL=/api
```

Khi chạy Vite riêng mà không qua nginx Docker, có thể đổi thành:

```dotenv
VITE_API_BASE_URL=http://127.0.0.1:8080/api
```

## Tài khoản kiểm tra

Các tài khoản này được tạo từ `../backend/demo-data.sql`:

```text
Quản lý:  manager01 / 1236qtth
Nhân viên: staff01 / 1236qtth
Khách hàng: customer01 / 1236qtth
```

Có thể đăng nhập bằng email hoặc tên đăng nhập.

## Cấu trúc chính

```text
src/
  components/common/   Button, Input, Modal, Pagination, Feedback, route guards
  components/layout/   Header, Footer, StoreLayout
  components/product/  ProductCard, ProductGrid, ProductFilter, ProductGallery, WishlistButton
  components/cart/     CartDrawer, CartItem, CartSummary, QuantityControl
  components/account/  ProfileForm, Orders, AddressForm, ManageOrders, ManagerStatistics
  context/             StoreContext, CatalogContext
  hooks/               useProducts
  pages/               Home, Products, ProductDetail, Cart, Checkout,
                       Auth, Account, Wishlist, About, Management, Information
  services/            api, authService, productService, cartService,
                       orderService, accountService, wishlistService,
                       contentService, managementService
  types/               UI/REST models
  utils/               format, validation, access
```

## REST API đang dùng

Frontend gọi API qua `src/services/api.ts`. Service tự bóc response envelope dạng:

```json
{ "success": true, "message": "...", "data": {} }
```

Các nhóm API chính:

- Public: `/products`, `/products/{id}`, `/categories`, `/brands`, `/products/materials`, `/banners`, `/contents`, `/storefront/settings`
- Auth: `/auth/login`, `/auth/register`, `/auth/me`, `/auth/logout`, `/auth/forgot-password`, `/auth/reset-password`
- Customer: `/cart`, `/orders`, `/addresses`, `/wishlist`
- Staff/Manager: `/admin/orders`, `/inventory`, `/admin/reviews`, `/admin/payments`, `/admin/transactions`, `/admin/statistics/dashboard`
- Manager: `/categories`, `/brands`, `/gold-prices`, `/admin/customers`, `/admin/staff`, `/admin/coupons`, `/admin/banners`, `/admin/contents`

Auth dùng JWT trong `localStorage` key `net-jewelry-token`. Khi đăng nhập, frontend xóa token cũ trước khi gửi request login để tránh lỗi quyền do phiên cũ.

## Quy tắc dữ liệu

- Không còn `src/data/products.ts`.
- Không fallback sang dữ liệu demo nếu API lỗi.
- Nếu DB chưa có sản phẩm/danh mục/banner, giao diện hiển thị empty state như “Chưa có sản phẩm”.
- Danh mục và thương hiệu trong filter dùng ID từ DB; URL có dạng `?categoryId=1`.

## Kiểm tra

```sh
npm test
npm run build
```

Kết quả gần nhất:

```text
Vitest: 11 passed
Vite build: passed
```

## Giao diện

Phong cách Minimal Flat E-commerce UI:

- Primary: `#123B6D`
- Text: `#111111`
- Secondary text: `#666666`
- Background: `#FFFFFF` / `#F8F8F8`
- Border: `#E5E5E5`

Không dùng box-shadow, gradient, glow, glassmorphism. Animation nhẹ, có hỗ trợ `prefers-reduced-motion`.



