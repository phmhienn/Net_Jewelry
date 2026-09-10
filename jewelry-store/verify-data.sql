-- Read-only checks for database.sql (25 tables). Every violations value should be 0.
SELECT '01_order_totals' AS rule, COUNT(*) AS violations FROM don_hang
WHERE tong_tien <> COALESCE((SELECT SUM(thanh_tien) FROM chi_tiet_don_hang WHERE don_hang_id=don_hang.id),0)
   OR tong_thanh_toan <> tong_tien-tien_giam+phi_van_chuyen
UNION ALL
SELECT '02_cart_totals', COUNT(*) FROM gio_hang
WHERE tong_tien <> COALESCE((SELECT SUM(thanh_tien) FROM chi_tiet_gio_hang WHERE gio_hang_id=gio_hang.id),0)
UNION ALL
SELECT '03_payment_amount', COUNT(*) FROM thanh_toan tt JOIN don_hang dh ON dh.id=tt.don_hang_id WHERE tt.so_tien<>dh.tong_thanh_toan
UNION ALL
SELECT '04_order_without_payment', COUNT(*) FROM don_hang dh WHERE NOT EXISTS (SELECT 1 FROM thanh_toan WHERE don_hang_id=dh.id)
UNION ALL
SELECT '05_reserved_stock', COUNT(*) FROM ton_kho tk WHERE tk.so_luong_dat <>
 (SELECT COALESCE(SUM(ct.so_luong),0) FROM chi_tiet_don_hang ct JOIN don_hang dh ON dh.id=ct.don_hang_id
  WHERE ct.bien_the_id=tk.bien_the_id AND dh.trang_thai IN ('CHO_XAC_NHAN','DA_XAC_NHAN','DANG_XU_LY'))
UNION ALL
SELECT '06_order_customer_role', COUNT(*) FROM don_hang dh JOIN tai_khoan tk ON tk.id=dh.tai_khoan_id JOIN vai_tro vt ON vt.id=tk.vai_tro_id WHERE vt.ten_vai_tro<>'KHACH_HANG'
UNION ALL
SELECT '07_multiple_default_addresses', COUNT(*) FROM (SELECT tai_khoan_id FROM dia_chi GROUP BY tai_khoan_id HAVING SUM(mac_dinh)<>1) addresses
UNION ALL
SELECT '08_multiple_primary_images', COUNT(*) FROM (SELECT san_pham_id FROM hinh_anh_san_pham GROUP BY san_pham_id HAVING SUM(la_anh_chinh)<>1) images
UNION ALL
SELECT '09_coupon_quota', COUNT(*) FROM ma_giam_gia mgg WHERE so_luong_da_dung<0 OR (so_luong IS NOT NULL AND so_luong_da_dung>so_luong)
 OR so_luong_da_dung <> (SELECT COUNT(*) FROM su_dung_ma_giam_gia sd JOIN don_hang dh ON dh.id=sd.don_hang_id WHERE sd.ma_giam_gia_id=mgg.id AND dh.trang_thai<>'DA_HUY')
UNION ALL
SELECT '10_coupon_values', COUNT(*) FROM ma_giam_gia WHERE (loai_giam='PHAN_TRAM' AND gia_tri_giam>100) OR giam_toi_da<=0
UNION ALL
SELECT '11_coupon_usage', COUNT(*) FROM su_dung_ma_giam_gia sd JOIN don_hang dh ON dh.id=sd.don_hang_id
WHERE sd.tai_khoan_id<>dh.tai_khoan_id OR sd.so_tien_giam<>dh.tien_giam OR sd.ma_giam_gia_id<>dh.ma_giam_gia_id
UNION ALL
SELECT '12_review_purchase', COUNT(*) FROM danh_gia dg WHERE dg.don_hang_id IS NULL OR NOT EXISTS
 (SELECT 1 FROM don_hang dh JOIN chi_tiet_don_hang ct ON ct.don_hang_id=dh.id JOIN bien_the_san_pham bt ON bt.id=ct.bien_the_id
  WHERE dh.id=dg.don_hang_id AND dh.tai_khoan_id=dg.tai_khoan_id AND dh.trang_thai='HOAN_THANH' AND bt.san_pham_id=dg.san_pham_id)
UNION ALL
SELECT '13_payment_confirmation', COUNT(*) FROM thanh_toan tt WHERE trang_thai='CONFIRMED' AND
 (thoi_gian_thanh_toan IS NULL OR NOT EXISTS(SELECT 1 FROM giao_dich gd WHERE gd.thanh_toan_id=tt.id AND gd.trang_thai='THANH_CONG'))
UNION ALL
SELECT '14_duplicate_successful_payment', COUNT(*) FROM (SELECT thanh_toan_id FROM giao_dich WHERE trang_thai='THANH_CONG' GROUP BY thanh_toan_id HAVING COUNT(*)>1) transactions
UNION ALL
SELECT '15_transaction_amount', COUNT(*) FROM giao_dich gd JOIN thanh_toan tt ON tt.id=gd.thanh_toan_id WHERE gd.so_tien<>tt.so_tien OR gd.phuong_thuc<>tt.phuong_thuc
UNION ALL
SELECT '16_delivery', COUNT(*) FROM don_hang dh LEFT JOIN giao_hang gh ON gh.don_hang_id=dh.id WHERE gh.id IS NULL OR gh.phi_van_chuyen<>dh.phi_van_chuyen
 OR (dh.trang_thai='HOAN_THANH' AND (gh.trang_thai<>'DA_GIAO' OR gh.ngay_nhan IS NULL))
UNION ALL
SELECT '17_completed_order', COUNT(*) FROM don_hang WHERE trang_thai='HOAN_THANH' AND ngay_hoan_thanh IS NULL
UNION ALL
SELECT '18_stock_threshold', COUNT(*) FROM ton_kho WHERE nguong_canh_bao<0;
