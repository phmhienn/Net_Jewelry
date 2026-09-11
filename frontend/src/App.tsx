import { Routes, Route, Link } from "react-router-dom";
import { StoreProvider } from "./context/StoreContext";
import { CatalogProvider } from "./context/CatalogContext";
import { StoreLayout } from "./components/layout/StoreLayout";
import { CustomerRoute } from "./components/common/CustomerRoute";
import { StaffRoute } from "./components/common/StaffRoute";
import Home from "./pages/Home";
import Products from "./pages/Products";
import ProductDetail from "./pages/ProductDetail";
import Wishlist from "./pages/Wishlist";
import Cart from "./pages/Cart";
import Checkout from "./pages/Checkout";
import Auth from "./pages/Auth";
import Account from "./pages/Account";
import About from "./pages/About";
import Management from "./pages/Management";
import PasswordRecovery from "./pages/PasswordRecovery";
import PaymentPage from "./pages/PaymentPage";
import Information from "./pages/Information";
import { EmptyState } from "./components/common/Feedback";

export default function App() {
  return (
    <StoreProvider>
      <CatalogProvider>
        <Routes>
          <Route element={<StoreLayout />}>
            <Route index element={<Home />} />
            <Route path="products" element={<Products />} />
            <Route path="products/:id" element={<ProductDetail />} />
            <Route path="wishlist" element={<CustomerRoute><Wishlist /></CustomerRoute>} />
            <Route path="cart" element={<CustomerRoute><Cart /></CustomerRoute>} />
            <Route path="checkout" element={<CustomerRoute><Checkout /></CustomerRoute>} />
            <Route path="payment/success" element={<CustomerRoute><PaymentPage success /></CustomerRoute>} />
            <Route path="payment/:orderId" element={<CustomerRoute><PaymentPage /></CustomerRoute>} />
            <Route path="login" element={<Auth key="login" />} />
            <Route path="register" element={<Auth key="register" register />} />
            <Route path="forgot-password" element={<PasswordRecovery />} />
            <Route path="reset-password" element={<PasswordRecovery reset />} />
            <Route path="account" element={<Account />} />
            <Route path="about" element={<About />} />
            <Route path="information/:slug" element={<Information />} />
            <Route path="management" element={<StaffRoute><Management /></StaffRoute>} />
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
      </CatalogProvider>
    </StoreProvider>
  );
}
