import { Minus, Plus } from "lucide-react";
export function QuantityControl({
  value,
  max,
  onChange,
  label = "Số lượng",
}: {
  value: number;
  max: number;
  onChange: (value: number) => void;
  label?: string;
}) {
  return (
    <div className="quantity-control" role="group" aria-label={label}>
      <button
        aria-label={`Giảm ${label.toLowerCase()}`}
        disabled={value <= 1}
        onClick={() => onChange(value - 1)}
      >
        <Minus size={15} />
      </button>
      <span aria-live="polite">{value}</span>
      <button
        aria-label={`Tăng ${label.toLowerCase()}`}
        disabled={value >= max}
        onClick={() => onChange(value + 1)}
      >
        <Plus size={15} />
      </button>
    </div>
  );
}
