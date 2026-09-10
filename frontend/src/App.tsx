import { Routes, Route, Link } from "react-router-dom";
import { StoreProvider } from "./context/StoreContext";
import { StoreLayout } from "./components/layout/StoreLayout";
import Home from "./pages/Home";
import Products from "./pages/Products";
import ProductDetail from "./pages/ProductDetail";
import Wishlist from "./pages/Wishlist";
import Cart from "./pages/Cart";
import Checkout from "./pages/Checkout";
import Auth from "./pages/Auth";
import Account from "./pages/Account";
import About from "./pages/About";
import { EmptyState } from "./components/common/Feedback";
export default function App() {
  return (
    <StoreProvider>
      <Routes>
        <Route element={<StoreLayout />}>
          <Route index element={<Home />} />
          <Route path="products" element={<Products />} />
          <Route path="products/:id" element={<ProductDetail />} />
          <Route path="wishlist" element={<Wishlist />} />
          <Route path="cart" element={<Cart />} />
          <Route path="checkout" element={<Checkout />} />
          <Route path="login" element={<Auth key="login" />} />
          <Route path="register" element={<Auth key="register" register />} />
          <Route path="account" element={<Account />} />
          <Route path="about" element={<About />} />
          <Route
            path="*"
            element={
              <div className="container page">
                <EmptyState
                  title="Trang không tồn tại"
                  description="Đường dẫn có thể đã thay đổi. Hãy trở về trang chủ để tiếp tục."
                >
                  <Link to="/" className="btn btn-primary">
                    Về trang chủ
                  </Link>
                </EmptyState>
              </div>
            }
          />
        </Route>
      </Routes>
    </StoreProvider>
  );
}
