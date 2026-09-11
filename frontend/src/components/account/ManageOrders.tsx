import { ChevronDown, RefreshCw, Search } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { managementService, type Row } from "../../services/managementService";
import { Button } from "../common/Button";
import { Input } from "../common/Input";
import { EmptyState, ErrorState } from "../common/Feedback";
import { money, errorMessage } from "../../utils/format";

const orderStatuses = [
  "CHO_XAC_NHAN",
  "DA_XAC_NHAN",
  "DANG_XU_LY",
  "DANG_GIAO_HANG",
  "HOAN_THANH",
  "DA_HUY",
];

const statusLabels: Record<string, string> = {
  CHO_XAC_NHAN: "Chờ xác nhận",
  DA_XAC_NHAN: "Đã xác nhận",
  DANG_XU_LY: "Đang xử lý",
  DANG_GIAO_HANG: "Đang giao",
  HOAN_THANH: "Hoàn thành",
  DA_HUY: "Đã hủy",
};

const nextStatus: Record<string, string> = {
  CHO_XAC_NHAN: "DA_XAC_NHAN",
  DA_XAC_NHAN: "DANG_XU_LY",
  DANG_XU_LY: "DANG_GIAO_HANG",
  DANG_GIAO_HANG: "HOAN_THANH",
};

const nextStatusLabels: Record<string, string> = {
  DA_XAC_NHAN: "Xác nhận",
  DANG_XU_LY: "Chuyển xử lý",
  DANG_GIAO_HANG: "Chuyển giao",
  HOAN_THANH: "Hoàn thành",
};

const value = (row: Row | undefined, key: string) => String(row?.[key] ?? "");
const numeric = (row: Row | undefined, key: string) => Number(row?.[key] ?? 0);
const formatDate = (raw: string) => raw ? new Date(raw).toLocaleDateString("vi-VN") : "—";

