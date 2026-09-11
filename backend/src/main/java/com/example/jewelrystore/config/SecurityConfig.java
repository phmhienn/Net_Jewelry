package com.example.jewelrystore.config;

import com.example.jewelrystore.exception.GlobalExceptionHandler;
import com.example.jewelrystore.security.JwtTokenProvider;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain security(
      HttpSecurity http,
      JwtTokenProvider jwt,
      ObjectMapper json,
      @org.springframework.beans.factory.annotation.Qualifier("cors") CorsConfigurationSource cors)
      throws Exception {
    http.cors(c -> c.configurationSource(cors))
        .csrf(c -> c.disable())
        .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.authorizeHttpRequests(
        a ->
            a.requestMatchers("/error", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                .permitAll()
                .requestMatchers(
                    org.springframework.http.HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/auth/staff/login",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password")
                .permitAll()
                .requestMatchers(
                    org.springframework.http.HttpMethod.GET,
                    "/api/products/**",
                    "/api/categories/**",
                    "/api/brands/**",
                    "/api/gold-prices/**",
                    "/uploads/**",
                    "/api/storefront/settings",
                    "/api/banners",
                    "/api/contents/**")
                .permitAll()
                .requestMatchers(
                    "/api/admin/coupons/**",
                    "/api/admin/banners/**",
                    "/api/admin/contents/**",
                    "/api/admin/staff/**",
                    "/api/admin/customers/**",
                    "/api/admin/statistics/**")
                .hasRole("QUAN_LY")
                .requestMatchers("/api/admin/**", "/api/inventory/**")
                .hasAnyRole("NHAN_VIEN", "QUAN_LY")
                .requestMatchers("/api/categories/**", "/api/brands/**", "/api/gold-prices/**")
                .hasRole("QUAN_LY")
                .requestMatchers("/api/products/**", "/api/variants/**", "/api/product-images/**")
                .authenticated()
                .requestMatchers(
                    "/api/wishlist/**",
                    "/api/coupons/**",
                    "/api/cart/**",
                    "/api/addresses/**",
                    "/api/orders/**",
                    "/api/payments/**",
                    "/api/reviews/**",
                    "/api/review-images/**")
                .hasRole("KHACH_HANG")
                .requestMatchers("/api/auth/**", "/api/account/**")
                .authenticated()
                .anyRequest()
                .denyAll());
    var entry =
        (org.springframework.security.web.AuthenticationEntryPoint)
            (req, res, e) -> {
              res.setStatus(401);
              res.setContentType("application/json;charset=UTF-8");
              json.writeValue(
                  res.getWriter(),
                  GlobalExceptionHandler.body(401, "Vui lòng đăng nhập", req.getRequestURI()));
            };
    http.exceptionHandling(
        c ->
            c.authenticationEntryPoint(entry)
                .accessDeniedHandler(
                    (req, res, e) -> {
                      res.setStatus(403);
                      res.setContentType("application/json;charset=UTF-8");
                      json.writeValue(
                          res.getWriter(),
                          GlobalExceptionHandler.body(
                              403, "Bạn không có quyền truy cập", req.getRequestURI()));
                    }));
    http.oauth2ResourceServer(
        c ->
            c.authenticationEntryPoint(entry)
                .jwt(j -> j.jwtAuthenticationConverter(jwt::authenticate)));
    return http.build();
  }
}
