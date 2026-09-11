-- ============================================================
-- NÉT Jewelry · dữ liệu kiểm tra có thể import lại
-- Dùng với schema database.sql (MySQL 8.x).
-- File này KHÔNG tạo/xóa database hoặc bảng.
-- Các bản ghi dùng mã nghiệp vụ NET/ORD/TXN để kiểm tra giao diện và API.
-- ============================================================

USE jewelry_store;
SET NAMES utf8mb4;
START TRANSACTION;


-- Dọn dữ liệu kiểm tra cũ từng dùng tiền tố DEMO/demo_ để website không còn hiển thị mã demo.
DELETE FROM hinh_anh_danh_gia WHERE danh_gia_id IN (SELECT id FROM danh_gia WHERE don_hang_id IN (SELECT id FROM don_hang WHERE ma_don_hang LIKE 'DEMO-%'));
DELETE FROM danh_gia WHERE don_hang_id IN (SELECT id FROM don_hang WHERE ma_don_hang LIKE 'DEMO-%');
DELETE FROM hinh_anh_danh_gia WHERE danh_gia_id IN (SELECT id FROM danh_gia WHERE san_pham_id IN (SELECT id FROM san_pham WHERE ma_sku LIKE 'DEMO-%'));
DELETE FROM danh_gia WHERE san_pham_id IN (SELECT id FROM san_pham WHERE ma_sku LIKE 'DEMO-%');
DELETE FROM giao_hang WHERE don_hang_id IN (SELECT id FROM don_hang WHERE ma_don_hang LIKE 'DEMO-%');
DELETE FROM giao_dich WHERE ma_giao_dich_thanh_toan LIKE 'DEMO-%';
DELETE FROM thanh_toan WHERE don_hang_id IN (SELECT id FROM don_hang WHERE ma_don_hang LIKE 'DEMO-%');
DELETE FROM su_dung_ma_giam_gia WHERE don_hang_id IN (SELECT id FROM don_hang WHERE ma_don_hang LIKE 'DEMO-%');
DELETE FROM chi_tiet_don_hang WHERE don_hang_id IN (SELECT id FROM don_hang WHERE ma_don_hang LIKE 'DEMO-%');
DELETE FROM chi_tiet_don_hang WHERE bien_the_id IN (SELECT id FROM bien_the_san_pham WHERE sku LIKE 'DEMO-%');
DELETE FROM don_hang WHERE ma_don_hang LIKE 'DEMO-%';
DELETE FROM yeu_thich WHERE san_pham_id IN (SELECT id FROM san_pham WHERE ma_sku LIKE 'DEMO-%') OR tai_khoan_id IN (SELECT id FROM tai_khoan WHERE ten_dang_nhap LIKE 'demo\_%');
DELETE FROM chi_tiet_gio_hang WHERE gio_hang_id IN (SELECT id FROM gio_hang WHERE tai_khoan_id IN (SELECT id FROM tai_khoan WHERE ten_dang_nhap LIKE 'demo\_%')) OR bien_the_id IN (SELECT id FROM bien_the_san_pham WHERE sku LIKE 'DEMO-%');
DELETE FROM gio_hang WHERE tai_khoan_id IN (SELECT id FROM tai_khoan WHERE ten_dang_nhap LIKE 'demo\_%');
DELETE FROM lich_su_kho WHERE bien_the_id IN (SELECT id FROM bien_the_san_pham WHERE sku LIKE 'DEMO-%') OR ly_do LIKE 'DEMO:%';
DELETE FROM ton_kho WHERE bien_the_id IN (SELECT id FROM bien_the_san_pham WHERE sku LIKE 'DEMO-%');
DELETE FROM hinh_anh_san_pham WHERE san_pham_id IN (SELECT id FROM san_pham WHERE ma_sku LIKE 'DEMO-%');
DELETE FROM bien_the_san_pham WHERE sku LIKE 'DEMO-%';
DELETE FROM san_pham WHERE ma_sku LIKE 'DEMO-%';
DELETE FROM ma_giam_gia WHERE ma_code LIKE 'DEMO%';
DELETE FROM dia_chi WHERE tai_khoan_id IN (SELECT id FROM tai_khoan WHERE ten_dang_nhap LIKE 'demo\_%');
DELETE FROM tai_khoan WHERE ten_dang_nhap LIKE 'demo\_%';

