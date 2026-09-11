import { useEffect, useState } from "react";
import { Link, Navigate, useSearchParams } from "react-router-dom";
import {
  ArrowRight,
  BarChart3,
  Boxes,
  CheckCircle2,
  Clock3,
  ClipboardList,
  CreditCard,
  Gift,
  Image as ImageIcon,
  Layers3,
  MessageSquareText,
  PackageSearch,
  Tags,
  Users,
  WalletCards,
  XCircle,
} from "lucide-react";
import { useStore } from "../context/StoreContext";
import { isManager, roleLabels } from "../utils/access";
import { managementService, type Dashboard, type Row } from "../services/managementService";
import { Button } from "../components/common/Button";
import { Input } from "../components/common/Input";
import { Modal } from "../components/common/Modal";
import { EmptyState, ErrorState } from "../components/common/Feedback";
import { money, errorMessage } from "../utils/format";
import { ManageOrders } from "../components/account/ManageOrders";
import { useCatalog } from "../context/CatalogContext";

type Section = "dashboard" | "orders" | "products" | "inventory" | "reviews" | "payments" | "catalog" | "customers" | "staff" | "gold" | "coupons" | "content";
type ManageFormSection = "catalog" | "category" | "brand" | "coupons" | "gold" | "content" | "banner" | "page";

const value = (row: Row, key: string) => String(row[key] ?? "");
const inputValue = (row: Row, key: string) => value(row, key) === "—" ? "" : value(row, key);
const idOf = (row: Row) => String(row.id ?? "");

const optionalNumber = (raw: FormDataEntryValue | null) => {
  const text = String(raw ?? "").trim();
  return text ? Number(text) : null;
};
const optionalText = (raw: FormDataEntryValue | null) => {
  const text = String(raw ?? "").trim();
  return text || null;
};
const toInstant = (raw: FormDataEntryValue | null) => {
  const text = String(raw ?? "").trim();
  return text ? new Date(text).toISOString() : null;
};
const toDateTimeLocal = (row: Row, key: string) => {
  const raw = inputValue(row, key);
  if (!raw) return "";
  const date = new Date(raw);
  return Number.isNaN(date.getTime()) ? "" : date.toISOString().slice(0, 16);
};

function PageHeader({ title, description, action }: { title: string; description: string; action?: React.ReactNode }) {
  return <div className="management-heading"><div><h1>{title}</h1><p>{description}</p></div>{action}</div>;
}

function DataTable({ rows, columns, actions }: { rows: Row[]; columns: [string, string][]; actions?: (row: Row) => React.ReactNode }) {
  if (!rows.length) return <EmptyState title="Chưa có dữ liệu" description="Chưa có mục nào được ghi nhận." />;
  return <div className="management-table-wrap"><table className="management-table"><thead><tr>{columns.map(([key, label]) => <th key={key}>{label}</th>)}{actions && <th>Thao tác</th>}</tr></thead><tbody>{rows.map((row) => <tr key={idOf(row)}>{columns.map(([key]) => <td key={key}>{formatCell(row, key)}</td>)}{actions && <td className="management-row-actions">{actions(row)}</td>}</tr>)}</tbody></table></div>;
}

function formatCell(row: Row, key: string) {
  const raw = row[key];
  if (raw == null || raw === "") return "—";
  if (["price", "total", "amount", "revenue", "value", "minimumOrder", "maximumDiscount", "buyPrice", "sellPrice"].includes(key)) return money(Number(raw));
  if (key.toLowerCase().includes("date") || key.endsWith("At")) return String(raw).includes("T") ? new Date(String(raw)).toLocaleString("vi-VN") : String(raw);
  if (typeof raw === "object") return "—";
  return String(raw);
}

