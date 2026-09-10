package com.example.jewelrystore.mapper;

import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  public UserResponse user(TaiKhoan kh) {
    return new UserResponse(
        kh.getId(),
        kh.getName(),
        kh.getEmail(),
        kh.getPhone(),
        kh.getAvatar(),
        kh.getRole(),
        kh.getStatus(),
        kh.getCreatedAt());
  }

  public StaffResponse staff(TaiKhoan nv) {
    return new StaffResponse(
        nv.getId(),
        nv.getName(),
        nv.getEmail(),
        nv.getPhone(),
        nv.getUsername(),
        nv.getRole(),
        nv.getStatus(),
        nv.getCreatedAt());
  }
}