-- Vai trò và ba tài khoản kiểm tra. Mật khẩu của cả ba là: 1236qtth
INSERT INTO vai_tro (ten_vai_tro, mo_ta)
VALUES
  ('KHACH_HANG', 'Khách hàng sử dụng website để mua sản phẩm'),
  ('NHAN_VIEN', 'Nhân viên quản lý sản phẩm, kho, đơn hàng và giao dịch'),
  ('QUAN_LY', 'Quản lý toàn bộ hệ thống')
ON DUPLICATE KEY UPDATE mo_ta = VALUES(mo_ta);

INSERT INTO tai_khoan
  (ho_ten, email, so_dien_thoai, ten_dang_nhap, mat_khau, vai_tro_id, trang_thai)
SELECT
  'Khách hàng kiểm tra', 'customer01@netjewelry.local', '0900000001',
  'customer01', '$2a$10$yAT2VWXRaeeCL2pLjPHBoeHP3aLbc05dvN9kdNWNKcrnr3dgkDcai',
  id, 'HOAT_DONG'
FROM vai_tro WHERE ten_vai_tro = 'KHACH_HANG'
ON DUPLICATE KEY UPDATE
  ho_ten = VALUES(ho_ten), so_dien_thoai = VALUES(so_dien_thoai),
  mat_khau = VALUES(mat_khau), vai_tro_id = VALUES(vai_tro_id), trang_thai = 'HOAT_DONG';

INSERT INTO tai_khoan
  (ho_ten, email, so_dien_thoai, ten_dang_nhap, mat_khau, vai_tro_id, trang_thai)
SELECT
  'Nhân viên kiểm tra', 'staff01@netjewelry.local', '0900000002',
  'staff01', '$2a$10$yAT2VWXRaeeCL2pLjPHBoeHP3aLbc05dvN9kdNWNKcrnr3dgkDcai',
  id, 'HOAT_DONG'
FROM vai_tro WHERE ten_vai_tro = 'NHAN_VIEN'
ON DUPLICATE KEY UPDATE
  ho_ten = VALUES(ho_ten), so_dien_thoai = VALUES(so_dien_thoai),
  mat_khau = VALUES(mat_khau), vai_tro_id = VALUES(vai_tro_id), trang_thai = 'HOAT_DONG';

INSERT INTO tai_khoan
  (ho_ten, email, so_dien_thoai, ten_dang_nhap, mat_khau, vai_tro_id, trang_thai)
SELECT
  'Quản lý kiểm tra', 'manager01@netjewelry.local', '0900000003',
  'manager01', '$2a$10$yAT2VWXRaeeCL2pLjPHBoeHP3aLbc05dvN9kdNWNKcrnr3dgkDcai',
  id, 'HOAT_DONG'
FROM vai_tro WHERE ten_vai_tro = 'QUAN_LY'
ON DUPLICATE KEY UPDATE
  ho_ten = VALUES(ho_ten), so_dien_thoai = VALUES(so_dien_thoai),
  mat_khau = VALUES(mat_khau), vai_tro_id = VALUES(vai_tro_id), trang_thai = 'HOAT_DONG';

SET @customer01_id := (SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'customer01');
SET @staff01_id := (SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'staff01');
SET @manager01_id := (SELECT id FROM tai_khoan WHERE ten_dang_nhap = 'manager01');

INSERT INTO dia_chi
  (tai_khoan_id, ho_ten_nguoi_nhan, so_dien_thoai, tinh_thanh, quan_huyen, phuong_xa, dia_chi_chi_tiet, mac_dinh)
SELECT @customer01_id, 'Khách hàng kiểm tra', '0900000001', 'TP. Hồ Chí Minh', 'Quận 1', 'Phường Bến Nghé', '12 Lê Lợi', TRUE
WHERE NOT EXISTS (
  SELECT 1 FROM dia_chi WHERE tai_khoan_id = @customer01_id AND dia_chi_chi_tiet = '12 Lê Lợi'
);

-- Danh mục và thương hiệu.
INSERT INTO danh_muc (ten_danh_muc, mo_ta, trang_thai)
VALUES
  ('Nhẫn', 'Nhẫn cho những dịp đáng nhớ.', 'HOAT_DONG'),
  ('Dây chuyền', 'Dây chuyền và mặt dây đeo hằng ngày.', 'HOAT_DONG'),
  ('Lắc tay', 'Lắc tay và vòng tay tinh giản.', 'HOAT_DONG'),
  ('Bông tai', 'Bông tai cho nhiều phong cách.', 'HOAT_DONG')
