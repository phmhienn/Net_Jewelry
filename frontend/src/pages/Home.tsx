import { Link } from "react-router-dom";
import {
  ArrowRight,
  ArrowUpRight,
  Truck,
  ShieldCheck,
  Gift,
  Sparkles,
} from "lucide-react";
import { photos } from "../data/products";
import { categories } from "../data/config";
import { useProducts } from "../hooks/useProducts";
import { ProductGrid } from "../components/product/ProductGrid";
import { Loading, ErrorState, EmptyState } from "../components/common/Feedback";
export default function Home() {
  const { items, loading, error, retry } = useProducts({});
  const categoryPhotos = [
    photos.ring,
    photos.necklace,
    photos.bracelet,
    photos.earrings,
  ];
  return (
    <>
      <section className="hero container">
        <div className="hero-copy">
          <div className="eyebrow">
            <span /> TINH TẾ TRONG TỪNG CHI TIẾT
          </div>
          <h1>
            Đẹp từ những
            <br />
            điều <span>giản đơn.</span>
          </h1>
          <p>
            Không cần quá nhiều để trở nên đặc biệt.
            <br className="desktop-break" /> Một món trang sức vừa đủ, một dấu
            ấn rất riêng.
          </p>
          <Link className="btn btn-primary" to="/products">
            Khám phá trang sức <ArrowRight size={18} />
          </Link>
          <div className="hero-note">
            <span className="note-line" /> Thiết kế tinh giản. Đồng hành mỗi
            ngày.
          </div>
        </div>
        <div className="hero-visual">
          <img
            src={photos.hero}
            alt="Dây chuyền vàng thanh mảnh trên nền vải sáng"
            fetchPriority="high"
            width="900"
            height="1000"
          />
          <div className="hero-image-label">
            <span>THE EVERYDAY COLLECTION</span>
            <Link
              to="/products?category=Vòng cổ"
              aria-label="Khám phá bộ sưu tập dây chuyền"
            >
              <ArrowUpRight size={25} />
            </Link>
          </div>
        </div>
        <div className="hero-index">
          <span>01 — 04</span>
          <div>
            <i />
            <i />
            <i />
            <i />
          </div>
          <span>Ít hơn, nhưng ý nghĩa hơn.</span>
        </div>
      </section>
      <section className="benefits container" aria-label="Dịch vụ của NÉT">
        <div>
          <ShieldCheck />
          <p>
            Chất liệu chọn lọc<span>Tỉ mỉ trong từng chi tiết</span>
          </p>
        </div>
        <div>
          <Truck />
          <p>
            Giao hàng toàn quốc<span>Miễn phí cho đơn từ 1.500.000₫</span>
          </p>
        </div>
        <div>
          <Gift />
          <p>
            Gói trọn yêu thương<span>Hộp quà trong mỗi đơn hàng</span>
          </p>
        </div>
        <div>
          <Sparkles />
          <p>
            Đồng hành dài lâu<span>Hỗ trợ chăm sóc trang sức</span>
          </p>
        </div>
      </section>
      <section className="section container" id="categories">
        <div className="section-heading">
          <div>
            <div className="eyebrow">DẤU ẤN RIÊNG CỦA BẠN</div>
            <h2>Tìm điều bạn yêu</h2>
          </div>
          <p>Những thiết kế nhỏ. Những cảm xúc rất riêng.</p>
        </div>
        <div className="category-grid">
          {categories.map((category, i) => (
            <Link
              key={category}
              className="category-card"
              to={`/products?category=${encodeURIComponent(category)}`}
            >
              <div>
                <img
                  src={categoryPhotos[i]}
                  alt={`Trang sức ${category.toLowerCase()}`}
                  loading="lazy"
                  width="500"
                  height="420"
                />
              </div>
              <span>
                {category}
                <ArrowUpRight size={20} />
              </span>
            </Link>
          ))}
        </div>
      </section>
      <section className="section featured container">
        <div className="section-heading">
          <div>
            <div className="eyebrow">ĐƯỢC YÊU THÍCH TẠI NÉT</div>
            <h2>Một chút tinh tế, mỗi ngày</h2>
          </div>
          <Link className="text-link" to="/products">
            Xem tất cả sản phẩm <ArrowRight size={17} />
          </Link>
        </div>
        {loading ? (
          <Loading />
        ) : error ? (
          <ErrorState message={error} retry={retry} />
        ) : !items.length ? (
          <EmptyState
            title="Sản phẩm đang được cập nhật"
            description="Mời bạn ghé lại để khám phá những thiết kế mới."
          />
        ) : (
          <ProductGrid products={items.slice(0, 4)} />
        )}
      </section>
      <section className="collection-cta container">
        <div>
          <div className="eyebrow">DÀNH CHO BẠN. HOẶC NGƯỜI BẠN THƯƠNG.</div>
          <h2>
            Một món quà nhỏ.
            <br />
            Một lời thương thật lớn.
          </h2>
          <p>Đôi khi, điều ý nghĩa nhất nằm trong những điều giản đơn.</p>
          <Link to="/products" className="btn btn-primary">
            Chọn một món quà <ArrowRight size={17} />
          </Link>
        </div>
        <div className="cta-image">
          <img
            src={photos.bracelet}
            alt="Vòng tay vàng dành tặng người thương"
            loading="lazy"
            width="700"
            height="500"
          />
        </div>
      </section>
    </>
  );
}