function DashboardPanel() {
  const { user } = useStore();
  const [data, setData] = useState<Dashboard | null>(null);
  const [error, setError] = useState("");
  const [version, setVersion] = useState(0);
  useEffect(() => { managementService.dashboard().then(setData).catch((e) => setError(errorMessage(e))); }, [version]);
  if (error) return <ErrorState message={error} retry={() => setVersion((n) => n + 1)} />;
  const completionRate = data?.totalOrders ? Math.round((data.completedOrders / data.totalOrders) * 100) : 0;
  const manager = isManager(user);
  const shortcuts = [
    { to: "?section=orders", icon: ClipboardList, title: "Xử lý đơn hàng", text: "Xem chi tiết, xác nhận và cập nhật trạng thái đơn." },
    { to: "?section=products", icon: PackageSearch, title: "Quản lý sản phẩm", text: "Thêm, sửa, xóa sản phẩm đang bán trên website." },
    { to: "?section=inventory", icon: Boxes, title: "Theo dõi kho", text: "Nhập, xuất và điều chỉnh tồn kho theo biến thể." },
    { to: "?section=reviews", icon: MessageSquareText, title: "Phản hồi đánh giá", text: "Trả lời đánh giá của khách hàng sau mua." },
    ...(manager ? [{ to: "?section=catalog", icon: Layers3, title: "Danh mục & thương hiệu", text: "Quản lý dữ liệu phân loại hiển thị ngoài website." }] : []),
    ...(manager ? [{ to: "?section=staff", icon: Users, title: "Nhân viên", text: "Theo dõi tài khoản nhân viên và phân quyền." }] : []),
  ];
  return <div className="dashboard-panel">
    <section className="dashboard-hero">
      <div>
        <div className="eyebrow">TỔNG QUAN CỬA HÀNG</div>
        <h1>Xin chào, {user?.name ?? "bạn"}</h1>
        <p>Không gian vận hành tập trung cho đơn hàng, sản phẩm, kho và các hoạt động của cửa hàng.</p>
      </div>
      <div className="dashboard-hero-actions">
        <Link className="btn btn-primary" to="?section=orders">Xử lý đơn <ArrowRight size={17} /></Link>
        <Link className="btn btn-secondary" to="?section=products">Sản phẩm</Link>
      </div>
    </section>

    <dl className="dashboard-kpis">
      <KpiCard icon={ClipboardList} label="Tổng đơn" value={data?.totalOrders ?? "—"} helper={`${data?.completedOrders ?? 0} đơn hoàn thành`} />
      <KpiCard icon={WalletCards} label="Doanh thu" value={data ? money(data.revenue) : "—"} helper="Tính từ đơn hoàn thành" featured />
      <KpiCard icon={PackageSearch} label="Sản phẩm" value={data?.products ?? "—"} helper="Sản phẩm trong hệ thống" />
      <KpiCard icon={Users} label="Khách hàng" value={data?.customers ?? "—"} helper="Tài khoản khách hàng" />
    </dl>

    <div className="dashboard-grid">
      <section className="dashboard-card">
        <div className="dashboard-card-heading">
          <h2>Tình trạng đơn hàng</h2>
          <span>{completionRate}% hoàn thành</span>
        </div>
        <div className="order-status-overview">
          <StatusMetric icon={CheckCircle2} label="Hoàn thành" value={data?.completedOrders ?? 0} />
          <StatusMetric icon={Clock3} label="Đang giao" value={data?.shippingOrders ?? 0} />
          <StatusMetric icon={XCircle} label="Đã hủy" value={data?.cancelledOrders ?? 0} />
        </div>
      </section>

      <section className="dashboard-card dashboard-note">
        <div className="dashboard-card-heading">
          <h2>Ghi chú vận hành</h2>
        </div>
        <p>Nếu bảng chưa có dữ liệu, các màn quản lý sẽ hiển thị trạng thái trống. Khi thêm sản phẩm, hãy chọn đúng danh mục, thương hiệu và kiểm tra tồn kho trước khi demo đặt hàng.</p>
      </section>
    </div>

    <section className="dashboard-card">
      <div className="dashboard-card-heading">
        <h2>Thao tác nhanh</h2>
        <span>{manager ? "Quản lý" : "Nhân viên"}</span>
      </div>
      <div className="dashboard-shortcuts">
        {shortcuts.map((item) => <Link key={item.to} to={item.to} className="dashboard-shortcut"><item.icon size={20} /><span><strong>{item.title}</strong><small>{item.text}</small></span><ArrowRight size={16} /></Link>)}
      </div>
    </section>
  </div>;
}

function KpiCard({ icon: Icon, label, value, helper, featured = false }: { icon: typeof Boxes; label: string; value: React.ReactNode; helper: string; featured?: boolean }) {
  return <div className={featured ? "featured" : ""}><dt><Icon size={18} />{label}</dt><dd>{value}</dd><small>{helper}</small></div>;
}

function StatusMetric({ icon: Icon, label, value }: { icon: typeof Boxes; label: string; value: number }) {
  return <div><Icon size={20} /><span>{label}</span><strong>{value}</strong></div>;
}

function ProductsAdmin() {
  const [rows, setRows] = useState<Row[]>([]), [error, setError] = useState(""), [version, setVersion] = useState(0);
  useEffect(() => { managementService.page<Row>("/admin/products", { page: 0, size: 100 }).then((d) => setRows(d.content)).catch((e) => setError(errorMessage(e))); }, [version]);
  const refresh = () => setVersion((n) => n + 1);
  return <><PageHeader title="Quản lý sản phẩm" description="Thêm, sửa, xóa sản phẩm đang bán trên website." action={<ProductEditor onSaved={refresh} />} />{error ? <ErrorState message={error} retry={refresh} /> : <DataTable rows={rows} columns={[["sku", "SKU"], ["name", "Sản phẩm"], ["category", "Danh mục"], ["brand", "Thương hiệu"], ["price", "Giá"], ["status", "Trạng thái"]]} actions={(row) => <div className="management-inline-actions"><ProductEditor row={row} onSaved={refresh} /><Button variant="ghost" onClick={async () => { if (confirm(`Xóa "${value(row, "name") || value(row, "sku")}"?`)) { await managementService.remove(`/products/${idOf(row)}`); refresh(); } }}>Xóa</Button></div>} />}</>;
}

function ProductEditor({ row, onSaved }: { row?: Row; onSaved: () => void }) {
  const [open, setOpen] = useState(false), [busy, setBusy] = useState(false), [error, setError] = useState("");
  const { categories, brands } = useCatalog();
  const editing = !!row;
  return <><Button variant={editing ? "secondary" : "primary"} onClick={() => setOpen(true)}>{editing ? "Sửa" : "Thêm sản phẩm"}</Button><Modal open={open} onClose={() => setOpen(false)} title={editing ? "Sửa sản phẩm" : "Thêm sản phẩm"}><form className="management-form" onSubmit={async (e) => { e.preventDefault(); const f = new FormData(e.currentTarget); const body = { sku: f.get("sku"), name: f.get("name"), categoryId: Number(f.get("categoryId")), brandId: Number(f.get("brandId")), description: f.get("description"), price: Number(f.get("price")), material: f.get("material"), size: f.get("size"), color: f.get("color"), weight: Number(f.get("weight") || 0), gemstone: f.get("gemstone"), status: f.get("status") || "DANG_BAN" }; setBusy(true); setError(""); try { if (editing) await managementService.update(`/products/${idOf(row!)}`, body); else await managementService.create("/products", body); setOpen(false); onSaved(); } catch (err) { setError(errorMessage(err)); } finally { setBusy(false); } }}><Input label="SKU" name="sku" defaultValue={inputValue(row ?? {}, "sku")} required /><Input label="Tên sản phẩm" name="name" defaultValue={inputValue(row ?? {}, "name")} required /><div className="form-grid"><label>Danh mục<select name="categoryId" defaultValue={inputValue(row ?? {}, "categoryId")} required><option value="">Chọn danh mục</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label><label>Thương hiệu<select name="brandId" defaultValue={inputValue(row ?? {}, "brandId")} required><option value="">Chọn thương hiệu</option>{brands.map((brand) => <option key={brand.id} value={brand.id}>{brand.name}</option>)}</select></label></div><Input label="Giá" name="price" type="number" min="1" defaultValue={inputValue(row ?? {}, "price")} required /><Input label="Chất liệu" name="material" defaultValue={inputValue(row ?? {}, "material")} required /><div className="form-grid"><Input label="Size" name="size" defaultValue={inputValue(row ?? {}, "size")} /><Input label="Màu sắc" name="color" defaultValue={inputValue(row ?? {}, "color")} /></div><div className="form-grid"><Input label="Trọng lượng" name="weight" type="number" min="0" step="0.01" defaultValue={inputValue(row ?? {}, "weight")} /><Input label="Đá quý" name="gemstone" defaultValue={inputValue(row ?? {}, "gemstone")} /></div><label>Mô tả<textarea name="description" defaultValue={inputValue(row ?? {}, "description")} /></label><StatusSelect defaultValue={inputValue(row ?? {}, "status") || "DANG_BAN"} product />{error && <p className="error-banner">{error}</p>}<Button loading={busy}>Lưu</Button></form></Modal></>;
}