ON DUPLICATE KEY UPDATE mo_ta = VALUES(mo_ta), trang_thai = 'HOAT_DONG';

INSERT INTO thuong_hieu (ten_thuong_hieu, logo, mo_ta, trang_thai)
VALUES
  ('NÉT Jewelry', NULL, 'Thiết kế trang sức tối giản cho mỗi ngày.', 'HOAT_DONG'),
  ('An Gia', NULL, 'Trang sức vàng 18K và đá tự nhiên.', 'HOAT_DONG'),
  ('Aurora', NULL, 'Thiết kế bạc hiện đại, dễ phối.', 'HOAT_DONG')
ON DUPLICATE KEY UPDATE mo_ta = VALUES(mo_ta), trang_thai = 'HOAT_DONG';

INSERT INTO gia_vang (loai_vang, don_vi, ngay_ap_dung, gia_mua, gia_ban)
VALUES
  ('Vàng 24K', 'VND/chỉ', CURDATE(), 8500000.00, 8750000.00),
  ('Vàng 18K', 'VND/chỉ', CURDATE(), 6300000.00, 6600000.00)
ON DUPLICATE KEY UPDATE gia_mua = VALUES(gia_mua), gia_ban = VALUES(gia_ban), ngay_cap_nhat = CURRENT_TIMESTAMP(6);

SET @cat_ring := (SELECT id FROM danh_muc WHERE ten_danh_muc = 'Nhẫn');
SET @cat_necklace := (SELECT id FROM danh_muc WHERE ten_danh_muc = 'Dây chuyền');
SET @cat_bracelet := (SELECT id FROM danh_muc WHERE ten_danh_muc = 'Lắc tay');
SET @cat_earring := (SELECT id FROM danh_muc WHERE ten_danh_muc = 'Bông tai');
SET @brand_net := (SELECT id FROM thuong_hieu WHERE ten_thuong_hieu = 'NÉT Jewelry');
SET @brand_angia := (SELECT id FROM thuong_hieu WHERE ten_thuong_hieu = 'An Gia');
SET @brand_aurora := (SELECT id FROM thuong_hieu WHERE ten_thuong_hieu = 'Aurora');

-- Sản phẩm / biến thể / tồn kho. Giá được lưu bằng VND.
INSERT INTO san_pham
  (ma_sku, ten_san_pham, danh_muc_id, thuong_hieu_id, mo_ta, gia, gia_khuyen_mai, size, mau_sac, chat_lieu, trong_luong, da_quy, trang_thai)
VALUES
  ('NET-RING-001', 'Nhẫn vàng NÉT Mảnh', @cat_ring, @brand_net, 'Nhẫn vàng 18K dáng mảnh, hoàn thiện đánh bóng nhẹ.', 3450000.00, 3150000.00, '12', 'Vàng', 'Vàng 18K', 0.780, NULL, 'DANG_BAN'),
  ('NET-RING-002', 'Nhẫn đá Moonstone', @cat_ring, @brand_angia, 'Nhẫn vàng 18K đính moonstone xanh dịu.', 5200000.00, NULL, '14', 'Vàng', 'Vàng 18K', 1.080, 'Moonstone', 'DANG_BAN'),
  ('NET-NECK-001', 'Dây chuyền NÉT Tròn', @cat_necklace, @brand_net, 'Dây chuyền vàng 18K với mặt tròn tối giản.', 4890000.00, 4590000.00, '45 cm', 'Vàng', 'Vàng 18K', 1.250, NULL, 'DANG_BAN'),
  ('NET-NECK-002', 'Dây chuyền Bạc Aurora', @cat_necklace, @brand_aurora, 'Dây bạc 925 nhẹ, phù hợp sử dụng mỗi ngày.', 1250000.00, NULL, '42 cm', 'Bạc', 'Bạc 925', 2.100, NULL, 'DANG_BAN'),
  ('NET-BRACELET-001', 'Lắc tay vàng Mây', @cat_bracelet, @brand_angia, 'Lắc tay vàng 18K liên kết mềm mại.', 6750000.00, NULL, '16 cm', 'Vàng', 'Vàng 18K', 1.650, NULL, 'DANG_BAN'),
  ('NET-EARRING-001', 'Bông tai Ngọc trai NÉT', @cat_earring, @brand_net, 'Bông tai vàng 18K đính ngọc trai nước ngọt.', 2850000.00, 2650000.00, NULL, 'Vàng', 'Vàng 18K', 0.620, 'Ngọc trai', 'DANG_BAN')
