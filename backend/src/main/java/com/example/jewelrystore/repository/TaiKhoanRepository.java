package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.TaiKhoan;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface TaiKhoanRepository extends BaseRepository<TaiKhoan> {
  Optional<TaiKhoan> findByEmailIgnoreCase(String email);

  Optional<TaiKhoan> findByUsernameIgnoreCase(String username);

  Optional<TaiKhoan> findByEmail(String email);

  Optional<TaiKhoan> findByUsername(String username);

  Optional<TaiKhoan> findByResetTokenHash(String hash);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);

  long countByAuthorityNameNot(String name);

  long countByAuthorityName(String name);

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select tk from TaiKhoan tk where tk.authority.name <> 'KHACH_HANG' order by tk.id")
  List<TaiKhoan> lockAll();
}
