import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { photos } from "../data/products";
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
            NÉT bắt đầu từ một ý tưởng giản đơn: trang sức đẹp nhất khi khiến
            bạn cảm thấy thoải mái là chính mình.
          </p>
          <p>
            Một đường cong thanh mảnh, một điểm sáng nhỏ, một món đồ có thể đeo
            từ sáng đến tối. Chúng tôi hướng đến những thiết kế vừa đủ để đồng
            hành cùng bạn mỗi ngày.
          </p>
          <Link to="/products" className="btn btn-primary">
            Khám phá thiết kế <ArrowRight size={17} />
          </Link>
        </div>
        <img
          src={photos.necklace}
          alt="Chi tiết dây chuyền trong bộ sưu tập NÉT"
          width="700"
          height="800"
        />
      </section>
      <div className="about-details">
        <section id="shipping">
          <h2>Giao hàng & đổi trả</h2>
          <p>
            Phí giao hàng minh họa là 30.000₫, miễn phí từ 1.500.000₫. Thời gian
            giao dự kiến 2–5 ngày làm việc. Khi triển khai cửa hàng chính thức,
            chính sách và thời gian sẽ được xác nhận theo từng đơn hàng.
          </p>
        </section>
        <section id="care">
          <h2>Chăm sóc những điều bạn yêu</h2>
          <p>
            Tháo trang sức khi vận động mạnh hoặc tiếp xúc hóa chất. Dùng khăn
            mềm để lau nhẹ sau khi đeo và bảo quản từng món trong túi hoặc hộp
            riêng. Tránh xịt nước hoa trực tiếp lên bề mặt trang sức.
          </p>
        </section>
      </div>
    </div>
  );
}