ON DUPLICATE KEY UPDATE
  ten_san_pham = VALUES(ten_san_pham), danh_muc_id = VALUES(danh_muc_id), thuong_hieu_id = VALUES(thuong_hieu_id),
  mo_ta = VALUES(mo_ta), gia = VALUES(gia), gia_khuyen_mai = VALUES(gia_khuyen_mai),
  size = VALUES(size), mau_sac = VALUES(mau_sac), chat_lieu = VALUES(chat_lieu), trong_luong = VALUES(trong_luong),
  da_quy = VALUES(da_quy), trang_thai = 'DANG_BAN';

SET @p_ring_1 := (SELECT id FROM san_pham WHERE ma_sku = 'NET-RING-001');
SET @p_ring_2 := (SELECT id FROM san_pham WHERE ma_sku = 'NET-RING-002');
SET @p_neck_1 := (SELECT id FROM san_pham WHERE ma_sku = 'NET-NECK-001');
SET @p_neck_2 := (SELECT id FROM san_pham WHERE ma_sku = 'NET-NECK-002');
SET @p_bracelet := (SELECT id FROM san_pham WHERE ma_sku = 'NET-BRACELET-001');
SET @p_earring := (SELECT id FROM san_pham WHERE ma_sku = 'NET-EARRING-001');

INSERT INTO hinh_anh_san_pham (san_pham_id, duong_dan, la_anh_chinh, thu_tu)
SELECT @p_ring_1, 'https://images.unsplash.com/photo-1605100804763-247f67b3557e?auto=format&fit=crop&w=900&q=80', TRUE, 1
WHERE NOT EXISTS (SELECT 1 FROM hinh_anh_san_pham WHERE san_pham_id = @p_ring_1 AND thu_tu = 1);
INSERT INTO hinh_anh_san_pham (san_pham_id, duong_dan, la_anh_chinh, thu_tu)
SELECT @p_ring_2, 'https://images.unsplash.com/photo-1603561596112-db1d434a8d38?auto=format&fit=crop&w=900&q=80', TRUE, 1
WHERE NOT EXISTS (SELECT 1 FROM hinh_anh_san_pham WHERE san_pham_id = @p_ring_2 AND thu_tu = 1);
INSERT INTO hinh_anh_san_pham (san_pham_id, duong_dan, la_anh_chinh, thu_tu)
SELECT @p_neck_1, 'https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?auto=format&fit=crop&w=900&q=80', TRUE, 1
WHERE NOT EXISTS (SELECT 1 FROM hinh_anh_san_pham WHERE san_pham_id = @p_neck_1 AND thu_tu = 1);
INSERT INTO hinh_anh_san_pham (san_pham_id, duong_dan, la_anh_chinh, thu_tu)
SELECT @p_neck_2, 'https://images.unsplash.com/photo-1611085583191-a3b181a88401?auto=format&fit=crop&w=900&q=80', TRUE, 1
WHERE NOT EXISTS (SELECT 1 FROM hinh_anh_san_pham WHERE san_pham_id = @p_neck_2 AND thu_tu = 1);
INSERT INTO hinh_anh_san_pham (san_pham_id, duong_dan, la_anh_chinh, thu_tu)
SELECT @p_bracelet, 'https://images.unsplash.com/photo-1617038260897-41a1f14a8ca0?auto=format&fit=crop&w=900&q=80', TRUE, 1
WHERE NOT EXISTS (SELECT 1 FROM hinh_anh_san_pham WHERE san_pham_id = @p_bracelet AND thu_tu = 1);
INSERT INTO hinh_anh_san_pham (san_pham_id, duong_dan, la_anh_chinh, thu_tu)
SELECT @p_earring, 'https://images.unsplash.com/photo-1635767798638-3e25273a8236?auto=format&fit=crop&w=900&q=80', TRUE, 1
WHERE NOT EXISTS (SELECT 1 FROM hinh_anh_san_pham WHERE san_pham_id = @p_earring AND thu_tu = 1);

