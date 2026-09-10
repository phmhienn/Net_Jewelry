export const brand = {
  name: "NÉT",
  descriptor: "JEWELRY",
  email: "hello@netjewelry.example",
  tagline: "Đẹp từ những điều giản đơn.",
};
export const categories = ["Nhẫn", "Vòng cổ", "Vòng tay", "Bông tai"] as const;
export const isDemo = import.meta.env.VITE_DEMO_MODE !== "false";
