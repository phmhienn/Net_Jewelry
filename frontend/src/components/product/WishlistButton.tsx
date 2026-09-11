import { useEffect, useState } from "react";
import { Heart } from "lucide-react";
import { Button } from "../common/Button";
import { wishlistService } from "../../services/wishlistService";
import { errorMessage } from "../../utils/format";
import { useStore } from "../../context/StoreContext";
import { isCustomer } from "../../utils/access";

export function WishlistButton({ productId }: { productId: string }) {
  const { user, notify } = useStore();
  const [saved, setSaved] = useState(false);
  const [loading, setLoading] = useState(false);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!user || !isCustomer(user)) return;
    let active = true;
    setLoading(true);
    wishlistService
      .has(productId)
      .then((exists) => {
        if (active) setSaved(exists);
      })
      .catch(() => undefined)
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [productId, user]);

  if (user && !isCustomer(user)) return null;

  return (
    <Button
      type="button"
      variant="secondary"
      className="wishlist-detail-btn"
      loading={busy || loading}
      aria-pressed={saved}
      onClick={async () => {
        if (!user) {
          notify("Vui lòng đăng nhập để lưu sản phẩm yêu thích.", true);
          return;
        }
        setBusy(true);
        try {
          if (saved) {
            await wishlistService.remove(productId);
            setSaved(false);
            notify("Đã bỏ sản phẩm khỏi yêu thích");
          } else {
            await wishlistService.add(productId);
            setSaved(true);
            notify("Đã lưu sản phẩm yêu thích");
          }
        } catch (error) {
          notify(errorMessage(error), true);
        } finally {
          setBusy(false);
        }
      }}
    >
      <Heart size={18} fill={saved ? "currentColor" : "none"} />
      {saved ? "Đã yêu thích" : "Yêu thích"}
    </Button>
  );
}