INSERT INTO bien_the_san_pham (san_pham_id, sku, size, mau_sac, gia, gia_khuyen_mai, trang_thai)
VALUES
  (@p_ring_1, 'NET-RING-001-12', '12', 'Vàng', 3450000.00, 3150000.00, 'DANG_BAN'),
  (@p_ring_2, 'NET-RING-002-14', '14', 'Vàng', 5200000.00, NULL, 'DANG_BAN'),
  (@p_neck_1, 'NET-NECK-001-45', '45 cm', 'Vàng', 4890000.00, 4590000.00, 'DANG_BAN'),
  (@p_neck_2, 'NET-NECK-002-42', '42 cm', 'Bạc', 1250000.00, NULL, 'DANG_BAN'),
  (@p_bracelet, 'NET-BRACELET-001-16', '16 cm', 'Vàng', 6750000.00, NULL, 'DANG_BAN'),
  (@p_earring, 'NET-EARRING-001', NULL, 'Vàng', 2850000.00, 2650000.00, 'DANG_BAN')
ON DUPLICATE KEY UPDATE
  gia = VALUES(gia), gia_khuyen_mai = VALUES(gia_khuyen_mai), trang_thai = 'DANG_BAN';

SET @v_ring_1 := (SELECT id FROM bien_the_san_pham WHERE sku = 'NET-RING-001-12');
SET @v_ring_2 := (SELECT id FROM bien_the_san_pham WHERE sku = 'NET-RING-002-14');
SET @v_neck_1 := (SELECT id FROM bien_the_san_pham WHERE sku = 'NET-NECK-001-45');
SET @v_neck_2 := (SELECT id FROM bien_the_san_pham WHERE sku = 'NET-NECK-002-42');
SET @v_bracelet := (SELECT id FROM bien_the_san_pham WHERE sku = 'NET-BRACELET-001-16');
SET @v_earring := (SELECT id FROM bien_the_san_pham WHERE sku = 'NET-EARRING-001');

INSERT INTO ton_kho (bien_the_id, so_luong_ton, so_luong_dat, nguong_canh_bao)
VALUES
  (@v_ring_1, 24, 0, 5), (@v_ring_2, 12, 0, 4), (@v_neck_1, 16, 0, 4),
  (@v_neck_2, 30, 0, 6), (@v_bracelet, 8, 0, 3), (@v_earring, 3, 0, 4)
ON DUPLICATE KEY UPDATE
  so_luong_ton = VALUES(so_luong_ton), so_luong_dat = VALUES(so_luong_dat), nguong_canh_bao = VALUES(nguong_canh_bao);

INSERT INTO lich_su_kho (bien_the_id, tai_khoan_id, loai, thay_doi_ton, thay_doi_dat, ton_sau, dat_sau, ly_do)
SELECT @v_ring_1, @staff01_id, 'NHAP', 24, 0, 24, 0, 'Tạo tồn kho ban đầu'
WHERE NOT EXISTS (SELECT 1 FROM lich_su_kho WHERE bien_the_id = @v_ring_1 AND ly_do = 'Tạo tồn kho ban đầu');

-- Giỏ hàng, yêu thích và mã giảm giá để kiểm tra khu vực khách hàng.
INSERT INTO gio_hang (tai_khoan_id, tong_tien)
VALUES (@customer01_id, 4590000.00)
ON DUPLICATE KEY UPDATE tong_tien = VALUES(tong_tien);
SET @cart_id := (SELECT id FROM gio_hang WHERE tai_khoan_id = @customer01_id);
INSERT INTO chi_tiet_gio_hang (gio_hang_id, bien_the_id, so_luong, don_gia, thanh_tien, da_chon)
VALUES (@cart_id, @v_neck_1, 1, 4590000.00, 4590000.00, TRUE)
ON DUPLICATE KEY UPDATE so_luong = VALUES(so_luong), don_gia = VALUES(don_gia), thanh_tien = VALUES(thanh_tien), da_chon = TRUE;
INSERT IGNORE INTO yeu_thich (tai_khoan_id, san_pham_id) VALUES (@customer01_id, @p_earring);

INSERT INTO ma_giam_gia
  (ma_code, ten_ma, loai_giam, gia_tri_giam, don_hang_toi_thieu, giam_toi_da, so_luong, so_luong_da_dung, ngay_bat_dau, ngay_ket_thuc, trang_thai)
