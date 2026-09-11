import type {
  Address,
  Banner,
  BrandItem,
  CartLine,
  CategoryItem,
  Order,
  PageResult,
  Product,
  ProductVariant,
  StorefrontSettings,
  User,
} from "../types";

export interface BackendPage<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface BackendImage {
  id: number;
  url: string;
  primaryImage?: boolean;
  sortOrder?: number;
}

export interface BackendVariant {
  id: number;
  sku?: string;
  size?: string;
  color?: string;
  price: number | string;
  stock?: number;
  reserved?: number;
  available?: number;
  salePrice?: number | string | null;
  status?: string;
}

export interface BackendProduct {
  id: number;
  sku?: string;
  name: string;
  categoryId?: number;
  category?: string;
  brandId?: number;
  brand?: string;
  description?: string;
  price: number | string;
  size?: string;
  color?: string;
  material?: string;
  weight?: number | string | null;
  gemstone?: string;
  status?: string;
  images?: BackendImage[];
  variants?: BackendVariant[];
  rating?: number;
  reviews?: number;
  salePrice?: number | string | null;
}

export interface BackendCartItem {
  id: number;
  variantId: number;
  productId: number;
  productName: string;
  sku?: string;
  size?: string;
  color?: string;
  image?: string;
  material?: string;
  quantity: number;
  unitPrice: number | string;
  total: number | string;
  available: number;
  selected?: boolean;
}

export interface BackendCart {
  id: number;
  items: BackendCartItem[];
  total: number | string;
}

export interface BackendAddress {
  id?: number;
  name: string;
  phone: string;
  city: string;
  district: string;
  ward?: string;
  street: string;
  defaultAddress?: boolean;
}

export interface BackendOrderLine {
  id: number;
  variantId: number;
  productId: number;
  productName: string;
  sku?: string;
  quantity: number;
  unitPrice: number | string;
  total: number | string;
  size?: string;
  color?: string;
}

export interface BackendPayment {
  id?: number;
  orderId?: number;
  amount?: number | string;
  method?: string;
  status?: string;
  paidAt?: string | null;
}

export interface BackendOrder {
  id: number;
  customerId?: number;
  date: string;
  subtotal?: number | string;
  shipping?: number | string;
  total: number | string;
  note?: string;
  address: BackendAddress;
  shippingMethod?: string;
  status: string;
  items?: BackendOrderLine[];
  payment?: BackendPayment | null;
  code?: string;
  discount?: number | string;
  couponCode?: string | null;
  delivery?: Record<string, unknown> | null;
}

export interface BackendUser {
  id: number;
  name: string;
  email: string;
  phone?: string;
  avatar?: string;
  role: User["role"];
  status?: User["status"];
  createdAt?: string;
}

export interface BackendLogin {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: BackendUser;
}

export interface BackendCategory {
  id: number;
  name: string;
  parentId?: number | null;
  description?: string;
  status?: string;
  children?: BackendCategory[];
}

export interface BackendBrand {
  id: number;
  name: string;
  logo?: string;
  description?: string;
  status?: string;
}

export interface BackendSettings {
  brandName?: string;
  descriptor?: string;
  email?: string;
  freeShippingThreshold?: number | string;
  shippingFee?: number | string;
}

export interface BackendBanner {
  id: number;
  title: string;
  image?: string;
  link?: string;
  sortOrder?: number;
}

export const asNumber = (value: number | string | null | undefined) =>
  Number(value ?? 0);

export const asId = (value: number | string | undefined) => String(value ?? "");

function imageUrl(images: BackendImage[] | undefined) {
  if (!images?.length) return "";
  return (
    images.find((image) => image.primaryImage)?.url ??
    [...images].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))[0]?.url ??
    ""
  );
}

export function mapVariant(input: BackendVariant): ProductVariant {
  const price = asNumber(input.salePrice ?? input.price);
  return {
    id: asId(input.id),
    sku: input.sku,
    size: input.size,
    color: input.color,
    price,
    originalPrice: input.salePrice ? asNumber(input.price) : undefined,
    stock: input.stock ?? input.available ?? 0,
    reserved: input.reserved ?? 0,
    available: input.available ?? input.stock ?? 0,
    status: input.status,
  };
}

