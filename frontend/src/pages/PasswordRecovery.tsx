import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { api } from "../services/api";
import { Input } from "../components/common/Input";
import { Button } from "../components/common/Button";
import { validEmail } from "../utils/validation";
import { errorMessage } from "../utils/format";

export default function PasswordRecovery({ reset = false }: { reset?: boolean }) {
  const [params] = useSearchParams();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  return (
    <div className="container page auth-page">
      <div className="auth-card">
        <div className="eyebrow">TÀI KHOẢN</div>
        <h1>{reset ? "Đặt lại mật khẩu" : "Quên mật khẩu"}</h1>
        <p className="auth-subtitle">{reset ? "Nhập mật khẩu mới cho liên kết đặt lại mật khẩu." : "Nhập email tài khoản để nhận hướng dẫn đặt lại mật khẩu nếu hệ thống email được bật."}</p>
        <form
          onSubmit={async (event) => {
            event.preventDefault();
            setError("");
            setMessage("");
            if (!reset && !validEmail(email)) {
              setError("Vui lòng nhập email hợp lệ.");
              return;
            }
            if (reset && password.length < 8) {
              setError("Mật khẩu cần ít nhất 8 ký tự.");
              return;
            }
            setBusy(true);
            try {
              if (reset) await api.post("/auth/reset-password", { token: params.get("token") ?? "", password });
              else await api.post("/auth/forgot-password", { email });
              setMessage(reset ? "Đã đặt lại mật khẩu." : "Nếu email tồn tại, hệ thống sẽ gửi hướng dẫn đặt lại mật khẩu.");
            } catch (err) {
              setError(errorMessage(err));
            } finally {
              setBusy(false);
            }
          }}
        >
          {reset ? <Input label="Mật khẩu mới" type="password" value={password} onChange={(e) => setPassword(e.target.value)} /> : <Input label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />}
          {error && <p className="error-banner" role="alert">{error}</p>}
          {message && <p className="info-banner" role="status">{message}</p>}
          <Button type="submit" loading={busy}>{reset ? "Lưu mật khẩu" : "Gửi hướng dẫn"}<ArrowRight size={17} /></Button>
        </form>
        <p className="auth-switch"><Link to="/login">Quay lại đăng nhập</Link></p>
      </div>
    </div>
  );
}