VALUES
  ('NET100K', 'Giảm 100.000đ cho đơn từ 3 triệu', 'SO_TIEN', 100000.00, 3000000.00, NULL, 100, 1, '2026-01-01 00:00:00', '2027-12-31 23:59:59', 'HOAT_DONG')
ON DUPLICATE KEY UPDATE
  ten_ma = VALUES(ten_ma), loai_giam = VALUES(loai_giam), gia_tri_giam = VALUES(gia_tri_giam),
  don_hang_toi_thieu = VALUES(don_hang_toi_thieu), so_luong = VALUES(so_luong), trang_thai = 'HOAT_DONG';
SET @coupon_id := (SELECT id FROM ma_giam_gia WHERE ma_code = 'NET100K');

-- Một đơn hoàn thành, thanh toán COD và giao hàng đã giao để kiểm tra lịch sử / báo cáo.
INSERT INTO don_hang
  (ma_don_hang, tai_khoan_id, nhan_vien_xu_ly_id, ma_giam_gia_id, ngay_dat, trang_thai, tong_tien, tien_giam, phi_van_chuyen, tong_thanh_toan, ho_ten_nguoi_nhan, so_dien_thoai_nguoi_nhan, dia_chi_giao_hang, phuong_thuc_van_chuyen, ghi_chu, idempotency_key, request_hash, ngay_xac_nhan, ngay_hoan_thanh)
VALUES
  ('ORD-20260815-001', @customer01_id, @staff01_id, @coupon_id, '2026-08-15 09:30:00', 'HOAN_THANH', 3450000.00, 100000.00, 30000.00, 3380000.00, 'Khách hàng kiểm tra', '0900000001', '12 Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh', 'Giao tiêu chuẩn', 'Đơn dữ liệu kiểm tra', 'order-20260815-001', REPEAT('a', 64), '2026-08-15 10:00:00', '2026-08-18 14:30:00')
ON DUPLICATE KEY UPDATE
  nhan_vien_xu_ly_id = VALUES(nhan_vien_xu_ly_id), ma_giam_gia_id = VALUES(ma_giam_gia_id), trang_thai = 'HOAN_THANH',
  tong_tien = VALUES(tong_tien), tien_giam = VALUES(tien_giam), phi_van_chuyen = VALUES(phi_van_chuyen), tong_thanh_toan = VALUES(tong_thanh_toan);
SET @order_id := (SELECT id FROM don_hang WHERE ma_don_hang = 'ORD-20260815-001');

INSERT INTO chi_tiet_don_hang
  (don_hang_id, bien_the_id, ten_san_pham_chot, sku_chot, size_chot, mau_sac_chot, so_luong, don_gia, thanh_tien)
SELECT @order_id, @v_ring_1, 'Nhẫn vàng NÉT Mảnh', 'NET-RING-001-12', '12', 'Vàng', 1, 3450000.00, 3450000.00
WHERE NOT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE don_hang_id = @order_id AND bien_the_id = @v_ring_1);
INSERT INTO su_dung_ma_giam_gia (ma_giam_gia_id, tai_khoan_id, don_hang_id, so_tien_giam)
SELECT @coupon_id, @customer01_id, @order_id, 100000.00
WHERE NOT EXISTS (SELECT 1 FROM su_dung_ma_giam_gia WHERE don_hang_id = @order_id);
INSERT INTO thanh_toan (don_hang_id, so_tien, phuong_thuc, trang_thai, thoi_gian_thanh_toan)
VALUES (@order_id, 3380000.00, 'COD', 'CONFIRMED', '2026-08-18 14:30:00')
ON DUPLICATE KEY UPDATE so_tien = VALUES(so_tien), trang_thai = 'CONFIRMED', thoi_gian_thanh_toan = VALUES(thoi_gian_thanh_toan);
SET @payment_id := (SELECT id FROM thanh_toan WHERE don_hang_id = @order_id);
INSERT INTO giao_dich (thanh_toan_id, ma_giao_dich_thanh_toan, so_tien, phuong_thuc, trang_thai, thoi_gian, ghi_chu)
SELECT @payment_id, 'TXN-20260818-001', 3380000.00, 'COD', 'THANH_CONG', '2026-08-18 14:30:00', 'Thu COD cho đơn kiểm tra'
WHERE NOT EXISTS (SELECT 1 FROM giao_dich WHERE ma_giao_dich_thanh_toan = 'TXN-20260818-001');
INSERT INTO giao_hang (don_hang_id, don_vi_van_chuyen, ma_van_don, phi_van_chuyen, ngay_du_kien, ngay_giao, ngay_nhan, trang_thai, ghi_chu)
VALUES (@order_id, 'Giao hàng nội bộ', 'SHIP-20260816-001', 30000.00, '2026-08-17 18:00:00', '2026-08-16 09:00:00', '2026-08-18 14:30:00', 'DA_GIAO', 'Dữ liệu giao hàng kiểm tra')
ON DUPLICATE KEY UPDATE trang_thai = 'DA_GIAO', ngay_nhan = VALUES(ngay_nhan), ghi_chu = VALUES(ghi_chu);

