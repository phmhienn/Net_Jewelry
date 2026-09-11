import { useEffect, useState } from "react";
import { managementService, type Dashboard } from "../../services/managementService";
import { money, errorMessage } from "../../utils/format";
import { Input } from "../common/Input";
import { Button } from "../common/Button";
import { ErrorState } from "../common/Feedback";

export function ManagerStatistics() {
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [version, setVersion] = useState(0);
  const [data, setData] = useState<Dashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => {
    let active = true;
    setLoading(true); setError("");
    managementService.dashboard({ from: from || undefined, to: to || undefined })
      .then((result) => { if (active) setData(result); })
      .catch((e) => { if (active) setError(errorMessage(e)); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [version]);
  if (error) return <ErrorState message={error} retry={() => setVersion((n) => n + 1)} />;
  return <div className="account-orders"><h2>Thống kê cửa hàng</h2><div className="order-management-toolbar"><Input label="Từ ngày" type="date" value={from} onChange={(e) => setFrom(e.target.value)} /><Input label="Đến ngày" type="date" value={to} onChange={(e) => setTo(e.target.value)} /><Button onClick={() => setVersion((n) => n + 1)}>Lọc</Button></div>{loading ? <p className="loading-text">Đang tải thống kê…</p> : data && <dl className="statistics-grid"><div><dt>Tổng đơn</dt><dd>{data.totalOrders}</dd></div><div><dt>Đơn hoàn thành</dt><dd>{data.completedOrders}</dd></div><div><dt>Đơn hủy</dt><dd>{data.cancelledOrders}</dd></div><div><dt>Đang giao</dt><dd>{data.shippingOrders}</dd></div><div><dt>Sản phẩm</dt><dd>{data.products}</dd></div><div><dt>Khách hàng</dt><dd>{data.customers}</dd></div><div><dt>Doanh thu</dt><dd>{money(data.revenue)}</dd></div></dl>}</div>;
}
