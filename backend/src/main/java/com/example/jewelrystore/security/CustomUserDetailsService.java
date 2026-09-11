package com.example.jewelrystore.security;

import com.example.jewelrystore.entity.TaiKhoan;
import com.example.jewelrystore.entity.enums.DomainEnums.AccountStatus;
import com.example.jewelrystore.repository.TaiKhoanRepository;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {
  private final TaiKhoanRepository accounts;

  public Optional<TaiKhoan> findAccount(String identifier) {
    String login = identifier.trim();
    return login.contains("@")
        ? accounts.findByEmailIgnoreCase(login.toLowerCase(Locale.ROOT))
        : accounts.findByUsernameIgnoreCase(login);
  }

  @Override
  public UserDetails loadUserByUsername(String identifier) {
    TaiKhoan tk =
        findAccount(identifier)
            .orElseThrow(() -> new UsernameNotFoundException("Thông tin đăng nhập không đúng"));
    return User.withUsername(tk.getUsername())
        .password(tk.getPassword())
        .roles(tk.getRole().name())
        .accountLocked(tk.getStatus() != AccountStatus.HOAT_DONG)
        .build();
  }
}
