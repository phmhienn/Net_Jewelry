import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  useRef,
} from "react";
import type { ReactNode } from "react";
import type { CartLine, Product, User } from "../types";
import { isDemo } from "../data/config";
import { errorMessage } from "../utils/format";
import { readLocal, writeLocal } from "../utils/storage";
import { cartService } from "../services/cartService";
import { authService } from "../services/authService";
interface Store {
  cart: CartLine[];
  cartLoading: boolean;
  cartError: string;
  reloadCart: () => void;
  wishlist: string[];
  user: User | null;
  authLoading: boolean;
  setUser: (user: User | null) => void;
  addToCart: (product: Product, quantity?: number) => Promise<boolean>;
  setQuantity: (id: string, quantity: number) => Promise<boolean>;
  clearCart: () => void;
  toggleWishlist: (id: string) => void;
  cartOpen: boolean;
  setCartOpen: (open: boolean) => void;
  notice: { text: string; error: boolean } | null;
  notify: (text: string, error?: boolean) => void;
}
const Context = createContext<Store | null>(null);
export function StoreProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<CartLine[]>(() =>
    isDemo ? readLocal("net-cart", []) : [],
  );
  const [wishlist, setWishlist] = useState<string[]>(() =>
    readLocal("net-wishlist", []),
  );
  const [user, setUser] = useState<User | null>(null);
  const [cartOpen, setCartOpen] = useState(false);
  const [notice, setNotice] = useState<Store["notice"]>(null);
  const notify = useCallback(
    (text: string, error = false) => setNotice({ text, error }),
    [],
  );
  const [authLoading, setAuthLoading] = useState(true);
  useEffect(() => {
    let active = true;
    authService
      .me()
      .then((data) => {
        if (active) setUser(data);
      })
      .catch((e) => {
        if (active) notify(errorMessage(e), true);
      })
      .finally(() => {
        if (active) setAuthLoading(false);
      });
    return () => {
      active = false;
    };
  }, [notify]);
  const [cartLoading, setCartLoading] = useState(!isDemo);
  const [cartError, setCartError] = useState("");
  const [version, setVersion] = useState(0);
  const lock = useRef(false);
  const currentCart = useRef(cart);
  currentCart.current = cart;
  useEffect(() => {
    if (isDemo) return;
    let active = true;
    setCartLoading(true);
    setCartError("");
    cartService
      .get()
      .then((items) => {
        if (active) setCart(items);
      })
      .catch((e) => {
        if (active) setCartError(errorMessage(e));
      })
      .finally(() => {
        if (active) setCartLoading(false);
      });
    return () => {
      active = false;
    };
  }, [version, user?.id]);
  useEffect(() => {
    if (isDemo) writeLocal("net-cart", cart);
  }, [cart]);
  useEffect(() => {
    writeLocal("net-wishlist", wishlist);
  }, [wishlist]);
  useEffect(() => {
    if (!notice) return;
    const timer = setTimeout(() => setNotice(null), 4500);
    return () => clearTimeout(timer);
  }, [notice]);
  async function saveCart(items: CartLine[]) {
    if (lock.current || cartLoading || cartError) {
      notify(
        cartError || "Giỏ hàng đang được cập nhật. Vui lòng thử lại.",
        true,
      );
      return false;
    }
    lock.current = true;
    setCartLoading(true);
    try {
      const next = await cartService.save(items);
      currentCart.current = next;
      setCart(next);
      return true;
    } catch (e) {
      notify(errorMessage(e), true);
      return false;
    } finally {
      lock.current = false;
      setCartLoading(false);
    }
  }
  async function addToCart(product: Product, quantity = 1) {
    const items = currentCart.current;
    const existing = items.find((item) => item.product.id === product.id);
    if (
      !Number.isInteger(quantity) ||
      quantity < 1 ||
      (existing?.quantity ?? 0) + quantity > product.stock
    ) {
      notify(`Chỉ còn ${product.stock} sản phẩm trong kho.`, true);
      return false;
    }
    const next = existing
      ? items.map((item) =>
          item.product.id === product.id
            ? { ...item, quantity: item.quantity + quantity }
            : item,
        )
      : [...items, { product, quantity }];
    const success = await saveCart(next);
    if (success) notify(`Đã thêm ${product.name} vào giỏ hàng`);
    return success;
  }
  const setQuantity = (id: string, quantity: number) =>
    saveCart(
      currentCart.current
        .map((item) =>
          item.product.id === id
            ? {
                ...item,
                quantity: Math.max(0, Math.min(item.product.stock, quantity)),
              }
            : item,
        )
        .filter((item) => item.quantity > 0),
    );
  return (
    <Context.Provider
      value={{
        cart,
        cartLoading,
        cartError,
        reloadCart: () => setVersion((n) => n + 1),
        wishlist,
        user,
        authLoading,
        setUser,
        addToCart,
        setQuantity,
        clearCart: () => {
          currentCart.current = [];
          setCart([]);
        },
        toggleWishlist: (id) =>
          setWishlist((ids) =>
            ids.includes(id)
              ? ids.filter((value) => value !== id)
              : [...ids, id],
          ),
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
