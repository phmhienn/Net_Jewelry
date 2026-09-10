import { useState } from "react";
import { Link, Navigate, useSearchParams } from "react-router-dom";
import { UserRound, Package, MapPin, Heart, LogOut } from "lucide-react";
import { useStore } from "../context/StoreContext";
import { authService } from "../services/authService";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { ProfileForm } from "../components/account/ProfileForm";
import { Orders } from "../components/account/Orders";
import { AddressForm } from "../components/account/AddressForm";
import { errorMessage } from "../utils/format";
export default function Account() {
  const { user, authLoading, setUser, notify, clearCart } = useStore();
  const [params, setParams] = useSearchParams();
  const tab = params.get("tab") ?? "profile";
  const [busy, setBusy] = useState(false);
  if (authLoading)
    return (
      <div className="container page" role="status">
        Đang tải tài khoản…
      </div>
    );
  if (!user) return <Navigate to="/login" replace />;
  const links = [
    { key: "profile", label: "Thông tin cá nhân", icon: UserRound },
    { key: "orders", label: "Đơn hàng của tôi", icon: Package },
    { key: "addresses", label: "Địa chỉ giao hàng", icon: MapPin },
  ];
  return (
    <div className="container page">
      <Breadcrumbs items={[{ label: "Tài khoản" }]} />
      <h1 className="page-title">Xin chào, {user.name}</h1>
      <p className="page-intro">
        Một không gian nhỏ cho những điều thuộc về bạn.
      </p>
      <div className="account-layout">
        <nav className="account-nav" aria-label="Quản lý tài khoản">
          {links.map((item) => (
            <button
              key={item.key}
              className={tab === item.key ? "active" : ""}
              aria-current={tab === item.key ? "page" : undefined}
              onClick={() => setParams({ tab: item.key })}
            >
              <item.icon size={18} />
              {item.label}
            </button>
          ))}
          <Link to="/wishlist">
            <Heart size={18} />
            Sản phẩm yêu thích
          </Link>
          <button
            disabled={busy}
            onClick={async () => {
              setBusy(true);
              try {
                await authService.logout();
                setUser(null);
                clearCart();
                notify("Đã đăng xuất");
              } catch (e) {
                notify(errorMessage(e), true);
              } finally {
                setBusy(false);
              }
            }}
          >
            <LogOut size={18} />
            {busy ? "Đang đăng xuất…" : "Đăng xuất"}
          </button>
        </nav>
        <div className="account-content">
          {tab === "orders" ? (
            <Orders />
          ) : tab === "addresses" ? (
            <AddressForm />
          ) : (
            <ProfileForm />
          )}
        </div>
      </div>
    </div>
  );
}
