package com.example.jewelrystore.config;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import java.util.*;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiAccessDocumentation {
  @Bean
  GlobalOpenApiCustomizer accessDocumentation() {
    return api ->
        api.getPaths()
            .forEach(
                (path, item) ->
                    item.readOperationsMap()
                        .forEach(
                            (method, operation) -> {
                              String verb = method.name();
                              List<String> roles = roles(path, verb);
                              operation.setSecurity(
                                  roles.isEmpty()
                                      ? List.of()
                                      : List.of(new SecurityRequirement().addList("bearerAuth")));
                              operation.addExtension("x-roles", roles);
                              operation.setDescription(
                                  (roles.isEmpty()
                                          ? "Public."
                                          : "Yêu cầu JWT. Vai trò: "
                                              + String.join(", ", roles)
                                              + ".")
                                      + " Response dùng ApiResponse; page bắt đầu từ 0, size tối đa 100. "
                                      + (operation.getDescription() == null
                                          ? ""
                                          : operation.getDescription()));
                            }));
  }

  private List<String> roles(String path, String method) {
    if (path.startsWith("/api/auth/")
        && Set.of(
                "/api/auth/register",
                "/api/auth/login",
                "/api/auth/staff/login",
                "/api/auth/forgot-password",
                "/api/auth/reset-password")
            .contains(path)) return List.of();
    if (method.equals("GET")
        && (path.startsWith("/api/banners")
            || path.startsWith("/api/contents")
            || path.startsWith("/api/storefront")
            || path.startsWith("/api/products")
            || path.startsWith("/api/categories")
            || path.startsWith("/api/brands")
            || path.startsWith("/api/gold-prices"))) return List.of();
    if (path.startsWith("/api/admin/coupons")
        || path.startsWith("/api/admin/banners")
        || path.startsWith("/api/admin/contents")
        || path.startsWith("/api/admin/staff")
        || path.startsWith("/api/admin/customers")
        || path.startsWith("/api/admin/statistics")
        || path.startsWith("/api/categories")
        || path.startsWith("/api/brands")
        || path.startsWith("/api/gold-prices")) return List.of("QUAN_LY");
    if (path.startsWith("/api/admin") || path.startsWith("/api/inventory"))
      return List.of("NHAN_VIEN", "QUAN_LY");
    if (path.contains("/reviews") || path.startsWith("/api/review-images"))
      return List.of("KHACH_HANG");
    if (path.startsWith("/api/storefront")
        || path.startsWith("/api/products")
        || path.startsWith("/api/variants")
        || path.startsWith("/api/product-images")) return List.of("NHAN_VIEN", "QUAN_LY");
    if (path.startsWith("/api/auth") || path.startsWith("/api/account"))
      return List.of("KHACH_HANG", "NHAN_VIEN", "QUAN_LY");
    return List.of("KHACH_HANG");
  }
}
