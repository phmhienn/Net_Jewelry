import { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { ArrowRight, Eye, EyeOff } from "lucide-react";
import { useStore } from "../context/StoreContext";
import { authService } from "../services/authService";
import { Input } from "../components/common/Input";
import { Button } from "../components/common/Button";
import { validEmail } from "../utils/validation";
import { errorMessage } from "../utils/format";

export default function Auth({ register = false }: { register?: boolean }) {
  const { user, setUser, notify } = useStore();
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = typeof location.state === "object" && location.state && "from" in location.state && typeof location.state.from === "string" ? location.state.from : "/account";
  const [name, setName] = useState("");
  const [username, setUsername] = useState("");
  const [identifier, setIdentifier] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [show, setShow] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  if (user) return <Navigate to={redirectTo} replace />;

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const issues: Record<string, string> = {};
    if (register && name.trim().length < 2) issues.name = "Họ tên cần ít nhất 2 ký tự.";
    if (register && username.trim().length < 4) issues.username = "Tên đăng nhập cần ít nhất 4 ký tự.";
    if (register && !validEmail(email)) issues.email = "Vui lòng nhập email hợp lệ.";
    if (!register && !identifier.trim()) issues.identifier = "Nhập email hoặc tên đăng nhập.";
    if (password.length < 8) issues.password = "Mật khẩu cần ít nhất 8 ký tự.";
    if (register && confirm !== password) issues.confirm = "Mật khẩu xác nhận chưa khớp.";
    setErrors(issues);
    setError("");
    if (Object.keys(issues).length) return;
    setBusy(true);
    try {
      const account = register
        ? await authService.register(name.trim(), email.trim(), password, username.trim())
        : await authService.login(identifier.trim(), password);
      setUser(account);
      notify(register ? "Đăng ký thành công" : "Đăng nhập thành công");
      navigate(redirectTo, { replace: true });
    } catch (e) {
      const message = errorMessage(e);
      setError(!register && (message.includes("quyền") || message.includes("đăng nhập")) ? "Thông tin đăng nhập không đúng hoặc tài khoản đã khóa." : message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="container page auth-page">
      <div className="auth-card">
        <div className="eyebrow">CHÀO MỪNG ĐẾN VỚI NÉT</div>
        <h1>{register ? "Tạo tài khoản" : "Đăng nhập tài khoản"}</h1>
        <p className="auth-subtitle">
          {register ? "Tạo tài khoản khách hàng để mua hàng, lưu địa chỉ và theo dõi đơn." : "Dùng email hoặc tên đăng nhập cho khách hàng, nhân viên và quản lý."}
        </p>
        <form onSubmit={(event) => void submit(event)} noValidate>
          {register ? <>
            <Input label="Họ và tên" autoComplete="name" value={name} error={errors.name} onChange={(e) => setName(e.target.value)} />
            <Input label="Tên đăng nhập" autoComplete="username" value={username} error={errors.username} onChange={(e) => setUsername(e.target.value)} />
            <Input label="Email" type="email" autoComplete="email" value={email} error={errors.email} onChange={(e) => setEmail(e.target.value)} />
          </> : (
            <Input label="Email hoặc tên đăng nhập" autoComplete="username" value={identifier} error={errors.identifier} onChange={(e) => setIdentifier(e.target.value)} />
          )}
          <div className="password-field">
            <Input label="Mật khẩu" type={show ? "text" : "password"} autoComplete={register ? "new-password" : "current-password"} value={password} error={errors.password} onChange={(e) => setPassword(e.target.value)} />
            <button type="button" className="icon-btn password-toggle" aria-label={show ? "Ẩn mật khẩu" : "Hiện mật khẩu"} onClick={() => setShow((s) => !s)}>
              {show ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>
          {register && <Input label="Xác nhận mật khẩu" type={show ? "text" : "password"} autoComplete="new-password" value={confirm} error={errors.confirm} onChange={(e) => setConfirm(e.target.value)} />}
          {error && <p className="error-banner" role="alert">{error}</p>}
          <Button className="w-full" loading={busy} type="submit">{register ? "Đăng ký" : "Đăng nhập"}<ArrowRight size={17} /></Button>
        </form>
        <p className="auth-switch">
          {register ? "Đã có tài khoản?" : "Bạn mới đến với NÉT?"} <Link to={register ? "/login" : "/register"}>{register ? "Đăng nhập" : "Tạo tài khoản"}</Link>
        </p>
        {!register && <p className="auth-switch"><Link to="/forgot-password">Quên mật khẩu?</Link></p>}
      </div>
    </div>
  );
}
