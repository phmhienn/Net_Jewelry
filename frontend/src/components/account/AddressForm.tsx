import { useEffect, useMemo, useState } from "react";
import { MapPin, Plus, Check, Pencil, Trash2 } from "lucide-react";
import { accountService } from "../../services/accountService";
import type { Address } from "../../types";
import { Input } from "../common/Input";
import { Button } from "../common/Button";
import { ErrorState } from "../common/Feedback";
import { validateAddress } from "../../utils/validation";
import { errorMessage } from "../../utils/format";
import { useStore } from "../../context/StoreContext";

const blank = (name = "", phone = ""): Address => ({
  name,
  phone,
  street: "",
  city: "",
  district: "",
  ward: "",
  defaultAddress: true,
});

function addressLabel(a: Address) {
  return [a.street, a.ward, a.district, a.city].filter(Boolean).join(", ");
}

export function AddressForm() {
  const { user, notify } = useStore();

  const [addresses, setAddresses] = useState<Address[]>([]);
  const [address, setAddress] = useState<Address>(blank(user?.name, user?.phone));
  const [original, setOriginal] = useState<Address>(blank(user?.name, user?.phone));
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [deleting, setDeleting] = useState<string | null>(null);
  const [error, setError] = useState("");
  const [loadError, setLoadError] = useState("");
  const [version, setVersion] = useState(0);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [mode, setMode] = useState<"view" | "edit">("view");

  // Kiểm tra người dùng có sửa gì không
  const isDirty = useMemo(
    () => JSON.stringify(address) !== JSON.stringify(original),
    [address, original],
  );

  const isNew = !address.id;

  useEffect(() => {
    let active = true;
    setLoading(true);
    setLoadError("");
    accountService
      .addresses()
      .then((data) => {
        if (!active) return;
        setAddresses(data);
        const first = data.find((a) => a.defaultAddress) ?? data[0];
        if (first) {
          setAddress(first);
          setOriginal(first);
          setMode("view");
        } else {
          const b = blank(user?.name, user?.phone);
          setAddress(b);
          setOriginal(b);
          setMode("edit");
        }
      })
      .catch((e) => { if (active) setLoadError(errorMessage(e)); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [version, user?.name, user?.phone]);

  const select = (item: Address) => {
    setAddress(item);
    setOriginal(item);
    setErrors({});
    setError("");
    setMode("view");
  };

  const startNew = () => {
    const b = blank(user?.name, user?.phone);
    setAddress(b);
    setOriginal(b);
    setErrors({});
    setError("");
    setMode("edit");
  };

  const startEdit = () => {
    setErrors({});
    setError("");
    setMode("edit");
  };

  const handleDelete = async (id: string) => {
    if (!confirm("Bạn có chắc muốn xoá địa chỉ này không?")) return;
    setDeleting(id);
    try {
      await accountService.removeAddress(id);
      notify("Đã xoá địa chỉ");
      setVersion((n) => n + 1);
    } catch (e) {
      notify(errorMessage(e), true);
    } finally {
      setDeleting(null);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const issues = validateAddress(address);
    setErrors(issues);
    if (Object.keys(issues).length) return;
    setBusy(true);
    setError("");
    try {
      await accountService.saveAddress(address);
      notify(isNew ? "Đã thêm địa chỉ giao hàng mới" : "Đã cập nhật địa chỉ giao hàng");
      setVersion((n) => n + 1);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  const update = (field: keyof Address, value: string) =>
    setAddress((a) => ({ ...a, [field]: value }));

  if (loading) return (
    <div className="address-loading" role="status">
      <div className="address-skeleton" />
      <div className="address-skeleton short" />
    </div>
  );
  if (loadError) return <ErrorState message={loadError} retry={() => setVersion((n) => n + 1)} />;

  return (
    <div className="address-page">
      {/* Header */}
      <div className="address-page-header">
        <div>
          <h2>Địa chỉ giao hàng</h2>
          <p className="address-page-sub">Quản lý địa chỉ nhận hàng của bạn. Địa chỉ mặc định sẽ tự động điền khi thanh toán.</p>
        </div>
        <button type="button" className="address-add-btn" onClick={startNew}>
          <Plus size={16} /> Thêm địa chỉ
        </button>
      </div>

      {/* Danh sách chip địa chỉ */}
      {addresses.length > 0 && (
        <div className="address-cards">
          {addresses.map((item) => (
            <div
              key={item.id}
              className={`address-card ${item.id === address.id && mode === "view" ? "selected" : ""}`}
              onClick={() => select(item)}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => e.key === "Enter" && select(item)}
            >
              <div className="address-card-top">
                <span className="address-card-name">
                  <MapPin size={14} />
                  {item.name}
                </span>
                {item.defaultAddress && (
                  <span className="address-default-badge">Mặc định</span>
                )}
              </div>
              <p className="address-card-detail">{addressLabel(item)}</p>
              <p className="address-card-phone">{item.phone}</p>
              <div className="address-card-actions">
                <button
                  type="button"
                  className="address-action-btn"
                  onClick={(e) => { e.stopPropagation(); select(item); startEdit(); }}
                >
                  <Pencil size={13} /> Sửa
                </button>
                <button
                  type="button"
                  className="address-action-btn danger"
                  disabled={deleting === item.id}
                  onClick={(e) => { e.stopPropagation(); void handleDelete(item.id!); }}
                >
                  <Trash2 size={13} /> {deleting === item.id ? "Đang xoá…" : "Xoá"}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Form */}
      <div className={`address-form-wrap ${mode === "edit" ? "visible" : ""}`}>
        <div className="address-form-header">
          <h3>{isNew ? "Thêm địa chỉ mới" : "Chỉnh sửa địa chỉ"}</h3>
          {isDirty && !isNew && (
            <span className="address-dirty-badge">Chưa lưu thay đổi</span>
          )}
        </div>

        <form noValidate onSubmit={(e) => void handleSubmit(e)} className="address-form-body">
          <div className="address-form-grid">
            <Input label="Họ và tên *" value={address.name} error={errors.name} onChange={(e) => update("name", e.target.value)} />
            <Input label="Số điện thoại *" type="tel" value={address.phone} error={errors.phone} onChange={(e) => update("phone", e.target.value)} />
          </div>
          <Input label="Số nhà, tên đường *" value={address.street} error={errors.street} onChange={(e) => update("street", e.target.value)} />
          <div className="address-form-grid">
            <Input label="Tỉnh / Thành phố *" value={address.city} error={errors.city} onChange={(e) => update("city", e.target.value)} />
            <Input label="Quận / Huyện *" value={address.district} error={errors.district} onChange={(e) => update("district", e.target.value)} />
          </div>
          <Input label="Phường / Xã *" value={address.ward ?? ""} error={errors.ward} onChange={(e) => update("ward", e.target.value)} />

          <label className="address-default-check">
            <input
              type="checkbox"
              checked={!!address.defaultAddress}
              onChange={(e) => setAddress((a) => ({ ...a, defaultAddress: e.target.checked }))}
            />
            <span>Đặt làm địa chỉ mặc định</span>
          </label>

          {error && <p className="error-banner" role="alert">{error}</p>}

          <div className="address-form-actions">
            <Button type="submit" loading={busy}>
              <Check size={16} />
              {isNew ? "Lưu địa chỉ mới" : isDirty ? "Lưu thay đổi" : "Đã lưu"}
            </Button>
            {mode === "edit" && !isNew && (
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => { setAddress(original); setErrors({}); setError(""); setMode("view"); }}
              >
                Huỷ
              </button>
            )}
          </div>
        </form>
      </div>

      {/* Empty state khi chưa có địa chỉ và không ở mode edit */}
      {!addresses.length && mode !== "edit" && (
        <div className="address-empty">
          <MapPin size={36} strokeWidth={1.5} />
          <p>Bạn chưa có địa chỉ giao hàng nào.</p>
          <button type="button" className="btn btn-primary" onClick={startNew}>
            <Plus size={16} /> Thêm địa chỉ đầu tiên
          </button>
        </div>
      )}
    </div>
  );
}
