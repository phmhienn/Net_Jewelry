export type UserRole = "KHACH_HANG" | "NHAN_VIEN" | "QUAN_LY";
export type AccountStatus = "HOAT_DONG" | "KHOA";

export interface User {
  id: string;
  name: string;
  email: string;
  phone?: string;
  avatar?: string;
  role: UserRole;
  status?: AccountStatus;
}

export interface ProductVariant {
  id: string;
  sku?: string;
  size?: string;
  color?: string;
  price: number;
  originalPrice?: number;
  stock: number;
  reserved: number;
  available: number;
  status?: string;
}

export interface Product {
  id: string;
  sku?: string;
  name: string;
  category: string;
  categoryId?: string;
  brand: string;
  brandId?: string;
  price: number;
  originalPrice?: number;
  material: string;
  gender?: string;
  size?: string;
  color?: string;
  weight?: number;
  gemstone?: string;
  image: string;
  images: string[];
  description: string;
  stock: number;
  rating: number;
  reviews: number;
  badge?: string;
  variantId?: string;
  variants?: ProductVariant[];
  status?: string;
}

export interface CartLine {
  id?: string;
  product: Product;
  variantId?: string;
  sku?: string;
  size?: string;
  color?: string;
  quantity: number;
  unitPrice?: number;
  total?: number;
  selected?: boolean;
}

export interface Address {
  id?: string;
  name: string;
  phone: string;
  street: string;
  city: string;
  district: string;
  ward?: string;
  defaultAddress?: boolean;
}

export interface PaymentInstruction {
  bankCode?: string;
  accountNumber?: string;
  accountName?: string;
  amount: number;
  content: string;
  qrUrl?: string | null;
}

export interface PaymentSummary {
  id?: string;
  orderId?: string;
  amount?: number;
  method?: string;
  status?: string;
  paidAt?: string | null;
  instruction?: PaymentInstruction | null;
}

export interface PaymentStatus {
  orderId: string;
  orderCode: string;
  paymentStatus: string;
  paymentMethod: string;
  amount: number;
  expiresAt?: string | null;
  expired?: boolean;
}

export interface Order {
  id: string;
  databaseId?: string;
  code?: string;
  date: string;
  subtotal?: number;
  shipping?: number;
  discount?: number;
  total: number;
  status: string;
  items: CartLine[];
  address: Address;
  payment: string;
  paymentDetail?: PaymentSummary | null;
  note?: string;
  delivery?: Record<string, unknown> | null;
}

export interface ProductQuery {
  search?: string;
  categoryId?: string;
  material?: string;
  brandId?: string;
  gender?: string;
  minPrice?: number;
  maxPrice?: number;
  sort?: string;
  page?: number;
  size?: number;
}

export interface ProductResult {
  items: Product[];
  total: number;
  pages: number;
  page: number;
}

export interface CategoryItem {
  id: string;
  name: string;
  parentId?: string | null;
  description?: string;
  status?: string;
  children?: CategoryItem[];
}

export interface BrandItem {
  id: string;
  name: string;
  logo?: string;
  description?: string;
  status?: string;
}

export interface StorefrontSettings {
  brandName: string;
  descriptor: string;
  email?: string;
  freeShippingThreshold: number;
  shippingFee: number;
}

export interface Banner {
  id: string;
  title: string;
  image?: string;
  link?: string;
  sortOrder?: number;
}

export interface PageResult<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
