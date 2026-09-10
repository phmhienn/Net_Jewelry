package com.example.jewelrystore.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI api() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Jewelry Store REST API")
                .version("1.0")
                .description(
                    "REST API theo database.sql 25 bảng. Đặt hàng COD; phân trang bắt đầu từ 0. KHACH_HANG, NHAN_VIEN, QUAN_LY. Xem README/API.md để biết phân quyền và luồng nghiệp vụ."))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }

  @Bean
  OperationCustomizer standardErrors() {
    return (operation, handler) -> {
      for (var code : new String[] {"400", "401", "403", "404", "409", "500"})
        operation
            .getResponses()
            .addApiResponse(
                code,
                new io.swagger.v3.oas.models.responses.ApiResponse()
                    .description(
                        switch (code) {
                          case "400" -> "Request hoặc validation không hợp lệ";
                          case "401" -> "Chưa đăng nhập hoặc JWT hết hiệu lực";
                          case "403" -> "Không đủ quyền hoặc không sở hữu dữ liệu";
                          case "404" -> "Không tìm thấy";
                          case "409" -> "Xung đột dữ liệu, tồn kho hoặc trạng thái";
                          default -> "Lỗi máy chủ";
                        }));
      return operation;
    };
  }
}