function InventoryAdmin() {
  const [rows, setRows] = useState<Row[]>([]), [keyword, setKeyword] = useState(""), [lowOnly, setLowOnly] = useState(false), [error, setError] = useState(""), [version, setVersion] = useState(0);
  useEffect(() => { managementService.page<Row>("/inventory", { page: 0, size: 100, keyword: keyword || undefined, lowStock: lowOnly }).then((d) => setRows(d.content)).catch((e) => setError(errorMessage(e))); }, [keyword, lowOnly, version]);
  const refresh = () => setVersion((n) => n + 1);
  return <><PageHeader title="Quản lý kho" description="Nhập, xuất và điều chỉnh tồn kho theo từng biến thể." /><div className="management-toolbar inventory-toolbar"><Input label="Tìm sản phẩm trong kho" placeholder="Mã SKU hoặc tên sản phẩm" value={keyword} onChange={(e) => setKeyword(e.target.value)} /><label className="inventory-low-filter"><input type="checkbox" checked={lowOnly} onChange={(e) => setLowOnly(e.target.checked)} /><span>Chỉ hiển thị sắp hết hàng</span></label></div>{error ? <ErrorState message={error} retry={refresh} /> : <DataTable rows={rows} columns={[["sku", "SKU"], ["productName", "Sản phẩm"], ["quantity", "Tồn kho"], ["reserved", "Đã giữ"], ["available", "Khả dụng"]]} actions={(row) => <InventoryAction row={row} onSaved={refresh} />} />}</>;
}

function InventoryAction({ row, onSaved }: { row: Row; onSaved: () => void }) {
  const [open, setOpen] = useState(false), [busy, setBusy] = useState(false), [error, setError] = useState("");
  return <><Button variant="secondary" onClick={() => setOpen(true)}>Điều chỉnh</Button><Modal open={open} onClose={() => setOpen(false)} title="Điều chỉnh tồn kho"><form className="management-form" onSubmit={async (e) => { e.preventDefault(); const f = new FormData(e.currentTarget); const action = String(f.get("action")); setBusy(true); setError(""); try { await managementService.create(`/inventory/${action}`, { variantId: Number(value(row, "variantId")), quantity: Number(f.get("quantity")), reason: f.get("reason") }); setOpen(false); onSaved(); } catch (err) { setError(errorMessage(err)); } finally { setBusy(false); } }}><label>Thao tác<select name="action"><option value="import">Nhập kho</option><option value="export">Xuất kho</option><option value="adjust">Điều chỉnh tồn vật lý</option></select></label><Input label="Số lượng" type="number" min="0" name="quantity" required /><Input label="Lý do" name="reason" />{error && <p className="error-banner">{error}</p>}<Button loading={busy}>Lưu</Button></form></Modal></>;
}

function ReviewsAdmin() {
  const [rows, setRows] = useState<Row[]>([]), [error, setError] = useState(""), [version, setVersion] = useState(0);
  useEffect(() => { managementService.page<Row>("/admin/reviews", { page: 0, size: 100 }).then((d) => setRows(d.content)).catch((e) => setError(errorMessage(e))); }, [version]);
  return <><PageHeader title="Quản lý đánh giá" description="Phản hồi đánh giá của khách hàng." />{error ? <ErrorState message={error} retry={() => setVersion((n) => n + 1)} /> : <DataTable rows={rows} columns={[["customerName", "Khách hàng"], ["productId", "Sản phẩm"], ["stars", "Số sao"], ["content", "Nội dung"], ["status", "Trạng thái"], ["reply", "Phản hồi"]]} actions={(row) => <ReviewAction row={row} onSaved={() => setVersion((n) => n + 1)} />} />}</>;
}

function ReviewAction({ row, onSaved }: { row: Row; onSaved: () => void }) {
  const [open, setOpen] = useState(false), [reply, setReply] = useState(inputValue(row, "reply")), [busy, setBusy] = useState(false), [error, setError] = useState("");
  return <><Button variant="secondary" onClick={() => setOpen(true)}>Phản hồi</Button><Modal open={open} onClose={() => setOpen(false)} title="Phản hồi đánh giá"><div className="management-form"><p>{value(row, "content")}</p><label>Phản hồi<textarea value={reply} onChange={(e) => setReply(e.target.value)} maxLength={3000} /></label>{error && <p className="error-banner">{error}</p>}<Button loading={busy} onClick={async () => { setBusy(true); setError(""); try { await managementService.update(`/admin/reviews/${idOf(row)}/reply`, { reply }); setOpen(false); onSaved(); } catch (err) { setError(errorMessage(err)); } finally { setBusy(false); } }}>Lưu phản hồi</Button></div></Modal></>;
}

