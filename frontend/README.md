# NÉT Jewelry — Frontend

Frontend React + TypeScript strict + Vite + Tailwind CSS + React Router + Axios + Lucide. Backend Spring Boot ở `../jewelry-store` được giữ nguyên. Backend hiện chỉ có application bootstrap, chưa có REST controller, nên ứng dụng mặc định chạy demo.

## Chạy project

Yêu cầu Node.js 22.12+ và npm.

```sh
cd frontend
npm install
npm run dev
```

```sh
npm run build
npm run test
npm run preview
```

Sao chép `.env.example` thành `.env` khi cần cấu hình. Vite cần khởi động lại sau khi thay biến môi trường.

## Chế độ demo / REST API

`VITE_DEMO_MODE=true` (mặc định khi chưa cấu hình) dùng catalog tập trung trong `src/data/products.ts`. Giỏ hàng, wishlist, địa chỉ, đơn hàng demo chỉ được lưu trên thiết bị; phiên demo dùng sessionStorage. Không lưu mật khẩu, không gửi email, không thực hiện thanh toán/giao hàng. Ảnh minh họa có thể được dùng lại giữa các thiết kế và thumbnail.

Tài khoản demo: **demo@netjewelry.vn / NetDemo123!**. Đăng ký demo mở một phiên mới, không tạo tài khoản bền vững trên máy chủ; sau đăng xuất dùng tài khoản demo trên để đăng nhập lại. Lịch sử đơn demo được nhóm theo email khi checkout. Wishlist hiện là danh sách lưu trên thiết bị, không đồng bộ tài khoản.

Để tích hợp backend:

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api
VITE_DEMO_MODE=false
```

Không tự fallback sang dữ liệu demo khi API lỗi. Auth thật giả định phiên cookie HttpOnly; Axios dùng `withCredentials`, không lưu token trong localStorage. Backend cần cho phép đúng frontend origin với credentials và thực hiện xác thực, phân quyền, kiểm tra tồn kho, giá, phí giao hàng và chống đơn lặp. Tổng tiền frontend chỉ là dự tính; tổng tiền xác nhận lấy từ response của server.

### REST contract dự kiến

Các kiểu response nằm ở `src/types/index.ts`. Response trả trực tiếp dữ liệu, không bọc trong `data`. Nếu backend dùng envelope, chỉnh tại service.

| Method | Endpoint           | Request                                                                                | Response                                        |
| ------ | ------------------ | -------------------------------------------------------------------------------------- | ----------------------------------------------- |
| GET    | `/products`        | query `search,category,maxPrice,brand,material,gender,sort,page`                       | `{items: Product[],total:number,pages:number}`  |
| GET    | `/products/:id`    | —                                                                                      | `Product`                                       |
| GET    | `/cart`            | guest session hoặc authenticated session                                               | `CartLine[]`                                    |
| PUT    | `/cart`            | `{items:[{productId,quantity}]}`                                                       | `CartLine[]` với giá/tồn kho mới                |
| POST   | `/orders`          | `{items:[{productId,quantity}],address,email,payment,note}` + header `Idempotency-Key` | `Order`, server xóa giỏ hàng sau tạo thành công |
| GET    | `/orders`          | authenticated session                                                                  | `Order[]` chỉ của người đang đăng nhập          |
| POST   | `/auth/login`      | `{email,password}`                                                                     | `User` + session cookie                         |
| POST   | `/auth/register`   | `{name,email,password}`                                                                | `User` + session cookie                         |
| GET    | `/auth/me`         | session cookie                                                                         | `User` hoặc HTTP 401                            |
| POST   | `/auth/logout`     | session cookie                                                                         | HTTP 2xx, hủy phiên                             |
| PATCH  | `/account`         | `{name,phone}`                                                                         | `User`                                          |
| GET    | `/account/address` | authenticated session                                                                  | `Address` hoặc `null`                           |
| PUT    | `/account/address` | `Address`                                                                              | `Address`                                       |

`sort`: `price-asc`, `price-desc`, `newest`, hoặc bỏ trống. Page bắt đầu từ 1; demo 8 sản phẩm/trang. `payment`: `cod` hoặc `bank_transfer`. Lỗi server: `{message:string}` và HTTP status phù hợp. Khi dùng Spring Security CSRF, thống nhất cookie `XSRF-TOKEN` / header `X-XSRF-TOKEN`, cấp cookie từ backend trước mutation; không tắt CSRF chỉ để nối frontend. Chưa xác nhận contract với backend thực tế vì chưa có endpoint.

Chuyển khoản hiện chỉ chọn phương thức và ghi nhận đơn; chưa tích hợp cổng thanh toán hoặc mã QR ngân hàng.

## Cấu trúc

```text
src/
  components/common/   Button, Input, Modal, Pagination, Feedback, Breadcrumbs
  components/layout/   Header, Footer, StoreLayout
  components/product/  ProductCard, ProductGrid, ProductFilter, ProductGallery
  components/cart/     CartDrawer, CartItem, CartSummary, QuantityControl
  components/account/  ProfileForm, Orders, AddressForm
  pages/               Home, Products, ProductDetail, Cart, Checkout,
                       Auth (login/register), Account, Wishlist, About
  context/             StoreContext (user, cart, wishlist, notices)
  hooks/               useProducts, useWebTools
  services/            api, productService, cartService, orderService,
                       authService, accountService
  data/                config, products (demo)
  types/               REST/UI models
  utils/               format, validation, storage, commerce.test
