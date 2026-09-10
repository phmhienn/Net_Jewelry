package com.example.jewelrystore.security;

import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.repository.*;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenProvider {
  private final JwtEncoder encoder;
  private final TaiKhoanRepository customers;

  private final long ttl;

  public JwtTokenProvider(
      JwtEncoder encoder,
      TaiKhoanRepository customers,
      @Value("${app.jwt.ttl-minutes}") long minutes) {
    this.encoder = encoder;
    this.customers = customers;
    this.ttl = minutes * 60;
    if (ttl < 60 || ttl > 86400)
      throw new IllegalArgumentException("JWT TTL must be 1–1440 minutes");
  }

  public long ttl() {
    return ttl;
  }

  public String issue(TaiKhoan user, Role role) {
    Instant now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .issuer("jewelry-store")
            .subject("account:" + user.getId())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ttl))
            .claim("version", user.getTokenVersion())
            .build();
    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }

  public UsernamePasswordAuthenticationToken authenticate(Jwt jwt) {
    try {
      String[] parts = jwt.getSubject().split(":");
      Long id = Long.valueOf(parts[1]);
      if (parts.length != 2 || !parts[0].equals("account")) throw new IllegalArgumentException();
      TaiKhoan account = customers.findById(id).orElseThrow();
      Number version = jwt.getClaim("version");
      if (account.getStatus() != AccountStatus.HOAT_DONG
          || version == null
          || account.getTokenVersion() != version.longValue()) throw new IllegalArgumentException();
      Role role = account.getRole();
      return new UsernamePasswordAuthenticationToken(
          new Actor(id, role), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    } catch (Exception e) {
      throw new OAuth2AuthenticationException("invalid_token");
    }
  }
}