export function ManageOrders() {
  const [rows, setRows] = useState<Row[]>([]);
  const [status, setStatus] = useState("");
  const [keyword, setKeyword] = useState("");
  const [date, setDate] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [version, setVersion] = useState(0);

  const params = useMemo(
    () => ({
      page: 0,
      size: 100,
      status: status || undefined,
      keyword: keyword.trim() || undefined,
      date: date || undefined,
    }),
    [status, keyword, date],
  );

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    managementService
      .page<Row>("/admin/orders", params)
      .then((data) => {
        if (active) setRows(data.content);
      })
      .catch((e) => {
        if (active) setError(errorMessage(e));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [params, version]);

  const refresh = () => setVersion((n) => n + 1);
  const hasFilter = !!status || !!keyword.trim() || !!date;
  const resetFilters = () => {
    setStatus("");
    setKeyword("");
    setDate("");
  };

  if (error) return <ErrorState message={error} retry={refresh} />;

  return (
    <div className="account-orders manage-orders-page">
      <div className="manage-orders-header">
        <div>
          <h2>Quản lý đơn hàng</h2>
          <p>Theo dõi, tìm kiếm và cập nhật trạng thái đơn hàng.</p>
        </div>
        <Button variant="secondary" type="button" onClick={refresh}>
          <RefreshCw size={16} />
          Làm mới
        </Button>
      </div>

      <div className="order-management-toolbar">
        <label className="field">
          Trạng thái
          <select value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="">Tất cả trạng thái</option>
            {orderStatuses.map((item) => (
              <option key={item} value={item}>{statusLabels[item]}</option>
            ))}
          </select>
        </label>
        <Input
          label="Tìm kiếm"
          placeholder="Mã đơn hàng"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <Input label="Ngày đặt" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        <div className="order-toolbar-actions">
          {hasFilter && <Button variant="ghost" type="button" onClick={resetFilters}>Xóa lọc</Button>}
        </div>
      </div>

      <div className="order-result-line">
        <Search size={16} />
        {loading ? "Đang tải đơn hàng…" : `${rows.length} đơn hàng phù hợp`}
      </div>

      {loading ? (
        <p className="loading-text">Đang tải đơn hàng…</p>
      ) : !rows.length ? (
        <EmptyState title="Chưa có đơn hàng" description="Đơn hàng sẽ hiển thị sau khi khách đặt mua hoặc khi bộ lọc có kết quả." />
      ) : (
        <div className="managed-order-list">
          {rows.map((row) => (
            <OrderCard key={value(row, "id")} row={row} onChanged={refresh} />
          ))}
        </div>
      )}
    </div>
  );
}

function OrderCard({ row, onChanged }: { row: Row; onChanged: () => void }) {
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState("");
  const status = value(row, "status");
  const next = nextStatus[status];

  const changeStatus = async (nextValue: string) => {
    setBusy(true);
    setActionError("");
    try {
      await managementService.patch(`/admin/orders/${value(row, "id")}/status`, { status: nextValue });
      onChanged();
    } catch (err) {
      setActionError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <details className="managed-order-card">
      <summary>
        <div className="order-summary-main">
          <span className="order-code">{value(row, "code") || `#${value(row, "id")}`}</span>
          <span className="order-date">{formatDate(value(row, "date"))}</span>
        </div>
        <div>
          <span className="order-summary-label">Tổng tiền</span>
          <strong>{money(numeric(row, "total"))}</strong>
        </div>
        <span className={`status-badge status-${status.toLowerCase()}`}>{statusLabels[status] ?? status}</span>
        <div className="order-card-actions" onClick={(event) => event.preventDefault()}>
          {next && (
            <Button type="button" loading={busy} onClick={() => void changeStatus(next)}>
              {nextStatusLabels[next] ?? "Chuyển trạng thái"}
            </Button>
          )}
          {status === "CHO_XAC_NHAN" && (
            <Button type="button" variant="secondary" disabled={busy} onClick={() => void changeStatus("DA_HUY")}>
              Hủy đơn
            </Button>
          )}
        </div>
        <ChevronDown className="order-expand-icon" size={18} aria-hidden="true" />
      </summary>
      {actionError && <p className="error-banner">{actionError}</p>}
      <OrderDetail id={value(row, "id")} />
    </details>
  );
}

function OrderDetail({ id }: { id: string }) {
  const [row, setRow] = useState<Row | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    setRow(null);
    setError("");
    managementService
      .get<Row>(`/admin/orders/${id}`)
      .then((data) => {
        if (active) setRow(data);
      })
      .catch((e) => {
        if (active) setError(errorMessage(e));
      });
    return () => {
      active = false;
    };
  }, [id]);

  if (error) return <p className="error-banner">{error}</p>;
  if (!row) return <p className="loading-text">Đang tải chi tiết…</p>;

  const items = Array.isArray(row.items) ? row.items as Row[] : [];
  const address = row.address as Row | undefined;
  const payment = row.payment as Row | undefined;
  const addressText = address
    ? [address.street, address.ward, address.district, address.city].filter(Boolean).join(", ")
    : "—";

  return (
    <div className="managed-order-detail">
      <div className="order-detail-grid">
        <InfoBlock label="Khách hàng" value={value(row, "customerName") || `#${value(row, "customerId")}`} />
        <InfoBlock label="Thanh toán" value={value(payment, "method") || "COD"} />
        <InfoBlock label="Ghi chú" value={value(row, "note") || "—"} />
        <InfoBlock label="Địa chỉ giao hàng" value={addressText} />
      </div>

      <div className="order-products-table">
        <div className="order-products-head">
          <span>Sản phẩm</span>
          <span>SL</span>
          <span>Đơn giá</span>
          <span>Thành tiền</span>
        </div>
        {items.map((item) => (
          <div className="order-products-row" key={value(item, "id") || value(item, "variantId")}>
            <div>
              <strong>{value(item, "productName")}</strong>
              <small>{[value(item, "sku"), value(item, "size"), value(item, "color")].filter(Boolean).join(" · ")}</small>
            </div>
            <span>{value(item, "quantity")}</span>
            <span>{money(numeric(item, "unitPrice"))}</span>
            <strong>{money(numeric(item, "total"))}</strong>
          </div>
        ))}
      </div>
    </div>
  );
}

function InfoBlock({ label, value }: { label: string; value: string }) {
  return (
    <div className="order-info-block">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
