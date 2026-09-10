import { useEffect } from "react";
import { Outlet, useLocation } from "react-router-dom";
import { Check, AlertCircle } from "lucide-react";
import { Header } from "./Header";
import { Footer } from "./Footer";
import { useStore } from "../../context/StoreContext";
import { CartDrawer } from "../cart/CartDrawer";
import { useWebTools } from "../../hooks/useWebTools";
export function StoreLayout() {
  const { pathname, hash } = useLocation();
  const { notice } = useStore();
  useWebTools();
  useEffect(() => {
    if (hash) {
      requestAnimationFrame(() =>
        document
          .getElementById(decodeURIComponent(hash.slice(1)))
          ?.scrollIntoView(),
      );
    } else window.scrollTo(0, 0);
    const titles: Record<string, string> = {
      "/": "Đẹp từ những điều giản đơn",
      "/products": "Tất cả trang sức",
      "/cart": "Giỏ hàng",
      "/checkout": "Thanh toán",
      "/account": "Tài khoản",
      "/wishlist": "Yêu thích",
      "/login": "Đăng nhập",
      "/register": "Đăng ký",
      "/about": "Câu chuyện NÉT",
    };
    document.title = `${titles[pathname] ?? "Trang sức"} | NÉT Jewelry`;
  }, [pathname, hash]);
  return (
    <>
      <a className="skip-link" href="#main">
        Đến nội dung chính
      </a>
      <Header />
      <main id="main" tabIndex={-1}>
        <Outlet />
      </main>
      <Footer />
      {notice && (
        <div
          className={`toast ${notice.error ? "toast-error" : ""}`}
          role={notice.error ? "alert" : "status"}
        >
          {notice.error ? <AlertCircle size={19} /> : <Check size={19} />}
          <span>{notice.text}</span>
        </div>
      )}
      <CartDrawer />
    </>
  );
}
