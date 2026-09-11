import { Link } from "react-router-dom";
import { ArrowRight, Gem, Truck, ShieldCheck } from "lucide-react";
import { Breadcrumbs } from "../components/common/Breadcrumbs";

export default function About() {
  return (
    <div className="container page">
      <Breadcrumbs items={[{ label: "Câu chuyện NÉT" }]} />
      <section className="about-intro">
        <div>
          <div className="eyebrow">CÂU CHUYỆN CỦA CHÚNG TÔI</div>
          <h1>
            Giản đơn trong thiết kế.
            <br />
            Riêng biệt ở bạn.
          </h1>
          <p>
            NÉT Jewelry xây dựng trải nghiệm mua sắm trang sức rõ ràng, tinh tế
            và dễ sử dụng. Dữ liệu sản phẩm, danh mục và nội dung được lấy từ hệ
            thống quản trị của cửa hàng.
          </p>
          <p>
            Chúng tôi ưu tiên thông tin minh bạch: chất liệu, biến thể, tồn kho,
            giá và trạng thái đơn hàng đều được đồng bộ qua backend.
          </p>
          <Link to="/products" className="btn btn-primary">
            Khám phá sản phẩm <ArrowRight size={17} />
          </Link>
        </div>
        <div className="about-visual" aria-label="Minh họa nhận diện thương hiệu NÉT Jewelry">
          <Gem size={64} />
          <span>NÉT Jewelry</span>
        </div>
      </section>
      <div className="about-details">
        <section id="shipping">
          <Truck size={22} />
          <h2>Giao hàng & đổi trả</h2>
          <p>
            Chính sách vận chuyển và đổi trả được áp dụng theo cấu hình của hệ
            thống tại thời điểm đặt hàng. Khách hàng có thể theo dõi trạng thái
            đơn trong tài khoản sau khi đặt hàng thành công.
          </p>
        </section>
        <section id="care">
          <ShieldCheck size={22} />
          <h2>Chăm sóc những điều bạn yêu</h2>
          <p>
            Tháo trang sức khi vận động mạnh hoặc tiếp xúc hóa chất. Dùng khăn
            mềm để lau nhẹ sau khi đeo và bảo quản từng món trong túi hoặc hộp
            riêng.
          </p>
        </section>
      </div>
    </div>
  );
}
