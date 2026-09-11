import type { ReactNode } from "react";
import { EmptyState } from "./Feedback";

export function DatabaseContent({
  empty,
  children,
}: {
  empty: boolean;
  children: ReactNode;
}) {
  if (empty) {
    return (
      <EmptyState
        title="Chưa có dữ liệu"
        description="Dữ liệu đang trống."
      />
    );
  }
  return children;
}
