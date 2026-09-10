import { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import {
  Search,
  Heart,
  ShoppingBag,
  UserRound,
  Menu,
  ArrowRight,
} from "lucide-react";
import { Modal } from "../common/Modal";
import { useStore } from "../../context/StoreContext";
import { totalQuantity } from "../../utils/format";
import { categories, brand } from "../../data/config";
export function Header() {
  const [menu, setMenu] = useState(false);
  const [search, setSearch] = useState(false);
  const [query, setQuery] = useState("");
  const { cart, user, setCartOpen, wishlist } = useStore();
  const navigate = useNavigate();
  const links = (
    <>
      <NavLink to="/" end onClick={() => setMenu(false)}>
        Trang chủ
      </NavLink>
      <NavLink to="/products" onClick={() => setMenu(false)}>
        Sản phẩm
      </NavLink>
      <div className="nav-dropdown">
        <button>
          Danh mục <span className="chevron">⌄</span>
        </button>
        <div className="nav-dropdown-panel">
          {categories.map((category) => (
            <Link
              key={category}
              to={`/products?category=${encodeURIComponent(category)}`}
              onClick={() => setMenu(false)}
            >
              {category}
            </Link>
          ))}
        </div>
      </div>
      <NavLink to="/about" onClick={() => setMenu(false)}>
        Giới thiệu
      </NavLink>
    </>
  );
  return (
    <>
      <div className="announcement">
        Một chút tinh tế, cho mỗi ngày của bạn.
        <span>
          Miễn phí vận chuyển cho đơn từ 1.500.000₫ <ArrowRight size={14} />
        </span>
      </div>
      <header className="header">
        <div className="container header-inner">
          <Link className="brand" to="/" aria-label="NÉT Jewelry — Trang chủ">
            {brand.name}
            <span>{brand.descriptor}</span>
          </Link>
          <nav className="desktop-nav" aria-label="Điều hướng chính">
            {links}
          </nav>
          <div className="header-actions">
            <button
              className="icon-btn"
              aria-label="Tìm kiếm"
              onClick={() => setSearch(true)}
            >
              <Search size={21} />
            </button>
            <Link
              className="icon-btn desktop-action"
              to="/wishlist"
              aria-label={`Yêu thích (${wishlist.length})`}
            >
              <Heart size={21} />
            </Link>
            <button
              className="icon-btn cart-button"
              aria-label={`Giỏ hàng (${totalQuantity(cart)})`}
              onClick={() => setCartOpen(true)}
            >
              <ShoppingBag size={21} />
              <span className="count">{totalQuantity(cart)}</span>
            </button>
            <Link
              to={user ? "/account" : "/login"}
              className="icon-btn desktop-action"
              aria-label="Tài khoản"
            >
              <UserRound size={21} />
            </Link>
            <button
              className="icon-btn mobile-action"
              aria-label="Mở menu"
              onClick={() => setMenu(true)}
            >
              <Menu size={23} />
            </button>
          </div>
        </div>
      </header>
      <Modal
        open={menu}
        onClose={() => setMenu(false)}
        title="Khám phá NÉT"
        drawer
      >
        <nav className="mobile-nav" aria-label="Menu di động">
          {links}
          <Link to="/wishlist" onClick={() => setMenu(false)}>
            Yêu thích ({wishlist.length})
          </Link>
          <Link
            to={user ? "/account" : "/login"}
            onClick={() => setMenu(false)}
          >
            Tài khoản
          </Link>
        </nav>
      </Modal>
      <Modal
        open={search}
        onClose={() => setSearch(false)}
        title="Bạn đang tìm điều gì?"
      >
        <form
          className="search-form"
          onSubmit={(event) => {
            event.preventDefault();
            navigate(`/products?search=${encodeURIComponent(query.trim())}`);
            setSearch(false);
          }}
        >
          <label className="sr-only" htmlFor="site-search">
            Tên sản phẩm hoặc danh mục
          </label>
          <input
            id="site-search"
            autoFocus
            placeholder="Nhẫn, dây chuyền, bông tai…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <button className="btn btn-primary" aria-label="Tìm sản phẩm">
            <Search size={20} />
          </button>
        </form>
        <p className="muted mt-4">Khám phá theo danh mục</p>
        <div className="chip-row">
          {categories.map((category) => (
            <Link
              className="chip"
              key={category}
              to={`/products?category=${encodeURIComponent(category)}`}
              onClick={() => setSearch(false)}
            >
              {category}
            </Link>
          ))}
        </div>
      </Modal>
    </>
  );
}
