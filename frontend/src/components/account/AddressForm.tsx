import { useEffect, useState } from "react";
import { useStore } from "../../context/StoreContext";
import { accountService } from "../../services/accountService";
import type { Address } from "../../types";
import { Input } from "../common/Input";
import { Button } from "../common/Button";
import { ErrorState } from "../common/Feedback";
import { validateAddress } from "../../utils/validation";
import { errorMessage } from "../../utils/format";
export function AddressForm() {
  const { user, notify } = useStore();
  const [address, setAddress] = useState<Address>({
    name: user?.name ?? "",
    phone: user?.phone ?? "",
    street: "",
    city: "",
    district: "",
  });
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [loadError, setLoadError] = useState("");
  const [version, setVersion] = useState(0);
  const [errors, setErrors] = useState<Record<string, string>>({});
  useEffect(() => {
    let active = true;
    setLoading(true);
    setLoadError("");
    accountService
      .getAddress(user!.email)
      .then((data) => {
        if (active && data) setAddress(data);
      })
      .catch((e) => {
        if (active) setLoadError(errorMessage(e));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [user, version]);
  if (loading) return <p role="status">Đang tải địa chỉ…</p>;
  if (loadError)
    return (
      <ErrorState message={loadError} retry={() => setVersion((n) => n + 1)} />
    );
  return (
    <form
      className="account-form"
      noValidate
      onSubmit={async (e) => {
        e.preventDefault();
        const issues = validateAddress(address);
        setErrors(issues);
        if (Object.keys(issues).length) return;
        setBusy(true);
        setError("");
        try {
          await accountService.saveAddress(user!.email, address);
          notify("Đã lưu địa chỉ giao hàng");
        } catch (e) {
          setError(errorMessage(e));
        } finally {
          setBusy(false);
        }
      }}
    >
      <h2>Địa chỉ giao hàng</h2>
      <p className="muted">Lưu địa chỉ nhận hàng của bạn.</p>
      {(
        [
          { key: "name", label: "Họ và tên", autoComplete: "name" },
          { key: "phone", label: "Số điện thoại", autoComplete: "tel" },
          {
            key: "street",
            label: "Số nhà, tên đường",
            autoComplete: "address-line1",
          },
          {
            key: "city",
            label: "Tỉnh / Thành phố",
            autoComplete: "address-level1",
          },
          {
            key: "district",
            label: "Phường / Xã, Quận / Huyện",
            autoComplete: "address-level2",
          },
        ] as const
      ).map((field) => (
        <Input
          key={field.key}
          label={field.label}
          autoComplete={field.autoComplete}
          value={address[field.key]}
          error={errors[field.key]}
          onChange={(e) =>
            setAddress((a) => ({ ...a, [field.key]: e.target.value }))
          }
        />
      ))}
      {error && (
        <p className="error-banner" role="alert">
          {error}
        </p>
      )}
      <Button type="submit" loading={busy}>
        Lưu địa chỉ
      </Button>
    </form>
  );
}