export function mapProduct(input: BackendProduct): Product {
  const variants = (input.variants ?? []).map(mapVariant);
  const available = variants.find(
    (variant) => variant.status !== "NGUNG_BAN" && variant.available > 0,
  );
  const firstVariant =
    available ?? variants.find((variant) => variant.status !== "NGUNG_BAN") ?? variants[0];
  const price = firstVariant?.price ?? asNumber(input.salePrice ?? input.price);
  const originalPrice = firstVariant?.originalPrice ?? (input.salePrice ? asNumber(input.price) : undefined);
  const images = (input.images ?? []).map((image) => image.url).filter(Boolean);
  const stock = variants.length
    ? variants.reduce((sum, variant) => sum + Math.max(0, variant.available), 0)
    : 0;
  return {
    id: asId(input.id),
    sku: input.sku,
    name: input.name,
    category: input.category ?? "Chưa phân loại",
    categoryId: input.categoryId ? asId(input.categoryId) : undefined,
    brand: input.brand ?? "Chưa có thương hiệu",
    brandId: input.brandId ? asId(input.brandId) : undefined,
    price,
    originalPrice,
    material: input.material ?? "Chưa cập nhật",
    size: firstVariant?.size ?? input.size,
    color: firstVariant?.color ?? input.color,
    weight: input.weight == null ? undefined : asNumber(input.weight),
    gemstone: input.gemstone,
    image: imageUrl(input.images),
    images,
    description: input.description || "Sản phẩm chưa có mô tả.",
    stock,
    rating: input.rating ?? 0,
    reviews: input.reviews ?? 0,
    variantId: firstVariant?.id,
    variants,
    status: input.status,
  };
}

export function mapCartItem(item: BackendCartItem): CartLine {
  return {
    id: asId(item.id),
    variantId: asId(item.variantId),
    sku: item.sku,
    size: item.size,
    color: item.color,
    quantity: item.quantity,
    unitPrice: asNumber(item.unitPrice),
    total: asNumber(item.total),
    selected: item.selected ?? true,
    product: {
      id: asId(item.productId),
      name: item.productName,
      sku: item.sku,
      category: "",
      brand: "",
      price: asNumber(item.unitPrice),
      material: item.material ?? "",
      image: item.image ?? "",
      images: item.image ? [item.image] : [],
      description: "",
      stock: item.available,
      rating: 0,
      reviews: 0,
      variantId: asId(item.variantId),
      size: item.size,
      color: item.color,
    },
  };
}

export function mapCart(cart: BackendCart): CartLine[] {
  return (cart.items ?? []).map(mapCartItem);
}

export function mapAddress(address: BackendAddress): Address {
  return {
    id: address.id ? asId(address.id) : undefined,
    name: address.name,
    phone: address.phone,
    city: address.city,
    district: address.district,
    ward: address.ward,
    street: address.street,
    defaultAddress: address.defaultAddress,
  };
}

export function mapOrder(order: BackendOrder): Order {
  return {
    id: order.code || asId(order.id),
    code: order.code,
    date: order.date,
    subtotal: asNumber(order.subtotal),
    shipping: asNumber(order.shipping),
    discount: asNumber(order.discount),
    total: asNumber(order.total),
    status: order.status,
    items: (order.items ?? []).map((line) => ({
      id: asId(line.id),
      variantId: asId(line.variantId),
      sku: line.sku,
      size: line.size,
      color: line.color,
      quantity: line.quantity,
      unitPrice: asNumber(line.unitPrice),
      total: asNumber(line.total),
      product: {
        id: asId(line.productId),
        name: line.productName,
        sku: line.sku,
        category: "",
        brand: "",
        price: asNumber(line.unitPrice),
        material: "",
        image: "",
        images: [],
        description: "",
        stock: line.quantity,
        rating: 0,
        reviews: 0,
        variantId: asId(line.variantId),
        size: line.size,
        color: line.color,
      },
    })),
    address: mapAddress(order.address),
    payment: order.payment?.method ?? "COD",
    note: order.note,
    delivery: order.delivery,
  };
}

export function mapUser(user: BackendUser): User {
  return {
    id: asId(user.id),
    name: user.name,
    email: user.email,
    phone: user.phone,
    avatar: user.avatar,
    role: user.role,
    status: user.status,
  };
}

export function mapCategory(category: BackendCategory): CategoryItem {
  return {
    id: asId(category.id),
    name: category.name,
    parentId: category.parentId ? asId(category.parentId) : null,
    description: category.description,
    status: category.status,
    children: category.children?.map(mapCategory) ?? [],
  };
}

export function mapBrand(brand: BackendBrand): BrandItem {
  return {
    id: asId(brand.id),
    name: brand.name,
    logo: brand.logo,
    description: brand.description,
    status: brand.status,
  };
}

export function mapSettings(settings: BackendSettings | null | undefined): StorefrontSettings {
  return {
    brandName: settings?.brandName || "NÉT",
    descriptor: settings?.descriptor || "JEWELRY",
    email: settings?.email || "hello@netjewelry.example",
    freeShippingThreshold: asNumber(settings?.freeShippingThreshold) || 1500000,
    shippingFee: asNumber(settings?.shippingFee) || 30000,
  };
}

export function mapBanner(banner: BackendBanner): Banner {
  return {
    id: asId(banner.id),
    title: banner.title,
    image: banner.image,
    link: banner.link,
    sortOrder: banner.sortOrder,
  };
}

export function mapPage<T, U>(page: BackendPage<T>, map: (item: T) => U): PageResult<U> {
  return {
    content: page.content.map(map),
    page: page.page,
    size: page.size,
    totalElements: page.totalElements,
    totalPages: page.totalPages,
  };
}