-- Đánh giá hiển thị trên chi tiết sản phẩm.
INSERT INTO danh_gia (san_pham_id, tai_khoan_id, don_hang_id, so_sao, noi_dung, trang_thai, phan_hoi)
SELECT @p_ring_1, @customer01_id, @order_id, 5, 'Thiết kế gọn, đeo vừa tay và đóng gói cẩn thận.', 'HIEN_THI', 'Cảm ơn bạn đã tin tưởng NÉT Jewelry.'
WHERE NOT EXISTS (SELECT 1 FROM danh_gia WHERE san_pham_id = @p_ring_1 AND tai_khoan_id = @customer01_id AND don_hang_id = @order_id);
SET @review_id := (SELECT id FROM danh_gia WHERE san_pham_id = @p_ring_1 AND tai_khoan_id = @customer01_id AND don_hang_id = @order_id LIMIT 1);
INSERT INTO hinh_anh_danh_gia (danh_gia_id, duong_dan, thu_tu)
SELECT @review_id, 'https://images.unsplash.com/photo-1605100804763-247f67b3557e?auto=format&fit=crop&w=900&q=80', 1
WHERE NOT EXISTS (SELECT 1 FROM hinh_anh_danh_gia WHERE danh_gia_id = @review_id AND thu_tu = 1);

-- Nội dung công khai và banner dùng để kiểm tra homepage/trang thông tin.
INSERT INTO banner (tieu_de, hinh_anh, duong_dan_lien_ket, thu_tu, trang_thai)
SELECT 'Trang sức cho mỗi ngày', 'https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?auto=format&fit=crop&w=1600&q=85', '/products', 1, 'HIEN_THI'
WHERE NOT EXISTS (SELECT 1 FROM banner WHERE tieu_de = 'Trang sức cho mỗi ngày');
INSERT INTO noi_dung_trang (loai_noi_dung, tieu_de, slug, noi_dung, trang_thai)
VALUES
  ('GIOI_THIEU', 'Câu chuyện NÉT', 'gioi-thieu', 'NÉT Jewelry chọn các thiết kế trang sức dễ đeo, rõ ràng về chất liệu và phù hợp với nhịp sống hằng ngày.', 'HIEN_THI'),
  ('CHINH_SACH', 'Giao hàng và đổi trả', 'giao-hang-doi-tra', 'Kiểm tra sản phẩm khi nhận hàng. Liên hệ cửa hàng nếu cần hỗ trợ đổi trả theo chính sách áp dụng.', 'HIEN_THI'),
  ('FAQ', 'Câu hỏi thường gặp', 'cau-hoi-thuong-gap', 'Bạn có thể liên hệ cửa hàng để được tư vấn về kích cỡ, chất liệu và đơn hàng.', 'HIEN_THI'),
  ('LIEN_HE', 'Liên hệ', 'lien-he', 'Hotline: 0900 000 000. Email: hello@netjewelry.local.', 'HIEN_THI')
ON DUPLICATE KEY UPDATE tieu_de = VALUES(tieu_de), noi_dung = VALUES(noi_dung), trang_thai = 'HIEN_THI';

COMMIT;

-- Kiểm tra nhanh sau import:
-- SELECT ma_sku, ten_san_pham, gia FROM san_pham WHERE ma_sku LIKE 'NET-%';
-- SELECT ten_dang_nhap, email FROM tai_khoan WHERE ten_dang_nhap LIKE '%01';

