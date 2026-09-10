import { Link } from "react-router-dom";
import { ArrowUpRight } from "lucide-react";
import { brand, isDemo } from "../../data/config";
export function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-grid">
        <div>
          <Link className="brand" to="/">
            {brand.name}
            <span>{brand.descriptor}</span>
          </Link>
          <p>
            Trang sức giản đơn.
            <br />
            Dấu ấn của riêng bạn.
          </p>
        </div>
        <div>
          <h3>Khám phá</h3>
          <Link to="/products">Tất cả sản phẩm</Link>
          <Link to="/products?sort=newest">Thiết kế mới</Link>
          <Link to="/about">Câu chuyện NÉT</Link>
        </div>
        <div>
          <h3>Chăm sóc khách hàng</h3>
          <Link to="/about#shipping">Giao hàng & đổi trả</Link>
          <Link to="/about#care">Hướng dẫn bảo quản</Link>
          <Link to="/account">Tài khoản & đơn hàng</Link>
        </div>
        <div>
          <h3>Kết nối với NÉT</h3>
          <p>
            Cần một chút tư vấn?
            <br />
            Chúng tôi luôn sẵn lòng lắng nghe.
          </p>
          <a href={`mailto:${brand.email}`} className="footer-contact">
            Liên hệ qua email <ArrowUpRight size={16} />
          </a>
        </div>
      </div>
      <div className="container footer-bottom">
        <span>© {new Date().getFullYear()} NÉT Jewelry.</span>
        <span>
          {isDemo
            ? "Bản trải nghiệm · Sản phẩm và giao dịch minh họa"
            : "Đẹp từ những điều giản đơn."}
        </span>
        <span>Tiếng Việt · VND</span>
      </div>
    </footer>
  );
}
