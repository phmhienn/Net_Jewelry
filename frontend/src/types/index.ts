export type Category = "Nhẫn" | "Vòng cổ" | "Vòng tay" | "Bông tai";
export interface Product {
  id: string;
  name: string;
  category: Category;
  price: number;
  originalPrice?: number;
  material: string;
  brand: string;
  gender: string;
  image: string;
  images: string[];
  description: string;
  stock: number;
  rating: number;
  reviews: number;
  badge?: string;
}
export interface CartLine {
  product: Product;
  quantity: number;
}
export interface User {
  id: string;
  name: string;
  email: string;
  phone?: string;
}
export interface Address {
  name: string;
  phone: string;
  street: string;
  city: string;
  district: string;
}
export interface Order {
  id: string;
  date: string;
  total: number;
  status: string;
  items: CartLine[];
  address: Address;
  payment: string;
}
export interface ProductQuery {
  search?: string;
  category?: string;
  material?: string;
  brand?: string;
  gender?: string;
  maxPrice?: number;
  sort?: string;
  page?: number;
}
export interface ProductResult {
  items: Product[];
  total: number;
  pages: number;
}
