import { Link } from "react-router-dom";
import { ArrowRight, ArrowUpRight, Truck, ShieldCheck, Gift, Sparkles } from "lucide-react";
import { useProducts } from "../hooks/useProducts";
import { useCatalog } from "../context/CatalogContext";
import { ProductGrid } from "../components/product/ProductGrid";
import { Loading, ErrorState, EmptyState } from "../components/common/Feedback";
import { money } from "../utils/format";

export default function Home() {
  const { items, loading, error, retry } = useProducts({ size: 8 });
  const { categories, settings } = useCatalog();
  const heroProduct = items.find((item) => item.image) ?? items[0];
  return (
    <>
      <section className="hero container">
        <div className="hero-copy">
          <div className="eyebrow"><span /> TINH TẾ TRONG TỪNG CHI TIẾT</div>
          <h1>Trang sức tinh tế<br />cho <span>phong cách của bạn.</span></h1>
          <p>Những thiết kế rõ ràng, dễ chọn và phù hợp cho nhiều khoảnh khắc hằng ngày.</p>
          <Link className="btn btn-primary" to="/products">Mua sắm ngay <ArrowRight size={18} /></Link>
          <div className="hero-note"><span className="note-line" /> Cam kết chất liệu rõ ràng, thiết kế tinh tế.</div>
        </div>
        <div className="hero-visual">
          {heroProduct?.image ? (
            <img src={heroProduct.image} alt={heroProduct.name} fetchPriority="high" width="900" height="1000" />
          ) : (
            <div className="hero-placeholder">Chưa có sản phẩm</div>
          )}
          {heroProduct && (
            <div className="hero-image-label">
              <span>{heroProduct.name}</span>
              <Link to={`/products/${heroProduct.id}`} aria-label={`Xem ${heroProduct.name}`}><ArrowUpRight size={25} /></Link>
            </div>
          )}
        </div>
        <div className="hero-index">
          <span>01 — 04</span>
          <div><i /><i /><i /><i /></div>
          <span>Ít hơn, nhưng rõ ràng hơn.</span>
        </div>
      </section>
      <section className="benefits container" aria-label="Dịch vụ của NÉT">
        <div><ShieldCheck /><p>Chất liệu rõ ràng<span>Thông tin sản phẩm minh bạch</span></p></div>
        <div><Truck /><p>Giao hàng toàn quốc<span>Miễn phí từ {money(settings.freeShippingThreshold)}</span></p></div>
        <div><Gift /><p>Đóng gói cẩn thận<span>Phù hợp để làm quà tặng</span></p></div>
        <div><Sparkles /><p>Hỗ trợ tận tâm<span>Đồng hành sau khi mua</span></p></div>
      </section>
      <section className="section container" id="categories">
        <div className="section-heading">
          <div><div className="eyebrow">DANH MỤC</div><h2>Tìm theo nhu cầu</h2></div>
          <p>Khám phá các dòng sản phẩm phù hợp với phong cách của bạn.</p>
        </div>
        {categories.length ? (
          <div className="category-grid">
            {categories.slice(0, 4).map((category) => (
              <Link key={category.id} className="category-card category-card-empty" to={`/products?categoryId=${encodeURIComponent(category.id)}`}>
                <div><span>{category.name}</span></div>
                <span>{category.name}<ArrowUpRight size={20} /></span>
              </Link>
            ))}
          </div>
        ) : (
          <EmptyState title="Chưa có danh mục" description="Danh mục sẽ hiển thị sau khi được thêm trong khu vực vận hành." />
        )}
      </section>
      <section className="section featured container">
        <div className="section-heading">
          <div><div className="eyebrow">SẢN PHẨM</div><h2>Sản phẩm nổi bật</h2></div>
          <Link className="text-link" to="/products">Xem tất cả sản phẩm <ArrowRight size={17} /></Link>
        </div>
        {loading ? <Loading /> : error ? <ErrorState message={error} retry={retry} /> : !items.length ? (
          <EmptyState title="Chưa có sản phẩm" description="Bộ sưu tập đang được cập nhật. Hãy quay lại sớm nhé!" />
        ) : <ProductGrid products={items.slice(0, 4)} />}
      </section>
      <section className="collection-cta container">
        <div>
          <div className="eyebrow">BỘ SƯU TẬP</div>
          <h2>Chọn món trang sức phù hợp cho bạn.</h2>
          <p>Xem danh sách sản phẩm, lọc theo danh mục, thương hiệu, chất liệu và khoảng giá.</p>
          <Link to="/products" className="btn btn-primary">Xem sản phẩm <ArrowRight size={17} /></Link>
        </div>
        <div className="cta-image cta-placeholder">Bộ sưu tập mới nhất</div>
      </section>
    </>
  );
}
