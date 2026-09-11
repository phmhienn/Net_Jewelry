import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";
import type { ReactNode } from "react";
import type { CartLine, Product, User } from "../types";
import { errorMessage } from "../utils/format";
import { cartService } from "../services/cartService";
import { authService } from "../services/authService";
import { AUTH_EXPIRED_EVENT } from "../services/api";
import { isCustomer } from "../utils/access";

interface Store {
  cart: CartLine[];
  cartLoading: boolean;
  cartError: string;
  reloadCart: () => void;
  user: User | null;
  authLoading: boolean;
  setUser: (user: User | null) => void;
  addToCart: (product: Product, quantity?: number) => Promise<boolean>;
  setQuantity: (id: string, quantity: number) => Promise<boolean>;
  clearCart: () => Promise<void>;
  cartOpen: boolean;
  setCartOpen: (open: boolean) => void;
  notice: { text: string; error: boolean } | null;
  notify: (text: string, error?: boolean) => void;
}

const Context = createContext<Store | null>(null);

function selectedVariant(product: Product) {
  return (
    product.variants?.find((variant) => variant.id === product.variantId) ??
    product.variants?.find((variant) => variant.available > 0) ??
    product.variants?.[0]
  );
}

export function StoreProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<CartLine[]>([]);
  const [user, setUser] = useState<User | null>(null);
  const [cartOpen, setCartOpen] = useState(false);
  const [notice, setNotice] = useState<Store["notice"]>(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [cartLoading, setCartLoading] = useState(false);
  const [cartError, setCartError] = useState("");
  const [version, setVersion] = useState(0);
  const lock = useRef(false);

  const notify = useCallback(
    (text: string, error = false) => setNotice({ text, error }),
    [],
  );

  const expireSession = useCallback(() => {
    setUser(null);
    setCart([]);
    setCartError("");
    setCartLoading(false);
    setCartOpen(false);
    setAuthLoading(false);
  }, []);

  useEffect(() => {
    if (typeof window === "undefined") return undefined;
    window.addEventListener(AUTH_EXPIRED_EVENT, expireSession);
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, expireSession);
  }, [expireSession]);

  useEffect(() => {
    let active = true;
    authService
      .me()
      .then((data) => {
        if (active) setUser(data);
      })
      .catch(() => {
        if (active) setUser(null);
      })
      .finally(() => {
        if (active) setAuthLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    if (!user || !isCustomer(user)) {
      setCart([]);
      setCartError("");
      setCartLoading(false);
      return;
    }
    let active = true;
    setCartLoading(true);
    setCartError("");
    cartService
      .get()
      .then((items) => {
        if (active) setCart(items);
      })
      .catch((error) => {
        if (active) setCartError(errorMessage(error));
      })
      .finally(() => {
        if (active) setCartLoading(false);
      });
    return () => {
      active = false;
    };
  }, [version, user?.id, user?.role]);

  useEffect(() => {
    if (!notice) return;
    const timer = setTimeout(() => setNotice(null), 4500);
    return () => clearTimeout(timer);
  }, [notice]);

  async function mutateCart(action: () => Promise<CartLine[]>) {
    if (!user) {
      notify("Vui lòng đăng nhập để sử dụng giỏ hàng.", true);
      return false;
    }
    if (!isCustomer(user)) {
      notify("Tài khoản nhân viên/quản lý không dùng chức năng mua hàng.", true);
      return false;
    }
    if (lock.current) return false;
    lock.current = true;
    setCartLoading(true);
    setCartError("");
    try {
      const next = await action();
      setCart(next);
      return true;
    } catch (error) {
      notify(errorMessage(error), true);
      return false;
    } finally {
      lock.current = false;
      setCartLoading(false);
    }
  }

  async function addToCart(product: Product, quantity = 1) {
    const variant = selectedVariant(product);
    if (!variant?.id) {
      notify("Sản phẩm chưa có biến thể để đặt hàng.", true);
      return false;
    }
    if (variant.available < quantity || product.stock < quantity) {
      notify(`Chỉ còn ${Math.max(0, variant.available || product.stock)} sản phẩm trong kho.`, true);
      return false;
    }
    const success = await mutateCart(() => cartService.add(variant.id, quantity));
    if (success) notify(`Đã thêm ${product.name} vào giỏ hàng`);
    return success;
  }

  async function setQuantity(id: string, quantity: number) {
    const item = cart.find((line) => line.id === id || line.product.id === id);
    if (!item?.id) return false;
    return mutateCart(() =>
      quantity <= 0 ? cartService.remove(item.id!) : cartService.update(item.id!, quantity),
    );
  }

  async function clearCart() {
    if (!user || !isCustomer(user)) {
      setCart([]);
      return;
    }
    await mutateCart(() => cartService.clear());
  }

  return (
    <Context.Provider
      value={{
        cart,
        cartLoading,
        cartError,
        reloadCart: () => setVersion((n) => n + 1),
        user,
        authLoading,
        setUser,
        addToCart,
        setQuantity,
        clearCart,
        cartOpen,
        setCartOpen,
        notice,
        notify,
      }}
    >
      {children}
    </Context.Provider>
  );
}

export function useStore() {
  const context = useContext(Context);
  if (!context) throw new Error("StoreProvider is required");
  return context;
}
