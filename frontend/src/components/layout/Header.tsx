import { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import {
  Search,
  Heart,
  ShoppingBag,
  UserRound,
  Menu,
  ArrowRight,
  ChevronDown,
} from "lucide-react";
import { Modal } from "../common/Modal";
import { useStore } from "../../context/StoreContext";
import { useCatalog } from "../../context/CatalogContext";
import { totalQuantity } from "../../utils/format";
import { isCustomer } from "../../utils/access";

export function Header() {
  const [menu, setMenu] = useState(false);
  const [search, setSearch] = useState(false);
  const [categoriesOpen, setCategoriesOpen] = useState(false);
  const [query, setQuery] = useState("");
  const { cart, user, setCartOpen } = useStore();
  const { categories, settings } = useCatalog();
  const navigate = useNavigate();
  const customer = isCustomer(user);

  const closeNavigation = () => {
    setMenu(false);
    setCategoriesOpen(false);
    if (document.activeElement instanceof HTMLElement) document.activeElement.blur();
  };

  const links = (
    <>
      <NavLink to="/" end onClick={closeNavigation}>
        Trang chủ
      </NavLink>
      <NavLink to="/products" onClick={closeNavigation}>
        Sản phẩm
      </NavLink>
      <div
        className={`nav-dropdown ${categoriesOpen ? "open" : ""}`}
        onMouseLeave={() => setCategoriesOpen(false)}
      >
        <button
          type="button"
          className="nav-category-trigger"
          aria-expanded={categoriesOpen}
          onClick={() => setCategoriesOpen((open) => !open)}
          onKeyDown={(event) => {
            if (event.key === "Escape") setCategoriesOpen(false);
          }}
        >
          <span>Danh mục</span>
          <ChevronDown size={14} strokeWidth={2.2} className="chevron" aria-hidden="true" />
        </button>
        <div className="nav-dropdown-panel">
          {categories.length ? (
            categories.map((category) => (
              <Link
                key={category.id}
                to={`/products?categoryId=${encodeURIComponent(category.id)}`}
                onClick={closeNavigation}
              >
                {category.name}
              </Link>
            ))
          ) : (
            <span>Chưa có danh mục</span>
          )}
        </div>
      </div>
      <NavLink to="/about" onClick={closeNavigation}>
        Giới thiệu
      </NavLink>
    </>
  );

  return (
    <>
      <div className="announcement">
        Một chút tinh tế, cho mỗi ngày của bạn.
        <span>
          Miễn phí vận chuyển cho đơn từ {settings.freeShippingThreshold.toLocaleString("vi-VN")}₫ <ArrowRight size={14} />
        </span>
      </div>
      <header className="header">
        <div className="container header-inner">
          <Link className="brand" to="/" aria-label="NÉT Jewelry — Trang chủ">
            {settings.brandName}
            <span>{settings.descriptor}</span>
          </Link>
          <nav className="desktop-nav" aria-label="Điều hướng chính">
            {links}
          </nav>
          <div className="header-actions">
            <button className="icon-btn" aria-label="Tìm kiếm" onClick={() => setSearch(true)}>
              <Search size={21} />
            </button>
            {customer && (
              <Link className="icon-btn desktop-action" to="/wishlist" aria-label="Yêu thích">
                <Heart size={21} />
              </Link>
            )}
            {customer && (
              <button
                className="icon-btn cart-button"
                aria-label={`Giỏ hàng (${totalQuantity(cart)})`}
                onClick={() => setCartOpen(true)}
              >
                <ShoppingBag size={21} />
                <span className="count">{totalQuantity(cart)}</span>
              </button>
            )}
            <Link to={user ? "/account" : "/login"} className="icon-btn desktop-action" aria-label="Tài khoản">
              <UserRound size={21} />
            </Link>
            <button className="icon-btn mobile-action" aria-label="Mở menu" onClick={() => setMenu(true)}>
              <Menu size={23} />
            </button>
          </div>
        </div>
      </header>
      <Modal open={menu} onClose={() => setMenu(false)} title="Khám phá NÉT" drawer>
        <nav className="mobile-nav" aria-label="Menu di động">
          {links}
          {customer && <Link to="/wishlist" onClick={closeNavigation}>Yêu thích</Link>}
          <Link to={user ? "/account" : "/login"} onClick={closeNavigation}>Tài khoản</Link>
        </nav>
      </Modal>
      <Modal open={search} onClose={() => setSearch(false)} title="Bạn đang tìm điều gì?">
        <form
          className="search-form"
          onSubmit={(event) => {
            event.preventDefault();
            navigate(`/products?search=${encodeURIComponent(query.trim())}`);
            setSearch(false);
          }}
        >
          <label className="sr-only" htmlFor="site-search">Tên sản phẩm hoặc danh mục</label>
          <input
            id="site-search"
            autoFocus
            placeholder="Nhẫn, dây chuyền, bông tai…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <button className="btn btn-primary" aria-label="Tìm sản phẩm"><Search size={20} /></button>
        </form>
        <p className="muted mt-4">Khám phá theo danh mục</p>
        <div className="chip-row">
          {categories.map((category) => (
            <Link
              className="chip"
              key={category.id}
              to={`/products?categoryId=${encodeURIComponent(category.id)}`}
              onClick={() => setSearch(false)}
            >
              {category.name}
            </Link>
          ))}
        </div>
      </Modal>
    </>
  );
}
