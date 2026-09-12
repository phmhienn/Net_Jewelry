package com.example.jewelrystore;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.util.Checks;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoreApiIntegrationTest {
  @Test
  void sharedLoginAcceptsEmailAndUsernameForEveryRole() throws Exception {
    var accounts =
        List.of(
            customers.findById(customerId).orElseThrow(),
            staff.findByUsername("staff").orElseThrow(),
            staff.findByUsername("manager").orElseThrow());
    for (TaiKhoan tk : accounts) {
      for (String identifier : List.of(tk.getEmail(), tk.getUsername())) {
        var response =
            ok(
                post("/api/auth/login"),
                null,
                Map.of(
                    "identifier",
                    "  " + identifier.toUpperCase(Locale.ROOT) + "  ",
                    "password",
                    "TestPass123!"));
        assertEquals(tk.getRole().name(), response.get("user").get("role").asString());
        assertFalse(response.get("user").has("password"));
        String token = response.get("accessToken").asString();
        assertEquals(tk.getId(), ok(get("/api/auth/me"), token, null).get("id").asLong());
        assertEquals(
            tk.getRole() == Role.KHACH_HANG ? 403 : 200,
            call(get("/api/admin/orders"), token, null).status());
        assertEquals(
            tk.getRole() == Role.QUAN_LY ? 200 : 403,
            call(get("/api/admin/statistics/dashboard"), token, null).status());
        assertEquals(
            tk.getRole() == Role.KHACH_HANG ? 200 : 403,
            call(get("/api/cart"), token, null).status());
      }
    }
  }

  @Test
  void sharedLoginRejectsWrongPasswordsLockedAccountsAndBlankIdentifiers() throws Exception {
    for (String username : List.of("staff", "manager")) {
      assertEquals(
          401,
          call(
                  post("/api/auth/login"),
                  null,
                  Map.of("identifier", username, "password", "wrong-password"))
              .status());
      var tk = staff.findByUsername(username).orElseThrow();
      tk.setStatus(AccountStatus.KHOA);
      staff.saveAndFlush(tk);
      for (String identifier : List.of(username, tk.getEmail())) {
        assertEquals(
            401,
            call(
                    post("/api/auth/login"),
                    null,
                    Map.of("identifier", identifier, "password", "TestPass123!"))
                .status());
      }
    }
    assertEquals(
        400,
        call(post("/api/auth/login"), null, Map.of("identifier", "  ", "password", "TestPass123!"))
            .status());
    assertEquals(
        401,
        call(
                post("/api/auth/login"),
                null,
                Map.of("identifier", "missing-user", "password", "TestPass123!"))
            .status());
  }

  @Test
  void sharedProfileUpdatesOnlyCurrentAccountWithoutChangingRole() throws Exception {
    for (String token : List.of(customerToken, staffToken, managerToken)) {
      var before = ok(get("/api/auth/me"), token, null);
      var updated =
          ok(
              put("/api/account/profile"),
              token,
              Map.of(
                  "name",
                  "Updated profile",
                  "phone",
                  "0901234567",
                  "role",
                  "QUAN_LY",
                  "id",
                  customerId));
      assertEquals(before.get("id").asLong(), updated.get("id").asLong());
      assertEquals(before.get("role").asString(), updated.get("role").asString());
      assertEquals("Updated profile", updated.get("name").asString());
      assertEquals(
          before.get("id").asLong(),
          ok(get("/api/account/profile"), token, null).get("id").asLong());
    }
    assertEquals(
        401,
        call(put("/api/account/profile"), null, Map.of("name", "Unauthorized profile")).status());
    assertEquals(403, call(get("/api/addresses"), staffToken, null).status());
    assertEquals(403, call(get("/api/admin/customers"), staffToken, null).status());
  }

  private void assertCheckViolation(String constraint, Runnable mutation) {
    var failure = assertThrows(org.springframework.dao.DataAccessException.class, mutation::run);
    Throwable cause = failure;
    while (cause.getCause() != null) cause = cause.getCause();
    assertInstanceOf(java.sql.SQLException.class, cause);
    var sqlError = (java.sql.SQLException) cause;
    assertTrue(
        sqlError.getErrorCode() == 3819 || "23513".equals(sqlError.getSQLState()),
        "Expected MySQL/H2 CHECK violation, got: " + sqlError.getMessage());
    assertTrue(sqlError.getMessage().toLowerCase(Locale.ROOT).contains(constraint));
  }

  @Test
  void databaseRejectsInvalidStockStaffRoleAndReviewStars() {
    assertCheckViolation(
        "ck_ton_kho",
        () ->
            jdbc.update(
                "update ton_kho set so_luong_dat = so_luong_ton + 1 where bien_the_id = ?",
                variantId));
    assertCheckViolation(
        "ck_tai_khoan_token_version",
        () -> jdbc.update("update tai_khoan set token_version = -1 where ten_dang_nhap = 'staff'"));
    assertCheckViolation(
        "ck_danh_gia_so_sao",
        () ->
            jdbc.update(
                "insert into danh_gia (san_pham_id, tai_khoan_id, so_sao, noi_dung, trang_thai, ngay_tao, ngay_cap_nhat) values (?, ?, 6, 'Invalid stars', 'HIEN_THI', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                productId,
                customerId));
    assertEquals(0, stocks.findByVariantId(variantId).orElseThrow().getReserved());
  }

  @Test
  void changingCartQuantityKeepsLineAndCartTotalsConsistent() throws Exception {
    add(customerToken, 1);
    long itemId = ok(get("/api/cart"), customerToken, null).get("items").get(0).get("id").asLong();
    var cart = ok(put("/api/cart/items/" + itemId), customerToken, Map.of("quantity", 3));
    assertEquals(300000, cart.get("total").asInt());
    assertEquals(300000, cart.get("items").get(0).get("total").asInt());
    assertCheckViolation(
        "ck_ct_gio_hang",
        () ->
            jdbc.update(
                "update chi_tiet_gio_hang set thanh_tien = thanh_tien + 1 where id = ?", itemId));
  }

  @Test
  void emptyCatalogAndCustomerDataDoNotGenerateExamples() throws Exception {
    stocks.deleteAll();
    variants.deleteAll();
    products.deleteAll();
    categories.deleteAll();
    brands.deleteAll();
    var catalog = ok(get("/api/products"), null, null);
    assertEquals(0, catalog.get("content").size());
    assertEquals(0, catalog.get("totalElements").asInt());
    assertEquals(0, ok(get("/api/categories"), null, null).size());
    assertEquals(0, ok(get("/api/brands"), null, null).size());
    assertEquals(0, ok(get("/api/products/materials"), null, null).size());
    assertEquals(0, ok(get("/api/cart"), customerToken, null).get("items").size());
    assertEquals(0, ok(get("/api/orders"), customerToken, null).get("content").size());
    assertEquals(0, ok(get("/api/addresses"), customerToken, null).size());
    var settings = ok(get("/api/storefront/settings"), null, null);
    assertTrue(settings.get("shippingFee").asDouble() >= 0);
    assertTrue(settings.get("freeShippingThreshold").asDouble() >= 0);
    assertEquals(0, products.count());
  }

  @Test
  void concurrentPaymentConfirmationCreatesOneTransaction() throws Exception {
    var order = complete();
    long paymentId = order.get("payment").get("id").asLong();
    try (var pool = Executors.newFixedThreadPool(2)) {
      var start = new CountDownLatch(1);
      Callable<Integer> confirm =
          () -> {
            start.await();
            return call(post("/api/admin/payments/" + paymentId + "/confirm"), staffToken, null)
                .status();
          };
      var first = pool.submit(confirm);
      var second = pool.submit(confirm);
      start.countDown();
      assertEquals(200, first.get(20, TimeUnit.SECONDS));
      assertEquals(200, second.get(20, TimeUnit.SECONDS));
    }
    assertEquals(1, jdbc.queryForObject("select count(*) from giao_dich", Integer.class));
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  @Autowired PlatformTransactionManager tx;
  @Autowired TaiKhoanRepository customers;
  @Autowired TaiKhoanRepository staff;
  @Autowired VaiTroRepository roles;
  @Autowired DanhMucRepository categories;
  @Autowired ThuongHieuRepository brands;
  @Autowired SanPhamRepository products;
  @Autowired BienTheSanPhamRepository variants;
  @Autowired TonKhoRepository stocks;
  @Autowired GioHangRepository carts;
  @Autowired PasswordEncoder encoder;

  @org.springframework.test.context.bean.override.mockito.MockitoSpyBean
  ThanhToanRepository paymentRepository;

  Long customerId, otherId, productId, variantId, categoryId, brandId;
  String customerToken, otherToken, staffToken, managerToken;

  record Reply(int status, JsonNode body) {
    JsonNode data() {
      return body.get("data");
    }
  }

  @Test
  void transactionRollsBackAfterOrderAndReservationWereWritten() throws Exception {
    add(customerToken, 2);
    org.mockito.Mockito.doThrow(
            new org.springframework.dao.DataIntegrityViolationException(
                "injected payment write failure"))
        .when(paymentRepository)
        .save(org.mockito.ArgumentMatchers.any(ThanhToan.class));
    assertEquals(
        409,
        call(
                post("/api/orders").header("Idempotency-Key", "rollback-after-write"),
                customerToken,
                order("COD"))
            .status());
    assertEquals(0, jdbc.queryForObject("select count(*) from don_hang", Integer.class));
    assertEquals(0, jdbc.queryForObject("select count(*) from chi_tiet_don_hang", Integer.class));
    assertEquals(0, jdbc.queryForObject("select count(*) from lich_su_kho", Integer.class));
    assertEquals(0, stocks.findByVariantId(variantId).orElseThrow().getReserved());
    assertEquals(1, ok(get("/api/cart"), customerToken, null).get("items").size());
  }

  @Test
  void staffCannotEscalateAndManagerCannotLockOrDemoteSelf() throws Exception {
    var employee = staff.findByUsername("staff").orElseThrow();
    var manager = staff.findByUsername("manager").orElseThrow();
    assertEquals(
        403,
        call(
                patch("/api/admin/staff/" + employee.getId() + "/role"),
                staffToken,
                Map.of("role", "QUAN_LY"))
            .status());
    assertEquals(
        400,
        call(
                patch("/api/admin/staff/" + manager.getId() + "/role"),
                managerToken,
                Map.of("role", "NHAN_VIEN"))
            .status());
    assertEquals(
        400,
        call(
                patch("/api/admin/staff/" + manager.getId() + "/status"),
                managerToken,
                Map.of("status", "KHOA"))
            .status());
    ok(
        patch("/api/admin/staff/" + employee.getId() + "/role"),
        managerToken,
        Map.of("role", "QUAN_LY"));
    assertEquals(401, call(get("/api/admin/staff"), staffToken, null).status());
    assertEquals(200, call(get("/api/admin/staff"), staffLogin("staff"), null).status());
  }

  @Test
  void uploadsVerifyBytesAndAreServedFromBackend() throws Exception {
    var fake =
        new org.springframework.mock.web.MockMultipartFile(
            "file",
            "fake.png",
            "image/png",
            "not an image".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    assertEquals(
        400,
        call(multipart("/api/account/avatar/upload").file(fake), customerToken, null).status());
    var buffer = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(
        new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB),
        "png",
        buffer);
    var valid =
        new org.springframework.mock.web.MockMultipartFile(
            "file", "avatar.png", "image/png", buffer.toByteArray());
    var uploaded = ok(multipart("/api/account/avatar/upload").file(valid), customerToken, null);
    assertTrue(uploaded.get("avatar").asText().startsWith("/uploads/"));
    assertEquals(
        200,
        mvc.perform(get(uploaded.get("avatar").asText())).andReturn().getResponse().getStatus());
    assertEquals(
        403,
        call(
                multipart("/api/products/" + productId + "/images/upload").file(valid),
                customerToken,
                null)
            .status());
  }

  private Map<String, Object> coupon(String code, int quantity) {
    return Map.of(
        "code",
        code,
        "name",
        "Ưu đãi",
        "type",
        "PHAN_TRAM",
        "value",
        20,
        "minimumOrder",
        100000,
        "quantity",
        quantity,
        "startsAt",
        Instant.now().minusSeconds(60).toString(),
        "endsAt",
        Instant.now().plusSeconds(3600).toString(),
        "status",
        "HOAT_DONG");
  }

  @Test
  void unifiedAccountsKeepCustomerAndStaffBoundaries() throws Exception {
    assertEquals(
        2, ok(get("/api/admin/customers"), managerToken, null).get("totalElements").asInt());
    assertEquals(2, ok(get("/api/admin/staff"), managerToken, null).get("totalElements").asInt());
    assertEquals(
        400,
        call(
                patch("/api/admin/staff/" + customerId + "/role"),
                managerToken,
                Map.of("role", "QUAN_LY"))
            .status());
    long staffId = staff.findByUsername("staff").orElseThrow().getId();
    assertEquals(400, call(get("/api/admin/customers/" + staffId), managerToken, null).status());
    assertEquals(
        200,
        call(
                post("/api/auth/login"),
                null,
                Map.of("email", "staff@example.test", "password", "TestPass123!"))
            .status());
    assertEquals(
        401,
        call(
                post("/api/auth/staff/login"),
                null,
                Map.of(
                    "username",
                    customers.findById(customerId).orElseThrow().getUsername(),
                    "password",
                    "TestPass123!"))
            .status());
    assertEquals(
        400,
        call(
                post("/api/auth/register"),
                null,
                Map.of(
                    "name",
                    "Người dùng",
                    "email",
                    "staff@example.test",
                    "password",
                    "TestPass123!",
                    "username",
                    "newuser"))
            .status());
    assertEquals(
        400,
        call(
                post("/api/auth/register"),
                null,
                Map.of(
                    "name",
                    "Người dùng",
                    "email",
                    "new@example.test",
                    "password",
                    "TestPass123!",
                    "username",
                    "staff"))
            .status());
  }

  @Test
  void wishlistPersistsCompositeKeyAndSeparatesOwners() throws Exception {
    ok(put("/api/wishlist/" + productId), customerToken, null);
    ok(put("/api/wishlist/" + productId), customerToken, null);
    assertEquals(1, ok(get("/api/wishlist"), customerToken, null).get("totalElements").asInt());
    assertEquals(0, ok(get("/api/wishlist"), otherToken, null).get("totalElements").asInt());
    ok(delete("/api/wishlist/" + productId), otherToken, null);
    assertTrue(ok(get("/api/wishlist/" + productId), customerToken, null).asBoolean());
    ok(delete("/api/wishlist/" + productId), customerToken, null);
    assertEquals(0, jdbc.queryForObject("select count(*) from yeu_thich", Integer.class));
    assertEquals(403, call(put("/api/wishlist/" + productId), staffToken, null).status());
  }

  @Test
  void selectedCartSalePriceCouponAndSnapshotsMatchDatabase() throws Exception {
    jdbc.update("update bien_the_san_pham set gia_khuyen_mai=80000 where id=?", variantId);
    var cart = add(customerToken, 2);
    long itemId = cart.get("items").get(0).get("id").asLong();
    assertEquals(160000, cart.get("total").asInt());
    ok(patch("/api/cart/items/" + itemId + "/selection"), customerToken, Map.of("selected", false));
    assertEquals(
        400,
        call(
                post("/api/orders").header("Idempotency-Key", "none-selected"),
                customerToken,
                order("COD"))
            .status());
    ok(patch("/api/cart/items/" + itemId + "/selection"), customerToken, Map.of("selected", true));
    ok(post("/api/admin/coupons"), managerToken, coupon("SAVE20", 1));
    var quote =
        ok(post("/api/coupons/quote"), customerToken, Map.of("code", "SAVE20", "subtotal", 1));
    assertEquals(32000, quote.get("discount").asInt());
    var request = new HashMap<>(order("COD"));
    request.put("couponCode", "SAVE20");
    var dh =
        ok(
            post("/api/orders").header("Idempotency-Key", "coupon-snapshot"),
            customerToken,
            request);
    assertEquals(158000, dh.get("total").asInt());
    assertEquals(32000, dh.get("discount").asInt());
    assertTrue(dh.get("code").asText().startsWith("ORD"));
    assertEquals(
        1,
        jdbc.queryForObject(
            "select so_luong_da_dung from ma_giam_gia where ma_code='SAVE20'", Integer.class));
    assertEquals(1, jdbc.queryForObject("select count(*) from su_dung_ma_giam_gia", Integer.class));
    long id = dh.get("id").asLong();
    jdbc.update("update san_pham set ten_san_pham='Tên mới' where id=?", productId);
    jdbc.update(
        "update bien_the_san_pham set gia=120000,gia_khuyen_mai=null,size='14' where id=?",
        variantId);
    var saved = ok(get("/api/orders/" + id), customerToken, null);
    assertEquals("Nhẫn Golden Curve", saved.get("items").get(0).get("productName").asText());
    assertEquals(80000, saved.get("items").get(0).get("unitPrice").asInt());
    assertEquals(
        "12",
        jdbc.queryForObject(
            "select size_chot from chi_tiet_don_hang where don_hang_id=?", String.class, id));
    ok(post("/api/orders/" + id + "/cancel"), customerToken, null);
    ok(post("/api/orders/" + id + "/cancel"), customerToken, null);
    assertEquals(
        0,
        jdbc.queryForObject(
            "select so_luong_da_dung from ma_giam_gia where ma_code='SAVE20'", Integer.class));
    assertEquals(1, jdbc.queryForObject("select count(*) from su_dung_ma_giam_gia", Integer.class));
  }

  @Test
  void concurrentCouponUseCannotExceedQuota() throws Exception {
    ok(post("/api/admin/coupons"), managerToken, coupon("LASTONE", 1));
    add(customerToken, 1);
    add(otherToken, 1);
    var request = new HashMap<>(order("COD"));
    request.put("couponCode", "LASTONE");
    try (var pool = Executors.newFixedThreadPool(2)) {
      var start = new CountDownLatch(1);
      var first =
          pool.submit(
              () -> {
                start.await();
                return call(
                        post("/api/orders").header("Idempotency-Key", "quota-first"),
                        customerToken,
                        request)
                    .status();
              });
      var second =
          pool.submit(
              () -> {
                start.await();
                return call(
                        post("/api/orders").header("Idempotency-Key", "quota-second"),
                        otherToken,
                        request)
                    .status();
              });
      start.countDown();
      var results =
          new ArrayList<>(
              List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS)));
      Collections.sort(results);
      assertEquals(List.of(201, 400), results);
    }
    assertEquals(1, jdbc.queryForObject("select count(*) from don_hang", Integer.class));
    assertEquals(1, stocks.findByVariantId(variantId).orElseThrow().getReserved());
  }

  @Test
  void couponValidationAndManagerOnlyContent() throws Exception {
    assertEquals(403, call(post("/api/admin/coupons"), staffToken, coupon("NOACCESS", 1)).status());
    var invalid = new HashMap<>(coupon("BADVALUE", 1));
    invalid.put("value", 101);
    assertEquals(400, call(post("/api/admin/coupons"), managerToken, invalid).status());
    invalid = new HashMap<>(coupon("EXPIRED", 1));
    invalid.put("startsAt", Instant.now().minusSeconds(3600).toString());
    invalid.put("endsAt", Instant.now().minusSeconds(60).toString());
    ok(post("/api/admin/coupons"), managerToken, invalid);
    add(customerToken, 1);
    assertEquals(
        400, call(post("/api/coupons/quote"), customerToken, Map.of("code", "EXPIRED")).status());
    var content =
        Map.of(
            "type",
            "GIOI_THIEU",
            "title",
            "Cửa hàng",
            "slug",
            "gioi-thieu",
            "content",
            "Nội dung từ database",
            "status",
            "HIEN_THI");
    assertEquals(403, call(post("/api/admin/contents"), staffToken, content).status());
    var nd = ok(post("/api/admin/contents"), managerToken, content);
    assertEquals(
        "Nội dung từ database",
        ok(get("/api/contents/gioi-thieu"), null, null).get("content").asText());
    var hidden = new HashMap<>(content);
    hidden.put("status", "AN");
    ok(put("/api/admin/contents/" + nd.get("id").asLong()), managerToken, hidden);
    assertEquals(404, call(get("/api/contents/gioi-thieu"), null, null).status());
    assertEquals(0, ok(get("/api/contents"), null, null).get("totalElements").asInt());
    ok(
        post("/api/admin/banners"),
        managerToken,
        Map.of(
            "image",
            "https://example.test/banner.png",
            "status",
            "HIEN_THI",
            "startsAt",
            Instant.now().plusSeconds(3600).toString()));
    assertEquals(0, ok(get("/api/banners"), null, null).get("totalElements").asInt());
  }

  @Test
  void saleFilteringAndInactiveVariantAndThresholdUseDbValues() throws Exception {
    jdbc.update("update bien_the_san_pham set gia_khuyen_mai=80000 where id=?", variantId);
    assertEquals(
        1,
        ok(get("/api/products?maxPrice=90000&sort=priceAsc"), null, null)
            .get("totalElements")
            .asInt());
    ok(patch("/api/inventory/" + variantId + "/threshold"), staffToken, Map.of("threshold", 2));
    assertEquals(
        0, ok(get("/api/inventory?lowStock=true"), staffToken, null).get("totalElements").asInt());
    jdbc.update("update bien_the_san_pham set trang_thai='NGUNG_BAN' where id=?", variantId);
    assertEquals(
        400,
        call(post("/api/cart/items"), customerToken, Map.of("variantId", variantId, "quantity", 1))
            .status());
    jdbc.update("update thuong_hieu set trang_thai='NGUNG_HOAT_DONG' where id=?", brandId);
    assertEquals(0, ok(get("/api/products"), null, null).get("totalElements").asInt());
  }

  @Test
  void deliveryAndPlainAddressFromSqlAreReadable() throws Exception {
    add(customerToken, 1);
    var dh = checkout(customerToken, "COD", "delivery-fields");
    long id = dh.get("id").asLong();
    assertEquals("CHO_XU_LY", dh.get("delivery").get("status").asText());
    ok(
        put("/api/admin/orders/" + id + "/delivery"),
        staffToken,
        Map.of("carrier", "Đơn vị giao hàng", "trackingCode", "VD123"));
    jdbc.update(
        "update don_hang set dia_chi_giao_hang='Địa chỉ lưu trực tiếp trong SQL' where id=?", id);
    var saved = ok(get("/api/orders/" + id), customerToken, null);
    assertEquals("Người nhận", saved.get("address").get("name").asText());
    assertEquals("Địa chỉ lưu trực tiếp trong SQL", saved.get("address").get("street").asText());
    for (String state : List.of("DA_XAC_NHAN", "DANG_XU_LY", "DANG_GIAO_HANG", "HOAN_THANH"))
      change(id, state);
    saved = ok(get("/api/orders/" + id), customerToken, null);
    assertEquals("DA_GIAO", saved.get("delivery").get("status").asText());
    assertEquals("VD123", saved.get("delivery").get("trackingCode").asText());
  }

  @BeforeEach
  void setup() throws Exception {
    String url;
    try (var connection = jdbc.getDataSource().getConnection()) {
      url = connection.getMetaData().getURL();
    }
    assertTrue(
        url.startsWith("jdbc:h2:mem:") || url.contains("/jewelry_test"),
        "Tests require an isolated test database");
    new TransactionTemplate(tx)
        .executeWithoutResult(
            s -> {
              for (String table :
                  List.of(
                      "banner",
                      "noi_dung_trang",
                      "yeu_thich",
                      "giao_hang",
                      "su_dung_ma_giam_gia",
                      "hinh_anh_danh_gia",
                      "danh_gia",
                      "giao_dich",
                      "thanh_toan",
                      "chi_tiet_don_hang",
                      "don_hang",
                      "ma_giam_gia",
                      "chi_tiet_gio_hang",
                      "gio_hang",
                      "lich_su_kho",
                      "ton_kho",
                      "bien_the_san_pham",
                      "hinh_anh_san_pham",
                      "san_pham",
                      "danh_muc",
                      "thuong_hieu",
                      "gia_vang",
                      "dia_chi",
                      "tai_khoan",
                      "vai_tro")) jdbc.update("delete from " + table);
              for (Role role : Role.values()) {
                var vt = new VaiTro();
                vt.setName(role.name());
                roles.save(vt);
              }
              customerId = customer("buyer@example.test");
              otherId = customer("other@example.test");
              employee("staff", Role.NHAN_VIEN);
              employee("manager", Role.QUAN_LY);
              var category = new DanhMuc();
              category.setName("Nhẫn");
              categories.save(category);
              categoryId = category.getId();
              var brand = new ThuongHieu();
              brand.setName("NÉT");
              brands.save(brand);
              brandId = brand.getId();
              SanPham sp = new SanPham();
              sp.setSku("NET-TEST");
              sp.setName("Nhẫn Golden Curve");
              sp.setCategory(category);
              sp.setBrand(brand);
              sp.setPrice(new BigDecimal("100000"));
              sp.setMaterial("Vàng 18K");
              products.save(sp);
              productId = sp.getId();
              BienTheSanPham btsp = new BienTheSanPham();
              btsp.setProduct(sp);
              btsp.setSku("NET-TEST-12");
              btsp.setSize("12");
              btsp.setPrice(sp.getPrice());
              variants.save(btsp);
              variantId = btsp.getId();
              var stock = new TonKho();
              stock.setVariant(btsp);
              stock.setQuantity(5);
              stocks.save(stock);
            });
    customerToken = login("buyer@example.test");
    otherToken = login("other@example.test");
    staffToken = staffLogin("staff");
    managerToken = staffLogin("manager");
  }

  private Long customer(String email) {
    TaiKhoan kh = new TaiKhoan();
    kh.setEmail(email);
    kh.setUsername("kh_" + UUID.randomUUID());
    kh.setAuthority(roles.findByName("KHACH_HANG").orElseThrow());
    kh.setName("Người mua");
    kh.setPassword(encoder.encode("TestPass123!"));
    customers.save(kh);
    var cart = new GioHang();
    cart.setCustomer(kh);
    carts.save(cart);
    return kh.getId();
  }

  private void employee(String username, Role role) {
    TaiKhoan nv = new TaiKhoan();
    nv.setUsername(username);
    nv.setEmail(username + "@example.test");
    nv.setName(username);
    nv.setPassword(encoder.encode("TestPass123!"));
    nv.setAuthority(roles.findByName(role.name()).orElseThrow());
    staff.save(nv);
  }

  private Reply call(AbstractMockHttpServletRequestBuilder<?> request, String token, Object body)
      throws Exception {
    if (token != null) request.header("Authorization", "Bearer " + token);
    if (body != null)
      request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
    var r = mvc.perform(request).andReturn().getResponse();
    return new Reply(
        r.getStatus(),
        r.getContentAsString().isBlank()
            ? json.createObjectNode()
            : json.readTree(r.getContentAsString()));
  }

  private JsonNode ok(AbstractMockHttpServletRequestBuilder<?> r, String token, Object body)
      throws Exception {
    var reply = call(r, token, body);
    assertTrue(reply.status() >= 200 && reply.status() < 300, reply.body().toString());
    return reply.data();
  }

  private String login(String email) throws Exception {
    return ok(post("/api/auth/login"), null, Map.of("email", email, "password", "TestPass123!"))
        .get("accessToken")
        .asText();
  }

  private String staffLogin(String username) throws Exception {
    return ok(
            post("/api/auth/login"), null, Map.of("username", username, "password", "TestPass123!"))
        .get("accessToken")
        .asText();
  }

  private Map<String, Object> address() {
    return Map.of(
        "name",
        "Người nhận",
        "phone",
        "0901234567",
        "city",
        "Hồ Chí Minh",
        "district",
        "Quận 1",
        "ward",
        "Bến Nghé",
        "street",
        "12 Nguyễn Huệ",
        "defaultAddress",
        true);
  }

  private Map<String, Object> order(String method) {
    return Map.of("address", address(), "payment", method, "note", "Đơn kiểm thử", "total", 0);
  }

  private JsonNode add(String token, int qty) throws Exception {
    return ok(post("/api/cart/items"), token, Map.of("variantId", variantId, "quantity", qty));
  }

  private JsonNode checkout(String token, String method, String key) throws Exception {
    return ok(post("/api/orders").header("Idempotency-Key", key), token, order(method));
  }

  private void change(long id, String state) throws Exception {
    ok(patch("/api/admin/orders/" + id + "/status"), staffToken, Map.of("status", state));
  }


  private JsonNode bankOrder(String key) throws Exception {
    add(customerToken, 1);
    return checkout(customerToken, "BANK_TRANSFER", key);
  }

  private Reply sepay(JsonNode order, long amount, String transferType, long transactionId, boolean validSignature)
      throws Exception {
    String code = order.get("code").asText();
    String body = json.writeValueAsString(Map.of(
        "id", transactionId,
        "gateway", "Vietcombank",
        "transactionDate", "2026-09-11 10:00:00",
        "accountNumber", "0123456789",
        "code", code,
        "content", "Thanh toan don hang " + code,
        "transferType", transferType,
        "transferAmount", amount,
        "referenceCode", "FT" + transactionId));
    return sepayRaw(body, validSignature);
  }

  private Reply sepayRaw(String body, boolean validSignature) throws Exception {
    String timestamp = String.valueOf(Instant.now().getEpochSecond());
    String signature = validSignature ? sepaySignature(timestamp, body) : "sha256=invalid";
    var response =
        mvc.perform(
                post("/api/payment/sepay/webhook")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-SePay-Timestamp", timestamp)
                    .header("X-SePay-Signature", signature)
                    .content(body))
            .andReturn()
            .getResponse();
    return new Reply(
        response.getStatus(),
        response.getContentAsString().isBlank()
            ? json.createObjectNode()
            : json.readTree(response.getContentAsString()));
  }

  private String sepaySignature(String timestamp, String body) {
    try {
      javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
      mac.init(new javax.crypto.spec.SecretKeySpec("test-sepay-secret".getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] digest = mac.doFinal((timestamp + "." + body).getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(digest.length * 2);
      for (byte b : digest) hex.append(String.format("%02x", b));
      return "sha256=" + hex;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private JsonNode complete() throws Exception {
    add(customerToken, 2);
    var o = checkout(customerToken, "COD", "complete-0001");
    for (String status : List.of("DA_XAC_NHAN", "DANG_XU_LY", "DANG_GIAO_HANG", "HOAN_THANH"))
      change(o.get("id").asLong(), status);
    return ok(get("/api/orders/" + o.get("id").asLong()), customerToken, null);
  }

  @Test
  void registerHashesPasswordAndRejectsDuplicateOrInvalid() throws Exception {
    var r =
        call(
            post("/api/auth/register"),
            null,
            Map.of("name", "Người mới", "email", "new@example.test", "password", "NewPass123!"));
    assertEquals(201, r.status());
    assertFalse(r.body().toString().contains("password"));
    var saved = customers.findByEmail("new@example.test").orElseThrow();
    assertTrue(encoder.matches("NewPass123!", saved.getPassword()));
    assertNotEquals("NewPass123!", saved.getPassword());
    assertEquals(
        400,
        call(
                post("/api/auth/register"),
                null,
                Map.of("name", "Người mới", "email", "new@example.test", "password", "NewPass123!"))
            .status());
    assertEquals(
        400,
        call(
                post("/api/auth/register"),
                null,
                Map.of("name", "A", "email", "bad", "password", "short"))
            .status());
  }

  @Test
  void loginLogoutAndAccountLockInvalidateTokens() throws Exception {
    assertEquals(
        401,
        call(
                post("/api/auth/login"),
                null,
                Map.of("email", "buyer@example.test", "password", "wrong"))
            .status());
    ok(post("/api/auth/logout"), customerToken, null);
    assertEquals(401, call(get("/api/auth/me"), customerToken, null).status());
    customerToken = login("buyer@example.test");
    ok(
        patch("/api/admin/customers/" + customerId + "/status"),
        managerToken,
        Map.of("status", "KHOA"));
    assertEquals(401, call(get("/api/cart"), customerToken, null).status());
  }

  @Test
  void passwordChangeAndResetAreSingleUse() throws Exception {
    ok(
        put("/api/auth/password"),
        customerToken,
        Map.of("currentPassword", "TestPass123!", "newPassword", "Changed123!"));
    assertEquals(401, call(get("/api/auth/me"), customerToken, null).status());
    new TransactionTemplate(tx)
        .executeWithoutResult(
            s -> {
              TaiKhoan kh = customers.findById(customerId).orElseThrow();
              kh.setResetTokenHash(Checks.hash("reset-test-token"));
              kh.setResetTokenExpiresAt(Instant.now().plusSeconds(600));
            });
    ok(
        post("/api/auth/reset-password"),
        null,
        Map.of("token", "reset-test-token", "newPassword", "TestPass123!"));
    assertEquals(
        400,
        call(
                post("/api/auth/reset-password"),
                null,
                Map.of("token", "reset-test-token", "newPassword", "TestPass123!"))
            .status());
    assertNotNull(login("buyer@example.test"));
    assertEquals(
        503,
        call(post("/api/auth/forgot-password"), null, Map.of("email", "buyer@example.test"))
            .status());
  }

  @Test
  void authorizationSeparatesCustomersStaffAndManagers() throws Exception {
    assertEquals(401, call(get("/api/cart"), null, null).status());
    assertEquals(403, call(get("/api/admin/staff"), customerToken, null).status());
    assertEquals(403, call(get("/api/admin/staff"), staffToken, null).status());
    assertEquals(403, call(get("/api/admin/statistics/dashboard"), staffToken, null).status());
    assertEquals(403, call(post("/api/products"), customerToken, productBody("OTHER")).status());
    assertEquals(
        403,
        call(post("/api/categories"), staffToken, Map.of("name", "Vòng cổ", "status", "HOAT_DONG"))
            .status());
    assertEquals(200, call(get("/api/inventory"), staffToken, null).status());
    assertEquals(200, call(get("/api/admin/staff"), managerToken, null).status());
  }

  private Map<String, Object> productBody(String sku) {
    return Map.of(
        "sku",
        sku,
        "name",
        "Nhẫn Test",
        "categoryId",
        categoryId,
        "brandId",
        brandId,
        "price",
        200000,
        "material",
        "Bạc 925",
        "status",
        "DANG_BAN");
  }

  @Test
  void productCrudSearchFiltersAndPagination() throws Exception {
    var p = ok(post("/api/products"), staffToken, productBody("OTHER"));
    long id = p.get("id").asLong();
    assertEquals(1, p.get("variants").size());
    assertEquals("OTHER", p.get("variants").get(0).get("sku").asText());
    assertEquals(0, p.get("variants").get(0).get("stock").asInt());
    assertEquals(0, p.get("variants").get(0).get("available").asInt());
    assertTrue(stocks.findByVariantId(p.get("variants").get(0).get("id").asLong()).isPresent());
    ok(put("/api/products/" + id), staffToken, productBody("OTHER"));
    assertEquals(
        1,
        ok(
                get("/api/products")
                    .param("keyword", "Golden")
                    .param("category", categoryId.toString())
                    .param("brand", brandId.toString())
                    .param("maxPrice", "150000"),
                null,
                null)
            .get("totalElements")
            .asInt());
    var paged = ok(get("/api/products").param("sort", "priceAsc").param("size", "1"), null, null);
    assertEquals(2, paged.get("totalElements").asInt());
    assertEquals(productId.longValue(), paged.get("content").get(0).get("id").asLong());
    assertEquals(400, call(get("/api/products").param("size", "10000"), null, null).status());
    assertEquals(400, call(get("/api/products").param("sort", "anything"), null, null).status());
    ok(delete("/api/products/" + id), staffToken, null);
    assertEquals(1, ok(get("/api/products"), null, null).get("totalElements").asInt());
    assertEquals(1, ok(get("/api/admin/products"), staffToken, null).get("totalElements").asInt());
  }

  @Test
  void productImagesVariantsAndCategoryCycle() throws Exception {
    var image =
        ok(
            post("/api/products/" + productId + "/images"),
            staffToken,
            Map.of("url", "https://example.test/ring.jpg", "primaryImage", true));
    assertTrue(image.get("primaryImage").asBoolean());
    assertEquals(1, ok(get("/api/products/" + productId + "/images"), null, null).size());
    assertEquals(
        400,
        call(
                post("/api/products/" + productId + "/images"),
                staffToken,
                Map.of("url", "javascript:alert(1)"))
            .status());
    var variant =
        ok(
            post("/api/products/" + productId + "/variants"),
            staffToken,
            Map.of("sku", "NET-NEW", "price", 150000, "size", "14"));
    assertEquals(0, variant.get("available").asInt());
    ok(delete("/api/variants/" + variant.get("id").asLong()), staffToken, null);
    assertEquals(
        400,
        call(
                put("/api/categories/" + categoryId),
                managerToken,
                Map.of("name", "Nhẫn", "parentId", categoryId, "status", "HOAT_DONG"))
            .status());
  }

  @Test
  void inventoryPreservesReservedAndLogsChanges() throws Exception {
    add(customerToken, 4);
    checkout(customerToken, "COD", "inventory-0001");
    assertEquals(
        409,
        call(
                post("/api/inventory/export"),
                staffToken,
                Map.of("variantId", variantId, "quantity", 2, "reason", "Xuất kho"))
            .status());
    ok(
        post("/api/inventory/import"),
        staffToken,
        Map.of("variantId", variantId, "quantity", 3, "reason", "Nhập kho"));
    assertEquals(8, stocks.findByVariantId(variantId).orElseThrow().getQuantity());
    assertEquals(
        2,
        ok(get("/api/inventory/" + variantId + "/history"), staffToken, null)
            .get("totalElements")
            .asInt());
  }

  @Test
  void inventorySearchesByVariantSkuProductSkuAndProductName() throws Exception {
    new TransactionTemplate(tx)
        .executeWithoutResult(
            status -> {
          var category = categories.findById(categoryId).orElseThrow();
          var brand = brands.findById(brandId).orElseThrow();
          SanPham sp = new SanPham();
          sp.setSku("NET-STOCK-OTHER");
          sp.setName("Dây chuyền kiểm kho");
          sp.setCategory(category);
          sp.setBrand(brand);
          sp.setPrice(new BigDecimal("220000"));
          sp.setMaterial("Bạc");
          products.save(sp);
          BienTheSanPham btsp = new BienTheSanPham();
          btsp.setProduct(sp);
          btsp.setSku("NET-STOCK-OTHER-42");
          btsp.setSize("42");
          btsp.setPrice(sp.getPrice());
          variants.save(btsp);
          var stock = new TonKho();
          stock.setVariant(btsp);
          stock.setQuantity(7);
          stocks.save(stock);
            });

    var byName = ok(get("/api/inventory").param("keyword", "kiểm kho"), staffToken, null);
    assertEquals(1, byName.get("totalElements").asInt());
    assertEquals("Dây chuyền kiểm kho", byName.get("content").get(0).get("productName").asText());

    var byVariantSku = ok(get("/api/inventory").param("keyword", "NET-TEST-12"), staffToken, null);
    assertEquals(1, byVariantSku.get("totalElements").asInt());
    assertEquals("NET-TEST-12", byVariantSku.get("content").get(0).get("sku").asText());

    var byProductSku = ok(get("/api/inventory").param("keyword", "NET-STOCK-OTHER"), staffToken, null);
    assertEquals(1, byProductSku.get("totalElements").asInt());
    assertEquals("NET-STOCK-OTHER-42", byProductSku.get("content").get(0).get("sku").asText());
  }

  @Test
  void cartValidatesStockQuantityAndOwnership() throws Exception {
    assertEquals(
        409,
        call(post("/api/cart/items"), customerToken, Map.of("variantId", variantId, "quantity", 6))
            .status());
    assertEquals(
        400,
        call(post("/api/cart/items"), customerToken, Map.of("variantId", variantId, "quantity", 0))
            .status());
    var cart = add(customerToken, 2);
    long id = cart.get("items").get(0).get("id").asLong();
    assertEquals(200000, cart.get("total").asInt());
    assertEquals(
        403, call(put("/api/cart/items/" + id), otherToken, Map.of("quantity", 1)).status());
    ok(put("/api/cart/items/" + id), customerToken, Map.of("quantity", 3));
    ok(delete("/api/cart/items/" + id), customerToken, null);
    assertEquals(0, ok(get("/api/cart"), customerToken, null).get("items").size());
  }

  @Test
  void checkoutUsesServerPriceReservesStockAndIsIdempotent() throws Exception {
    add(customerToken, 2);
    var o = checkout(customerToken, "COD", "checkout-0001");
    assertEquals(200000, o.get("subtotal").asInt());
    assertEquals(230000, o.get("total").asInt());
    assertEquals(2, stocks.findByVariantId(variantId).orElseThrow().getReserved());
    assertEquals(0, ok(get("/api/cart"), customerToken, null).get("items").size());
    assertEquals(
        o.get("id").asLong(), checkout(customerToken, "COD", "checkout-0001").get("id").asLong());
    assertEquals(1, jdbc.queryForObject("select count(*) from don_hang", Integer.class));
    assertEquals(
        400,
        call(
                post("/api/orders").header("Idempotency-Key", "checkout-0001"),
                customerToken,
                order("ONLINE"))
            .status());
    assertEquals(403, call(get("/api/orders/" + o.get("id").asLong()), otherToken, null).status());
  }

  @Test
  void adminOrdersFilterByCodeAndOrderDate() throws Exception {
    add(customerToken, 1);
    var first = checkout(customerToken, "COD", "filter-order-0001");
    add(otherToken, 1);
    var second = checkout(otherToken, "COD", "filter-order-0002");
    jdbc.update(
        "update don_hang set ngay_dat = ? where id = ?",
        "2026-01-05 09:15:00",
        first.get("id").asLong());
    jdbc.update(
        "update don_hang set ngay_dat = ? where id = ?",
        "2026-01-06 10:30:00",
        second.get("id").asLong());

    var byCode =
        ok(
            get("/api/admin/orders").param("keyword", first.get("code").asText()),
            staffToken,
            null);
    assertEquals(1, byCode.get("totalElements").asInt());
    assertEquals(first.get("code").asText(), byCode.get("content").get(0).get("code").asText());

    var byDate = ok(get("/api/admin/orders").param("date", "2026-01-06"), staffToken, null);
    assertEquals(1, byDate.get("totalElements").asInt());
    assertEquals(second.get("code").asText(), byDate.get("content").get(0).get("code").asText());
  }

  @Test
  void failedCheckoutLeavesCartStockAndOrdersUnchanged() throws Exception {
    add(customerToken, 4);
    ok(
        post("/api/inventory/adjust"),
        staffToken,
        Map.of("variantId", variantId, "quantity", 2, "reason", "Kiểm kê"));
    assertEquals(
        409,
        call(
                post("/api/orders").header("Idempotency-Key", "rollback-0001"),
                customerToken,
                order("COD"))
            .status());
    assertEquals(0, jdbc.queryForObject("select count(*) from don_hang", Integer.class));
    assertEquals(0, stocks.findByVariantId(variantId).orElseThrow().getReserved());
    assertEquals(1, ok(get("/api/cart"), customerToken, null).get("items").size());
  }

  @Test
  void concurrentCustomersCannotOversell() throws Exception {
    add(customerToken, 4);
    add(otherToken, 4);
    try (var pool = Executors.newFixedThreadPool(2)) {
      var start = new CountDownLatch(1);
      var a =
          pool.submit(
              () -> {
                start.await();
                return call(
                        post("/api/orders").header("Idempotency-Key", "concurrent-001"),
                        customerToken,
                        order("COD"))
                    .status();
              });
      var b =
          pool.submit(
              () -> {
                start.await();
                return call(
                        post("/api/orders").header("Idempotency-Key", "concurrent-002"),
                        otherToken,
                        order("COD"))
                    .status();
              });
      start.countDown();
      var statuses =
          new ArrayList<>(List.of(a.get(20, TimeUnit.SECONDS), b.get(20, TimeUnit.SECONDS)));
      Collections.sort(statuses);
      assertEquals(List.of(201, 409), statuses);
    }
    assertEquals(4, stocks.findByVariantId(variantId).orElseThrow().getReserved());
    assertEquals(1, jdbc.queryForObject("select count(*) from don_hang", Integer.class));
  }

  @Test
  void cancellationReleasesStockOnlyOnce() throws Exception {
    add(customerToken, 3);
    var o = checkout(customerToken, "COD", "cancel-0001");
    long id = o.get("id").asLong();
    ok(post("/api/orders/" + id + "/cancel"), customerToken, null);
    ok(post("/api/orders/" + id + "/cancel"), customerToken, null);
    var stock = stocks.findByVariantId(variantId).orElseThrow();
    assertEquals(5, stock.getQuantity());
    assertEquals(0, stock.getReserved());
    assertEquals(
        409,
        call(
                patch("/api/admin/orders/" + id + "/status"),
                staffToken,
                Map.of("status", "DA_XAC_NHAN"))
            .status());
  }

  @Test
  void orderStateMachineAndCodPaymentApplyExactlyOnce() throws Exception {
    var o = complete();
    assertEquals("HOAN_THANH", o.get("status").asText());
    assertEquals("CONFIRMED", o.get("payment").get("status").asText());
    var stock = stocks.findByVariantId(variantId).orElseThrow();
    assertEquals(3, stock.getQuantity());
    assertEquals(0, stock.getReserved());
    change(o.get("id").asLong(), "HOAN_THANH");
    assertEquals(1, jdbc.queryForObject("select count(*) from giao_dich", Integer.class));
    assertEquals(
        409,
        call(post("/api/orders/" + o.get("id").asLong() + "/cancel"), customerToken, null)
            .status());
  }

  @Test
  void onlineSimulationUnavailableAndBankTransferReturnsPaymentInstruction() throws Exception {
    add(customerToken, 1);
    assertEquals(
        400,
        call(
                post("/api/orders").header("Idempotency-Key", "unsupported-online"),
                customerToken,
                order("ONLINE"))
            .status());
    var bank = checkout(customerToken, "BANK_TRANSFER", "supported-bank");
    assertEquals("BANK_TRANSFER", bank.get("payment").get("method").asText());
    assertEquals("PENDING", bank.get("payment").get("status").asText());
    assertTrue(bank.get("code").asText().startsWith("ORD"));
    assertEquals(bank.get("code").asText(), bank.get("payment").get("instruction").get("content").asText());
    assertEquals("VCB", bank.get("payment").get("instruction").get("bankCode").asText());
    assertTrue(bank.get("payment").get("instruction").get("qrUrl").asText().contains("img.vietqr.io"));

    add(customerToken, 1);
    var dh = checkout(customerToken, "COD", "payment-ownership");
    long paymentId = dh.get("payment").get("id").asLong();
    assertEquals(403, call(get("/api/payments/" + paymentId), otherToken, null).status());
    assertEquals(
        404,
        call(
                post("/api/payments/" + paymentId + "/simulate"),
                customerToken,
                Map.of("success", true))
            .status());
    assertEquals(
        403,
        call(post("/api/admin/payments/" + paymentId + "/confirm"), customerToken, null).status());
    assertEquals(
        400,
        call(post("/api/admin/payments/" + paymentId + "/confirm"), staffToken, null).status());
  }

  @Test
  void sepayWebhookConfirmsValidInboundTransfer() throws Exception {
    var order = bankOrder("sepay-valid-001");
    var reply = sepay(order, order.get("payment").get("amount").asLong(), "in", 1001L, true);
    assertEquals(200, reply.status());
    var status = ok(get("/api/orders/" + order.get("id").asLong() + "/payment-status"), customerToken, null);
    assertEquals("CONFIRMED", status.get("paymentStatus").asText());
    assertEquals("BANK_TRANSFER", status.get("paymentMethod").asText());
    assertEquals("DA_XAC_NHAN", ok(get("/api/orders/" + order.get("id").asLong()), customerToken, null).get("status").asText());
    assertEquals(1, jdbc.queryForObject("select count(*) from giao_dich where ma_giao_dich_thanh_toan='SEPAY-1001'", Integer.class));
  }

  @Test
  void sepayWebhookRejectsInvalidSignature() throws Exception {
    var order = bankOrder("sepay-signature-001");
    assertEquals(401, sepay(order, order.get("payment").get("amount").asLong(), "in", 1002L, false).status());
    assertEquals("PENDING", ok(get("/api/orders/" + order.get("id").asLong() + "/payment-status"), customerToken, null).get("paymentStatus").asText());
  }

  @Test
  void sepayWebhookRejectsInvalidAmount() throws Exception {
    var order = bankOrder("sepay-amount-001");
    assertEquals(400, sepay(order, order.get("payment").get("amount").asLong() - 1, "in", 1003L, true).status());
    assertEquals("PENDING", ok(get("/api/orders/" + order.get("id").asLong() + "/payment-status"), customerToken, null).get("paymentStatus").asText());
  }

  @Test
  void sepayWebhookIsIdempotentForDuplicateTransactions() throws Exception {
    var order = bankOrder("sepay-duplicate-001");
    assertEquals(200, sepay(order, order.get("payment").get("amount").asLong(), "in", 1004L, true).status());
    assertEquals(200, sepay(order, order.get("payment").get("amount").asLong(), "in", 1004L, true).status());
    assertEquals(1, jdbc.queryForObject("select count(*) from giao_dich where ma_giao_dich_thanh_toan='SEPAY-1004'", Integer.class));
  }

  @Test
  void sepayWebhookRejectsUnknownOrderWithoutCreatingTransaction() throws Exception {
    String body = json.writeValueAsString(Map.of(
        "id", 1005,
        "gateway", "Vietcombank",
        "transactionDate", "2026-09-11 10:00:00",
        "accountNumber", "0123456789",
        "code", "ORD20991231UNKNOWN01",
        "content", "Thanh toan ORD20991231UNKNOWN01",
        "transferType", "in",
        "transferAmount", 130000,
        "referenceCode", "FT1005"));
    assertEquals(400, sepayRaw(body, true).status());
    assertEquals(0, jdbc.queryForObject("select count(*) from giao_dich where ma_giao_dich_thanh_toan='SEPAY-1005'", Integer.class));
  }

  @Test
  void sepayWebhookIgnoresOutboundTransfers() throws Exception {
    var order = bankOrder("sepay-out-001");
    assertEquals(200, sepay(order, order.get("payment").get("amount").asLong(), "out", 1006L, true).status());
    assertEquals("PENDING", ok(get("/api/orders/" + order.get("id").asLong() + "/payment-status"), customerToken, null).get("paymentStatus").asText());
    assertEquals(0, jdbc.queryForObject("select count(*) from giao_dich where ma_giao_dich_thanh_toan='SEPAY-1006'", Integer.class));
  }

  @Test
  void reviewsRequireCompletedPurchaseAndRespectModeration() throws Exception {
    var request = Map.of("stars", 5, "content", "Sản phẩm đẹp");
    assertEquals(
        400,
        call(post("/api/products/" + productId + "/reviews"), customerToken, request).status());
    complete();
    var review = ok(post("/api/products/" + productId + "/reviews"), customerToken, request);
    long id = review.get("id").asLong();
    assertEquals(
        400,
        call(post("/api/products/" + productId + "/reviews"), customerToken, request).status());
    assertEquals(
        400,
        call(put("/api/reviews/" + id), customerToken, Map.of("stars", 6, "content", "Sai"))
            .status());
    assertEquals(403, call(delete("/api/reviews/" + id), otherToken, null).status());
    ok(
        post("/api/reviews/" + id + "/images"),
        customerToken,
        Map.of("url", "https://example.test/review.png"));
    ok(put("/api/admin/reviews/" + id + "/reply"), staffToken, Map.of("reply", "Cảm ơn bạn"));
    ok(patch("/api/admin/reviews/" + id + "/status"), staffToken, Map.of("status", "AN"));
    ok(put("/api/reviews/" + id), customerToken, request);
    assertEquals(
        0,
        ok(get("/api/products/" + productId + "/reviews"), null, null)
            .get("totalElements")
            .asInt());
    ok(delete("/api/reviews/" + id), customerToken, null);
    assertEquals(0, jdbc.queryForObject("select count(*) from hinh_anh_danh_gia", Integer.class));
  }

  @Test
  void addressesEnforceOwnershipAndSingleDefault() throws Exception {
    var first = ok(post("/api/addresses"), customerToken, address());
    var second = ok(post("/api/addresses"), customerToken, address());
    var all = ok(get("/api/addresses"), customerToken, null);
    assertFalse(all.get(0).get("defaultAddress").asBoolean());
    assertTrue(all.get(1).get("defaultAddress").asBoolean());
    assertEquals(
        403, call(delete("/api/addresses/" + first.get("id").asLong()), otherToken, null).status());
    ok(delete("/api/addresses/" + second.get("id").asLong()), customerToken, null);
    assertTrue(
        ok(get("/api/addresses"), customerToken, null).get(0).get("defaultAddress").asBoolean());
  }

  @Test
  void reportsUseCompletedRevenueAndSupportEmptyResults() throws Exception {
    assertEquals(
        0, ok(get("/api/admin/statistics/dashboard"), managerToken, null).get("revenue").asInt());
    complete();
    assertEquals(
        200000,
        ok(get("/api/admin/statistics/dashboard"), managerToken, null).get("revenue").asInt());
    for (String group : List.of("date", "category", "product")) {
      var result =
          ok(get("/api/admin/statistics/revenue").param("group", group), managerToken, null);
      assertEquals(1, result.get("totalElements").asInt());
      assertEquals(200000, result.get("content").get(0).get("amount").asInt());
    }
    assertEquals(
        1,
        ok(get("/api/admin/statistics/customers"), managerToken, null)
            .get("topCustomers")
            .get("totalElements")
            .asInt());
    assertEquals(
        230000,
        ok(get("/api/admin/customers/" + customerId), managerToken, null).get("spending").asInt());
  }

  @Test
  void goldPriceHistoryValidationAndLatestPerType() throws Exception {
    var body =
        Map.of(
            "type",
            "Vàng 9999 mẫu",
            "buyPrice",
            10000000,
            "sellPrice",
            10200000,
            "date",
            "2026-01-01",
            "unit",
            "VND/chỉ");
    ok(post("/api/gold-prices"), managerToken, body);
    assertEquals(400, call(post("/api/gold-prices"), managerToken, body).status());
    assertEquals(1, ok(get("/api/gold-prices/current"), null, null).size());
    assertEquals(1, ok(get("/api/gold-prices"), null, null).get("totalElements").asInt());
  }

  @Test
  void openApiAndCorsAreAvailable() throws Exception {
    var doc = call(get("/v3/api-docs"), null, null);
    assertEquals(200, doc.status());
    assertTrue(doc.body().has("paths"));
    java.nio.file.Files.writeString(
        java.nio.file.Path.of("target/openapi.json"), doc.body().toPrettyString());
    var response =
        mvc.perform(
                options("/api/cart")
                    .header("Origin", "http://127.0.0.1:5173")
                    .header("Access-Control-Request-Method", "GET")
                    .header("Access-Control-Request-Headers", "authorization"))
            .andReturn()
            .getResponse();
    assertEquals(200, response.getStatus());
    assertEquals("http://127.0.0.1:5173", response.getHeader("Access-Control-Allow-Origin"));
  }
}

