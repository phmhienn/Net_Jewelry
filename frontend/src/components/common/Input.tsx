import { useId } from "react";
import type { InputHTMLAttributes } from "react";
interface Props extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}
export function Input({ label, error, id, ...props }: Props) {
  const generated = useId();
  const fieldId = id ?? generated;
  return (
    <div className="field">
      <label htmlFor={fieldId}>{label}</label>
      <input
        {...props}
        id={fieldId}
        aria-invalid={!!error}
        aria-describedby={error ? `${fieldId}-error` : undefined}
      />
      {error && (
        <small role="alert" id={`${fieldId}-error`} className="error-text">
          {error}
        </small>
      )}
    </div>
  );
}
