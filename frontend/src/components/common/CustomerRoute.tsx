import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useStore } from "../../context/StoreContext";
import { isCustomer } from "../../utils/access";

export function CustomerRoute({ children }: { children: ReactNode }) {
  const { user, authLoading } = useStore();
  const location = useLocation();
  const from = `${location.pathname}${location.search}`;

  if (authLoading) return <div className="container page">Đang tải tài khoản…</div>;
  if (!user) return <Navigate to="/login" replace state={{ from }} />;
  if (!isCustomer(user)) return <Navigate to="/account" replace />;
  return children;
}
