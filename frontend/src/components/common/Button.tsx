import type { ButtonHTMLAttributes } from "react";
import { LoaderCircle } from "lucide-react";
interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "ghost";
  loading?: boolean;
}
export function Button({
  children,
  variant = "primary",
  loading,
  className = "",
  disabled,
  ...props
}: Props) {
  return (
    <button
      {...props}
      disabled={disabled || loading}
      className={`btn btn-${variant} ${className}`}
      aria-busy={loading || undefined}
    >
      {loading && <LoaderCircle size={17} className="spin" />}
      {children}
    </button>
  );
}