```

Không có Admin vì project hiện không có Admin. Thương hiệu/email thay tại `src/data/config.ts`; email `.example` là placeholder cần thay trước khi vận hành thật. Màu và spacing nằm trong `src/styles.css`, Tailwind dành cho utility nhỏ. Không dùng Redux.

## Design & accessibility

Primary `#123B6D`, text `#111111`, secondary `#666666`, nền trắng/`#F8F8F8`, border `#E5E5E5`. Font Be Vietnam Pro với fallback sans-serif. Không shadow/gradient/glow. Button 6px; motion 180–300ms; hỗ trợ reduced-motion. Native dialog cung cấp focus trap, Escape và trả focus về trigger. Input có label, `aria-invalid`, validation message; các toast và loading có live region; skip link tới main. Grid 2 cột mobile, 3 tablet/catalog, 4 desktop homepage. Bộ lọc/menu chuyển drawer trên mobile.

WebMCP tùy chọn: `navigate_product_search` chỉ điều hướng tìm kiếm qua cùng Router như giao diện. Feature detection và cleanup bằng AbortSignal. Trình duyệt không hỗ trợ vẫn hoạt động bình thường.

## Ảnh

Ảnh minh họa từ Unsplash, theo Unsplash License, được dẫn trực tiếp từ CDN:

- Mathilde Langevin: [hero](https://unsplash.com/photos/gold-necklace-on-white-textile-QgK0yj6kc6k), [vòng cổ](https://unsplash.com/photos/gold-necklace-on-white-textile-v3hCo9Jljfo), [vòng tay](https://unsplash.com/photos/gold-bracelet-on-white-textile-xWSYLbHBgTY).
- Nataliya Melnychuk: [nhẫn](https://unsplash.com/photos/gold-ring-on-white-textile-Ki7TPcA9204).
- JESUS ECA: [bông tai](https://unsplash.com/photos/a-pair-of-gold-earrings-on-a-white-surface-DRbPrVTyTyA).

Font Google Fonts và ảnh CDN cần internet. Khi vận hành thật nên thay bằng ảnh đúng SKU và tự host asset nếu cần kiểm soát cache/availability.

## Triển khai

`npm run build` tạo `dist/`. Static host cần SPA fallback về `index.html` cho các route React Router. Sites metadata nằm ở `.openai/hosting.json`. Không đưa `.env`, node_modules, dữ liệu demo trong trình duyệt hoặc credential vào source repository.
