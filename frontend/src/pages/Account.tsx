import { useState } from "react";
import { Link, Navigate, useLocation, useSearchParams } from "react-router-dom";
import { UserRound, Package, MapPin, LogOut, ClipboardList, ChartNoAxesColumn, Heart } from "lucide-react";
import { useStore } from "../context/StoreContext";
import { authService } from "../services/authService";
import { Breadcrumbs } from "../components/common/Breadcrumbs";
import { ProfileForm } from "../components/account/ProfileForm";
import { Orders } from "../components/account/Orders";
import { AddressForm } from "../components/account/AddressForm";
import { errorMessage } from "../utils/format";
import { accountTabs, isCustomer, roleLabels } from "../utils/access";
import { ManageOrders } from "../components/account/ManageOrders";
import { ManagerStatistics } from "../components/account/ManagerStatistics";

export default function Account() {
  const { user, authLoading, setUser, notify, clearCart } = useStore();
  const location = useLocation();
  const [params, setParams] = useSearchParams();
  const [busy, setBusy] = useState(false);
  if (authLoading) return <div className="container page" role="status">Đang tải tài khoản…</div>;
  if (!user) return <Navigate to="/login" replace state={{ from: `${location.pathname}${location.search}` }} />;
  const allowedTabs = accountTabs(user.role);
  const requestedTab = params.get("tab") ?? "profile";
  const tab = allowedTabs.includes(requestedTab) ? requestedTab : "profile";
  const links = [
    { key: "profile", label: "Thông tin cá nhân", icon: UserRound },
    { key: "orders", label: "Đơn hàng của tôi", icon: Package },
    { key: "addresses", label: "Địa chỉ giao hàng", icon: MapPin },
    { key: "manage-orders", label: "Xử lý đơn hàng", icon: ClipboardList },
    { key: "statistics", label: "Thống kê cửa hàng", icon: ChartNoAxesColumn },
  ].filter((item) => allowedTabs.includes(item.key));
  return (
    <div className="container page">
      <Breadcrumbs items={[{ label: "Tài khoản" }]} />
      <div className="account-page-header">
        <div><h1 className="page-title">Xin chào, {user.name}</h1><p className="page-intro">{roleLabels[user.role]}</p></div>
        {!isCustomer(user) && <Link className="btn btn-primary account-management-link" to="/management">Mở khu vực vận hành</Link>}
      </div>
      <div className="account-layout">
        <nav className="account-nav" aria-label="Quản lý tài khoản">
          {links.map((item) => <button key={item.key} className={tab === item.key ? "active" : ""} aria-current={tab === item.key ? "page" : undefined} onClick={() => setParams({ tab: item.key })}><item.icon size={18} />{item.label}</button>)}
          {isCustomer(user) && <Link to="/wishlist" className="account-nav-link"><Heart size={18} strokeWidth={2} aria-hidden="true" />Sản phẩm yêu thích</Link>}
          <button disabled={busy} onClick={async () => { setBusy(true); try { await authService.logout(); setUser(null); await clearCart(); notify("Đã đăng xuất"); } catch (e) { notify(errorMessage(e), true); } finally { setBusy(false); } }}><LogOut size={18} />{busy ? "Đang đăng xuất…" : "Đăng xuất"}</button>
        </nav>
        <div className="account-content">
          {tab === "manage-orders" ? <ManageOrders key={user.id} /> : tab === "statistics" ? <ManagerStatistics key={user.id} /> : tab === "orders" ? <Orders /> : tab === "addresses" ? <AddressForm /> : <ProfileForm key={user.id} />}
        </div>
      </div>
    </div>
  );
}
