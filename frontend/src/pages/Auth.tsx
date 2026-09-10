import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { ArrowRight, Eye, EyeOff } from "lucide-react";
import { useStore } from "../context/StoreContext";
import { authService } from "../services/authService";
import { Input } from "../components/common/Input";
import { Button } from "../components/common/Button";
import { isDemo } from "../data/config";
import { validEmail } from "../utils/validation";
import { errorMessage } from "../utils/format";
export default function Auth({ register = false }: { register?: boolean }) {
  const { user, setUser, notify } = useStore();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [show, setShow] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  if (user) return <Navigate to="/account" replace />;
  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const issues: Record<string, string> = {};
    if (register && name.trim().length < 2)
      issues.name = "Họ tên cần ít nhất 2 ký tự.";
    if (!validEmail(email)) issues.email = "Vui lòng nhập email hợp lệ.";
    if (password.length < 8) issues.password = "Mật khẩu cần ít nhất 8 ký tự.";
    if (register && confirm !== password)
      issues.confirm = "Mật khẩu xác nhận chưa khớp.";
    setErrors(issues);
    setError("");
    if (Object.keys(issues).length) {
      setTimeout(
        () =>
          form
            .querySelector<HTMLInputElement>('[aria-invalid="true"]')
            ?.focus(),
        0,
      );
      return;
    }
    setBusy(true);
    try {
      const account = register
        ? await authService.register(name.trim(), email.trim(), password)
        : await authService.login(email.trim(), password);
      setUser(account);
      notify(
        register
          ? isDemo
            ? "Đã tạo tài khoản trải nghiệm"
            : "Đăng ký thành công"
          : "Đăng nhập thành công",
      );
      navigate("/account");
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="container page auth-page">
      <div className="auth-card">
        <div className="eyebrow">CHÀO MỪNG ĐẾN VỚI NÉT</div>
        <h1>
          {register ? "Bắt đầu câu chuyện của bạn" : "Rất vui được gặp lại bạn"}
        </h1>
        <p className="auth-subtitle">
          {register
            ? "Lưu những thiết kế bạn yêu và theo dõi đơn hàng dễ dàng."
            : "Đăng nhập để tiếp tục với những điều bạn yêu."}
        </p>
        {isDemo && (
          <div className="info-banner">
            {register ? (
              "Đăng ký demo tạo phiên trải nghiệm trên trình duyệt này. Không lưu mật khẩu và không tạo tài khoản trên máy chủ."
            ) : (
              <>
                Tài khoản trải nghiệm:
                <br />
                <strong>demo@netjewelry.vn</strong> /{" "}
                <strong>NetDemo123!</strong>
                <button
                  className="text-link"
                  type="button"
                  onClick={() => {
                    setEmail("demo@netjewelry.vn");
                    setPassword("NetDemo123!");
                  }}
                >
                  Điền tài khoản demo <ArrowRight size={14} />
                </button>
              </>
            )}
          </div>
        )}
        <form onSubmit={(event) => void submit(event)} noValidate>
          {register && (
            <Input
              label="Họ và tên"
              autoComplete="name"
              value={name}
              error={errors.name}
              onChange={(e) => setName(e.target.value)}
            />
          )}
          <Input
            label="Email"
            type="email"
            autoComplete="email"
            value={email}
            error={errors.email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <div className="password-field">
            <Input
              label="Mật khẩu"
              type={show ? "text" : "password"}
              autoComplete={register ? "new-password" : "current-password"}
              value={password}
              error={errors.password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <button
              type="button"
              className="icon-btn password-toggle"
              aria-label={show ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
              onClick={() => setShow((s) => !s)}
            >
              {show ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </div>
          {register && (
            <Input
              label="Xác nhận mật khẩu"
              type={show ? "text" : "password"}
              autoComplete="new-password"
              value={confirm}
              error={errors.confirm}
              onChange={(e) => setConfirm(e.target.value)}
            />
          )}
          <div aria-live="polite">
            {error && (
              <p className="error-banner" role="alert">
                {error}
              </p>
            )}
          </div>
          <Button className="w-full" loading={busy} type="submit">
            {register ? "Đăng ký" : "Đăng nhập"}
            <ArrowRight size={17} />
          </Button>
        </form>
        <p className="auth-switch">
          {register ? "Đã có tài khoản?" : "Bạn mới đến với NÉT?"}{" "}
          <Link to={register ? "/login" : "/register"}>
            {register ? "Đăng nhập" : "Tạo tài khoản"}
          </Link>
        </p>
      </div>
    </div>
  );
}
