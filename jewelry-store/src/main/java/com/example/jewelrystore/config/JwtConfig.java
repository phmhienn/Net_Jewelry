package com.example.jewelrystore.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.cors.*;

@Configuration
public class JwtConfig {
  @Bean
  SecretKeySpec jwtKey(@Value("${app.jwt.secret}") String secret) {
    byte[] bytes;
    try {
      bytes = Base64.getDecoder().decode(secret);
    } catch (Exception e) {
      throw new IllegalStateException("JWT_SECRET must be base64", e);
    }
    if (bytes.length < 32)
      throw new IllegalStateException("JWT_SECRET needs at least 32 random bytes (base64 encoded)");
    return new SecretKeySpec(bytes, "HmacSHA256");
  }

  @Bean
  JwtEncoder jwtEncoder(SecretKeySpec key) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }

  @Bean
  JwtDecoder jwtDecoder(SecretKeySpec key) {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("jewelry-store"));
    return decoder;
  }
}
