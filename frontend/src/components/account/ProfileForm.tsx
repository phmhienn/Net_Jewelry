import { useState } from "react";
import { useStore } from "../../context/StoreContext";
import { authService } from "../../services/authService";
import { Input } from "../common/Input";
import { Button } from "../common/Button";
import { errorMessage } from "../../utils/format";

export function ProfileForm() {
  const { user, setUser, notify } = useStore();
  const [name, setName] = useState(user?.name ?? "");
  const [phone, setPhone] = useState(user?.phone ?? "");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [errors, setErrors] = useState<Record<string, string>>({});
  if (!user) return null;
  return (
    <form className="account-form" noValidate onSubmit={async (e) => {
      e.preventDefault();
      const issues: Record<string, string> = {};
      if (name.trim().length < 2) issues.name = "Họ tên cần ít nhất 2 ký tự.";
      if (phone && !/^(0\d{9}|\+84\d{9})$/.test(phone.replace(/[\s.-]/g, ""))) issues.phone = "Số điện thoại chưa hợp lệ.";
      setErrors(issues);
      if (Object.keys(issues).length) return;
      setBusy(true); setError("");
      try { setUser(await authService.update({ ...user, name: name.trim(), phone })); notify("Đã lưu thông tin cá nhân"); }
      catch (error) { setError(errorMessage(error)); }
      finally { setBusy(false); }
    }}>
      <h2>Thông tin cá nhân</h2>
      <p className="muted">Thông tin để NÉT đồng hành cùng bạn tốt hơn.</p>
      <Input label="Họ và tên" value={name} error={errors.name} onChange={(e) => setName(e.target.value)} autoComplete="name" />
      <Input label="Email" value={user.email} readOnly type="email" />
      <Input label="Số điện thoại" value={phone} error={errors.phone} onChange={(e) => setPhone(e.target.value)} autoComplete="tel" />
      {error && <p className="error-banner" role="alert">{error}</p>}
      <Button type="submit" loading={busy}>Lưu thay đổi</Button>
    </form>
  );
}