function SimplePage({ title, description, path, columns, managerOnly = false, action, section }: { title: string; description: string; path: string; columns: [string, string][]; managerOnly?: boolean; action?: (refresh: () => void) => React.ReactNode; section?: ManageFormSection }) {
  const { user } = useStore();
  const [rows, setRows] = useState<Row[]>([]), [error, setError] = useState(""), [version, setVersion] = useState(0);
  const refresh = () => setVersion((n) => n + 1);
  useEffect(() => { const loader = path === "/categories" || path === "/brands" ? managementService.list<Row>(path) : managementService.page<Row>(path, { page: 0, size: 100 }).then((p) => p.content); loader.then(setRows).catch((e) => setError(errorMessage(e))); }, [path, version]);
  if (managerOnly && !isManager(user)) return <Navigate to="/management" replace />;
  return <><PageHeader title={title} description={description} action={action?.(refresh)} />{error ? <ErrorState message={error} retry={refresh} /> : <DataTable rows={rows} columns={columns} actions={section ? (row) => <ManagerRowAction path={path} section={section} row={row} onSaved={refresh} /> : undefined} />}</>;
}

function ManagerRowAction({ path, section, row, onSaved }: { path: string; section: ManageFormSection; row: Row; onSaved: () => void }) {
  return <div className="management-inline-actions"><ManagerEditor path={path} section={section} row={row} onSaved={onSaved} /><Button variant="ghost" onClick={async () => { if (confirm(`Xóa “${value(row, "name") || value(row, "title") || value(row, "code") || idOf(row)}”?`)) { await managementService.remove(`${path}/${idOf(row)}`); onSaved(); } }}>Xóa</Button></div>;
}

function ManagerCreate({ section, onSaved }: { section: ManageFormSection; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const title = section === "catalog" ? "Thêm danh mục / thương hiệu" : section === "category" ? "Thêm danh mục" : section === "brand" ? "Thêm thương hiệu" : section === "coupons" ? "Tạo mã giảm giá" : section === "gold" ? "Thêm giá vàng" : section === "banner" ? "Thêm banner" : "Thêm nội dung";
  return <><Button onClick={() => setOpen(true)}>{title}</Button><Modal open={open} onClose={() => setOpen(false)} title={title}><ManagerForm section={section} onSaved={() => { setOpen(false); onSaved(); }} /></Modal></>;
}

function ManagerForm({ section, row, path, onSaved }: { section?: ManageFormSection; row?: Row; path?: string; onSaved: () => void }) {
  const [busy, setBusy] = useState(false), [error, setError] = useState("");
  const [createKind, setCreateKind] = useState(section === "content" ? "page" : section === "catalog" ? "category" : "");
  const { categories } = useCatalog();
  const isEdit = !!row && !!path;
  const effectiveSection = (section === "catalog" ? createKind : section === "content" ? createKind : section) as ManageFormSection | undefined;

  const submit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const f = new FormData(event.currentTarget);
    let target = path ?? "/categories";
    if (section === "catalog") target = createKind === "brand" ? "/brands" : "/categories";
    if (section === "content") target = createKind === "banner" ? "/admin/banners" : "/admin/contents";
    let body: Row;
    if (effectiveSection === "brand" || target === "/brands") body = { name: f.get("name"), logo: optionalText(f.get("logo")), description: optionalText(f.get("description")), status: f.get("status") || "HOAT_DONG" };
    else if (effectiveSection === "category" || target === "/categories") body = { name: f.get("name"), parentId: optionalNumber(f.get("parentId")), description: optionalText(f.get("description")), status: f.get("status") || "HOAT_DONG" };
    else if (effectiveSection === "coupons" || target === "/admin/coupons") body = { code: f.get("code"), name: f.get("name"), type: f.get("type") || "SO_TIEN", value: Number(f.get("value")), minimumOrder: optionalNumber(f.get("minimumOrder")) ?? 0, maximumDiscount: optionalNumber(f.get("maximumDiscount")), quantity: optionalNumber(f.get("quantity")) ?? 0, startsAt: toInstant(f.get("startsAt")), endsAt: toInstant(f.get("endsAt")), status: f.get("status") || "HOAT_DONG" };
    else if (effectiveSection === "gold" || target === "/gold-prices") body = { type: f.get("type"), buyPrice: Number(f.get("buyPrice")), sellPrice: Number(f.get("sellPrice")), date: f.get("date"), unit: f.get("unit") || "chỉ" };
    else if (effectiveSection === "banner" || target === "/admin/banners") body = { title: optionalText(f.get("title")), image: f.get("image"), link: optionalText(f.get("link")), sortOrder: optionalNumber(f.get("sortOrder")) ?? 0, startsAt: toInstant(f.get("startsAt")), endsAt: toInstant(f.get("endsAt")), status: f.get("status") || "HIEN_THI" };
    else body = { type: f.get("type") || "GIOI_THIEU", title: f.get("title"), slug: f.get("slug"), content: f.get("content"), status: f.get("status") || "HIEN_THI" };
    setBusy(true); setError("");
    try { if (isEdit) await managementService.update(`${target}/${idOf(row!)}`, body); else await managementService.create(target, body); onSaved(); }
    catch (err) { setError(errorMessage(err)); }
    finally { setBusy(false); }
  };

  return <form className="management-form" onSubmit={(event) => void submit(event)}>
    {section === "catalog" && <label>Loại<select name="type" value={createKind} onChange={(e) => setCreateKind(e.target.value)}><option value="category">Danh mục</option><option value="brand">Thương hiệu</option></select></label>}
    {section === "content" && <label>Loại<select name="kind" value={createKind} onChange={(e) => setCreateKind(e.target.value)}><option value="page">Trang nội dung</option><option value="banner">Banner</option></select></label>}
    {(effectiveSection === "category" || effectiveSection === "brand") && <><Input label="Tên" name="name" defaultValue={inputValue(row ?? {}, "name")} required />{effectiveSection === "category" && <label>Danh mục cha<select name="parentId" defaultValue={inputValue(row ?? {}, "parentId")}><option value="">Không có</option>{categories.filter((category) => category.id !== idOf(row ?? {})).map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label>}{effectiveSection === "brand" && <Input label="Logo" name="logo" placeholder="URL logo" defaultValue={inputValue(row ?? {}, "logo")} />}<label>Mô tả<textarea name="description" defaultValue={inputValue(row ?? {}, "description")} /></label><StatusSelect defaultValue={inputValue(row ?? {}, "status") || "HOAT_DONG"} /></>}
    {effectiveSection === "coupons" && <><Input label="Mã giảm giá" name="code" defaultValue={inputValue(row ?? {}, "code")} required /><Input label="Tên" name="name" defaultValue={inputValue(row ?? {}, "name")} required /><label>Loại<select name="type" defaultValue={inputValue(row ?? {}, "type") || "SO_TIEN"}><option value="SO_TIEN">Giảm tiền</option><option value="PHAN_TRAM">Phần trăm</option></select></label><Input label="Giá trị" name="value" type="number" defaultValue={inputValue(row ?? {}, "value")} required /><Input label="Đơn tối thiểu" name="minimumOrder" type="number" defaultValue={inputValue(row ?? {}, "minimumOrder")} /><Input label="Giảm tối đa" name="maximumDiscount" type="number" defaultValue={inputValue(row ?? {}, "maximumDiscount")} /><Input label="Số lượng" name="quantity" type="number" defaultValue={inputValue(row ?? {}, "quantity")} /><Input label="Bắt đầu" name="startsAt" type="datetime-local" defaultValue={toDateTimeLocal(row ?? {}, "startsAt")} required /><Input label="Kết thúc" name="endsAt" type="datetime-local" defaultValue={toDateTimeLocal(row ?? {}, "endsAt")} required /><StatusSelect defaultValue={inputValue(row ?? {}, "status") || "HOAT_DONG"} coupon /></>}
    {effectiveSection === "gold" && <><Input label="Loại vàng" name="type" defaultValue={inputValue(row ?? {}, "type")} required /><Input label="Giá mua" name="buyPrice" type="number" defaultValue={inputValue(row ?? {}, "buyPrice")} required /><Input label="Giá bán" name="sellPrice" type="number" defaultValue={inputValue(row ?? {}, "sellPrice")} required /><Input label="Ngày" name="date" type="date" defaultValue={inputValue(row ?? {}, "date")} required /><Input label="Đơn vị" name="unit" defaultValue={inputValue(row ?? {}, "unit") || "chỉ"} required /></>}
    {effectiveSection === "banner" && <><Input label="Tiêu đề" name="title" defaultValue={inputValue(row ?? {}, "title")} /><Input label="Ảnh banner" name="image" defaultValue={inputValue(row ?? {}, "image")} required /><Input label="Liên kết" name="link" placeholder="/products" defaultValue={inputValue(row ?? {}, "link")} /><Input label="Thứ tự" name="sortOrder" type="number" defaultValue={inputValue(row ?? {}, "sortOrder")} /><Input label="Bắt đầu" name="startsAt" type="datetime-local" defaultValue={toDateTimeLocal(row ?? {}, "startsAt")} /><Input label="Kết thúc" name="endsAt" type="datetime-local" defaultValue={toDateTimeLocal(row ?? {}, "endsAt")} /><StatusSelect defaultValue={inputValue(row ?? {}, "status") || "HIEN_THI"} content /></>}
    {effectiveSection === "page" && <><label>Loại nội dung<select name="type" defaultValue={inputValue(row ?? {}, "type") || "GIOI_THIEU"}><option value="GIOI_THIEU">Giới thiệu</option><option value="CHINH_SACH">Chính sách</option><option value="FAQ">FAQ</option><option value="LIEN_HE">Liên hệ</option><option value="TRANG_CHU">Trang chủ</option></select></label><Input label="Tiêu đề" name="title" defaultValue={inputValue(row ?? {}, "title")} required /><Input label="Slug" name="slug" defaultValue={inputValue(row ?? {}, "slug")} required /><label>Nội dung<textarea name="content" defaultValue={inputValue(row ?? {}, "content")} required /></label><StatusSelect defaultValue={inputValue(row ?? {}, "status") || "HIEN_THI"} content /></>}
    {error && <p className="error-banner">{error}</p>}<Button loading={busy}>Lưu</Button>
  </form>;
}

function ManagerEditor({ path, section, row, onSaved }: { path: string; section: ManageFormSection; row: Row; onSaved: () => void }) { const [open, setOpen] = useState(false); return <><Button variant="secondary" onClick={() => setOpen(true)}>Sửa</Button><Modal open={open} onClose={() => setOpen(false)} title="Sửa dữ liệu"><ManagerForm path={path} section={section} row={row} onSaved={() => { setOpen(false); onSaved(); }} /></Modal></>; }
function StatusSelect({ defaultValue, product = false, coupon = false, content = false }: { defaultValue?: string; product?: boolean; coupon?: boolean; content?: boolean }) {
  const options = product
    ? [["DANG_BAN", "Đang bán"], ["NGUNG_BAN", "Ngừng bán"]]
    : coupon
      ? [["HOAT_DONG", "Hoạt động"], ["NGUNG", "Ngừng"]]
      : content
        ? [["HIEN_THI", "Hiển thị"], ["AN", "Ẩn"]]
        : [["HOAT_DONG", "Hoạt động"], ["NGUNG_HOAT_DONG", "Ngừng hoạt động"]];
  return <label>Trạng thái<select name="status" defaultValue={defaultValue}>{options.map(([key, label]) => <option key={key} value={key}>{label}</option>)}</select></label>;
}

function PaymentsAdmin() { return <SimplePage title="Thanh toán & giao dịch" description="Theo dõi thanh toán COD và giao dịch chuyển khoản." path="/admin/payments" columns={[["orderId", "Đơn hàng"], ["amount", "Số tiền"], ["method", "Phương thức"], ["status", "Trạng thái"], ["paidAt", "Thời gian thanh toán"]]} />; }
function CatalogAdmin() {
  const [categories, setCategories] = useState<Row[]>([]), [brands, setBrands] = useState<Row[]>([]), [error, setError] = useState(""), [version, setVersion] = useState(0);
  const refresh = () => setVersion((n) => n + 1);
  useEffect(() => { Promise.all([managementService.list<Row>("/categories"), managementService.list<Row>("/brands")]).then(([c, b]) => { setCategories(c); setBrands(b); }).catch((e) => setError(errorMessage(e))); }, [version]);
  const categoryName = new Map(categories.map((category) => [idOf(category), value(category, "name")]));
  const categoryRows = categories.map((category) => ({ ...category, parentName: category.parentId ? categoryName.get(String(category.parentId)) || "—" : "—" }));
  return <><PageHeader title="Danh mục & thương hiệu" description="Quản lý danh mục và thương hiệu hiển thị trên website." action={<div className="management-header-actions"><ManagerCreate section="category" onSaved={refresh} /><ManagerCreate section="brand" onSaved={refresh} /></div>} />{error ? <ErrorState message={error} retry={refresh} /> : <div className="management-stack"><section><h2 className="management-section-title">Danh mục</h2><DataTable rows={categoryRows} columns={[["name", "Tên"], ["parentName", "Danh mục cha"], ["description", "Mô tả"], ["status", "Trạng thái"]]} actions={(row) => <ManagerRowAction path="/categories" section="category" row={row} onSaved={refresh} />} /></section><section><h2 className="management-section-title">Thương hiệu</h2><DataTable rows={brands} columns={[["name", "Tên"], ["description", "Mô tả"], ["status", "Trạng thái"]]} actions={(row) => <ManagerRowAction path="/brands" section="brand" row={row} onSaved={refresh} />} /></section></div>}</>;
}
function accountStatusLabel(status: unknown) {
  return status === "KHOA" ? "Đã khóa" : "Hoạt động";
}

function CustomersAdmin() {
  const { user } = useStore();
  const [rows, setRows] = useState<Row[]>([]);
  const [keyword, setKeyword] = useState("");
  const [error, setError] = useState("");
  const [version, setVersion] = useState(0);
  const [busyId, setBusyId] = useState("");
  const refresh = () => setVersion((n) => n + 1);

  useEffect(() => {
    managementService
      .page<Row>("/admin/customers", { page: 0, size: 100, keyword: keyword || undefined })
      .then((data) => setRows(data.content))
      .catch((err) => setError(errorMessage(err)));
  }, [keyword, version]);

  if (!isManager(user)) return <Navigate to="/management" replace />;

  return <>
    <PageHeader
      title="Quản lý khách hàng"
      description="Thêm, sửa thông tin và khóa tài khoản khách hàng. Không xóa cứng để giữ lịch sử đơn hàng."
      action={<CustomerEditor onSaved={refresh} />}
    />
    <div className="management-toolbar customer-admin-toolbar">
      <Input label="Tìm khách hàng" placeholder="Nhập họ tên, email hoặc số điện thoại" value={keyword} onChange={(event) => setKeyword(event.target.value)} />
    </div>
    {error ? <ErrorState message={error} retry={refresh} /> : <DataTable rows={rows.map((row) => ({ ...row, statusLabel: accountStatusLabel(row.status) }))} columns={[["name", "Họ tên"], ["email", "Email"], ["phone", "SĐT"], ["statusLabel", "Trạng thái"], ["createdAt", "Ngày tạo"]]} actions={(row) => <div className="management-inline-actions">
      <CustomerEditor row={row} onSaved={refresh} />
      <Button variant="ghost" loading={busyId === idOf(row)} disabled={value(row, "status") === "KHOA"} onClick={async () => {
        if (!confirm(`Khóa tài khoản khách hàng “${value(row, "name") || value(row, "email") || idOf(row)}”?`)) return;
        setBusyId(idOf(row));
        try {
          await managementService.remove(`/admin/customers/${idOf(row)}`);
          refresh();
        } catch (err) {
          setError(errorMessage(err));
        } finally {
          setBusyId("");
        }
      }}>{value(row, "status") === "KHOA" ? "Đã khóa" : "Xóa/khóa"}</Button>
    </div>} />}
  </>;
}

function CustomerEditor({ row, onSaved }: { row?: Row; onSaved: () => void }) {
  const [open, setOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const editing = !!row;

  return <>
    <Button variant={editing ? "secondary" : "primary"} onClick={() => { setError(""); setOpen(true); }}>{editing ? "Sửa" : "Thêm khách hàng"}</Button>
    <Modal open={open} onClose={() => setOpen(false)} title={editing ? "Sửa khách hàng" : "Thêm khách hàng"}>
      <form className="management-form customer-form" onSubmit={async (event) => {
        event.preventDefault();
        const form = new FormData(event.currentTarget);
        const body = {
          name: String(form.get("name") ?? "").trim(),
          email: String(form.get("email") ?? "").trim(),
          phone: optionalText(form.get("phone")),
          ...(editing ? {} : { password: String(form.get("password") ?? ""), status: form.get("status") || "HOAT_DONG" }),
        };
        setBusy(true);
        setError("");
        try {
          if (editing) {
            await managementService.update(`/admin/customers/${idOf(row!)}`, body);
            const nextStatus = String(form.get("status") || value(row!, "status") || "HOAT_DONG");
            if (nextStatus !== value(row!, "status")) {
              await managementService.patch(`/admin/customers/${idOf(row!)}/status`, { status: nextStatus });
            }
          } else {
            await managementService.create("/admin/customers", body);
          }
          setOpen(false);
          onSaved();
        } catch (err) {
          setError(errorMessage(err));
        } finally {
          setBusy(false);
        }
      }}>
        <Input label="Họ và tên" name="name" defaultValue={inputValue(row ?? {}, "name")} required />
        <Input label="Email" name="email" type="email" defaultValue={inputValue(row ?? {}, "email")} required />
        <Input label="Số điện thoại" name="phone" placeholder="Ví dụ: 0912345678" defaultValue={inputValue(row ?? {}, "phone")} />
        {!editing && <Input label="Mật khẩu" name="password" type="password" minLength={8} required />}
        <label>Trạng thái<select name="status" defaultValue={inputValue(row ?? {}, "status") || "HOAT_DONG"}><option value="HOAT_DONG">Hoạt động</option><option value="KHOA">Khóa</option></select></label>
        {error && <p className="error-banner">{error}</p>}
        <Button loading={busy}>Lưu khách hàng</Button>
      </form>
    </Modal>
  </>;
}
function staffRoleLabel(role: unknown) {
  return role === "QUAN_LY" ? "Quản lý" : "Nhân viên";
}

function StaffAdmin() {
  const { user } = useStore();
  const [rows, setRows] = useState<Row[]>([]);
  const [keyword, setKeyword] = useState("");
  const [error, setError] = useState("");
  const [version, setVersion] = useState(0);
  const [busyId, setBusyId] = useState("");
  const refresh = () => setVersion((n) => n + 1);

  useEffect(() => {
    managementService
      .page<Row>("/admin/staff", { page: 0, size: 100, keyword: keyword || undefined })
      .then((data) => setRows(data.content))
      .catch((err) => setError(errorMessage(err)));
  }, [keyword, version]);

  if (!isManager(user)) return <Navigate to="/management" replace />;

  return <>
    <PageHeader
      title="Quản lý nhân viên"
      description="Thêm, sửa thông tin, phân quyền và khóa tài khoản nhân viên. Không cho quản lý tự khóa hoặc tự đổi quyền của mình."
      action={<StaffEditor onSaved={refresh} />}
    />
    <div className="management-toolbar staff-admin-toolbar">
      <Input label="Tìm nhân viên" placeholder="Nhập họ tên, email, SĐT hoặc tên đăng nhập" value={keyword} onChange={(event) => setKeyword(event.target.value)} />
    </div>
    {error ? <ErrorState message={error} retry={refresh} /> : <DataTable rows={rows.map((row) => ({ ...row, roleLabel: staffRoleLabel(row.role), statusLabel: accountStatusLabel(row.status) }))} columns={[["name", "Họ tên"], ["username", "Tên đăng nhập"], ["email", "Email"], ["phone", "SĐT"], ["roleLabel", "Vai trò"], ["statusLabel", "Trạng thái"]]} actions={(row) => <div className="management-inline-actions">
      <StaffEditor row={row} onSaved={refresh} />
      <Button variant="ghost" loading={busyId === idOf(row)} disabled={String(user?.id ?? "") === idOf(row) || value(row, "status") === "KHOA"} onClick={async () => {
        if (!confirm(`Khóa tài khoản nhân viên “${value(row, "name") || value(row, "username") || idOf(row)}”?`)) return;
        setBusyId(idOf(row));
        try {
          await managementService.remove(`/admin/staff/${idOf(row)}`);
          refresh();
        } catch (err) {
          setError(errorMessage(err));
        } finally {
          setBusyId("");
        }
      }}>{value(row, "status") === "KHOA" ? "Đã khóa" : "Xóa/khóa"}</Button>
    </div>} />}
  </>;
}

function StaffEditor({ row, onSaved }: { row?: Row; onSaved: () => void }) {
  const { user } = useStore();
  const [open, setOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const editing = !!row;
  const self = editing && String(user?.id ?? "") === idOf(row);

  return <>
    <Button variant={editing ? "secondary" : "primary"} onClick={() => { setError(""); setOpen(true); }}>{editing ? "Sửa" : "Thêm nhân viên"}</Button>
    <Modal open={open} onClose={() => setOpen(false)} title={editing ? "Sửa nhân viên" : "Thêm nhân viên"}>
      <form className="management-form staff-form" onSubmit={async (event) => {
        event.preventDefault();
        const form = new FormData(event.currentTarget);
        const baseBody = {
          name: String(form.get("name") ?? "").trim(),
          email: String(form.get("email") ?? "").trim(),
          phone: optionalText(form.get("phone")),
        };
        setBusy(true);
        setError("");
        try {
          if (editing) {
            await managementService.update(`/admin/staff/${idOf(row!)}`, baseBody);
            const nextRole = String(form.get("role") || value(row!, "role") || "NHAN_VIEN");
            const nextStatus = String(form.get("status") || value(row!, "status") || "HOAT_DONG");
            if (!self && nextRole !== value(row!, "role")) {
              await managementService.patch(`/admin/staff/${idOf(row!)}/role`, { role: nextRole });
            }
            if (!self && nextStatus !== value(row!, "status")) {
              await managementService.patch(`/admin/staff/${idOf(row!)}/status`, { status: nextStatus });
            }
          } else {
            await managementService.create("/admin/staff", {
              ...baseBody,
              username: String(form.get("username") ?? "").trim(),
              password: String(form.get("password") ?? ""),
              role: form.get("role") || "NHAN_VIEN",
            });
          }
          setOpen(false);
          onSaved();
        } catch (err) {
          setError(errorMessage(err));
        } finally {
          setBusy(false);
        }
      }}>
        <Input label="Họ và tên" name="name" defaultValue={inputValue(row ?? {}, "name")} required />
        {!editing && <Input label="Tên đăng nhập" name="username" placeholder="Ví dụ: staff02" defaultValue={inputValue(row ?? {}, "username")} required />}
        {editing && <Input label="Tên đăng nhập" value={inputValue(row ?? {}, "username")} disabled />}
        <Input label="Email" name="email" type="email" defaultValue={inputValue(row ?? {}, "email")} required />
        <Input label="Số điện thoại" name="phone" placeholder="Ví dụ: 0912345678" defaultValue={inputValue(row ?? {}, "phone")} />
        {!editing && <Input label="Mật khẩu" name="password" type="password" minLength={8} required />}
        <div className="form-grid">
          <label>Vai trò<select name="role" defaultValue={inputValue(row ?? {}, "role") || "NHAN_VIEN"} disabled={self}><option value="NHAN_VIEN">Nhân viên</option><option value="QUAN_LY">Quản lý</option></select></label>
          <label>Trạng thái<select name="status" defaultValue={inputValue(row ?? {}, "status") || "HOAT_DONG"} disabled={self}><option value="HOAT_DONG">Hoạt động</option><option value="KHOA">Khóa</option></select></label>
        </div>
        {self && <p className="info-banner">Bạn không thể tự đổi vai trò hoặc tự khóa tài khoản của mình.</p>}
        {error && <p className="error-banner">{error}</p>}
        <Button loading={busy}>Lưu nhân viên</Button>
      </form>
    </Modal>
  </>;
}
function GoldAdmin() { return <SimplePage title="Giá vàng" description="Cập nhật và xem lịch sử giá vàng." path="/gold-prices" section="gold" columns={[["type", "Loại"], ["buyPrice", "Giá mua"], ["sellPrice", "Giá bán"], ["date", "Ngày"], ["unit", "Đơn vị"]]} managerOnly action={(refresh) => <ManagerCreate section="gold" onSaved={refresh} />} />; }
function CouponsAdmin() { return <SimplePage title="Mã giảm giá" description="Quản lý mã giảm giá trong phạm vi đồ án." path="/admin/coupons" section="coupons" columns={[["code", "Mã"], ["name", "Tên"], ["type", "Loại"], ["value", "Giá trị"], ["status", "Trạng thái"]]} managerOnly action={(refresh) => <ManagerCreate section="coupons" onSaved={refresh} />} />; }
function ContentAdmin() {
  const [contents, setContents] = useState<Row[]>([]), [banners, setBanners] = useState<Row[]>([]), [error, setError] = useState(""), [version, setVersion] = useState(0);
  const refresh = () => setVersion((n) => n + 1);
  useEffect(() => { Promise.all([managementService.page<Row>("/admin/contents", { page: 0, size: 100 }).then((p) => p.content), managementService.page<Row>("/admin/banners", { page: 0, size: 100 }).then((p) => p.content)]).then(([c, b]) => { setContents(c); setBanners(b); }).catch((e) => setError(errorMessage(e))); }, [version]);
  return <><PageHeader title="Banner & nội dung" description="Quản lý banner và các trang thông tin public." action={<div className="management-header-actions"><ManagerCreate section="banner" onSaved={refresh} /><ManagerCreate section="page" onSaved={refresh} /></div>} />{error ? <ErrorState message={error} retry={refresh} /> : <div className="management-stack"><section><h2 className="management-section-title">Banner</h2><DataTable rows={banners} columns={[["title", "Tiêu đề"], ["link", "Liên kết"], ["sortOrder", "Thứ tự"], ["status", "Trạng thái"]]} actions={(row) => <ManagerRowAction path="/admin/banners" section="banner" row={row} onSaved={refresh} />} /></section><section><h2 className="management-section-title">Trang nội dung</h2><DataTable rows={contents} columns={[["title", "Tiêu đề"], ["type", "Loại"], ["slug", "Slug"], ["status", "Trạng thái"]]} actions={(row) => <ManagerRowAction path="/admin/contents" section="page" row={row} onSaved={refresh} />} /></section></div>}</>;
}

export default function Management() {
  const { user, authLoading } = useStore();
  const [params, setParams] = useSearchParams();
  if (authLoading) return <div className="container page">Đang tải tài khoản…</div>;
  if (!user) return <Navigate to="/login" replace />;
  const section = (params.get("section") as Section) || "dashboard";
  const manager = isManager(user);
  const items = [
    { key: "dashboard", label: "Tổng quan", icon: BarChart3, manager: false },
    { key: "orders", label: "Đơn hàng", icon: ClipboardList, manager: false },
    { key: "products", label: "Sản phẩm", icon: PackageSearch, manager: false },
    { key: "inventory", label: "Kho hàng", icon: Boxes, manager: false },
    { key: "reviews", label: "Đánh giá", icon: MessageSquareText, manager: false },
    { key: "payments", label: "Thanh toán", icon: CreditCard, manager: false },
    { key: "catalog", label: "Danh mục & thương hiệu", icon: Layers3, manager: true },
    { key: "gold", label: "Giá vàng", icon: WalletCards, manager: true },
    { key: "customers", label: "Khách hàng", icon: Users, manager: true },
    { key: "staff", label: "Nhân viên", icon: Users, manager: true },
    { key: "coupons", label: "Mã giảm giá", icon: Gift, manager: true },
    { key: "content", label: "Banner & nội dung", icon: ImageIcon, manager: true },
  ].filter((item) => manager || !item.manager) as { key: Section; label: string; icon: typeof Tags; manager: boolean }[];
  const allowed = items.some((item) => item.key === section) ? section : "dashboard";
  const content = allowed === "orders" ? <ManageOrders /> : allowed === "products" ? <ProductsAdmin /> : allowed === "inventory" ? <InventoryAdmin /> : allowed === "reviews" ? <ReviewsAdmin /> : allowed === "payments" ? <PaymentsAdmin /> : allowed === "catalog" ? <CatalogAdmin /> : allowed === "gold" ? <GoldAdmin /> : allowed === "customers" ? <CustomersAdmin /> : allowed === "staff" ? <StaffAdmin /> : allowed === "coupons" ? <CouponsAdmin /> : allowed === "content" ? <ContentAdmin /> : <DashboardPanel />;
  return <div className="management-page"><aside className="management-sidebar"><Link to="/" className="management-brand"><Boxes size={22} /><span>Vận hành cửa hàng</span><small>{roleLabels[user.role]}</small></Link><nav aria-label="Điều hướng quản trị">{items.map((item) => <button key={item.key} className={allowed === item.key ? "active" : ""} onClick={() => setParams({ section: item.key })}><item.icon size={18} />{item.label}</button>)}</nav></aside><main className="management-main">{content}</main></div>;
}
