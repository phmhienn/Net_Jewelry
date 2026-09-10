package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.UserService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
  private final TaiKhoanRepository customers;
  private final UserMapper userMapper;
  private final CurrentActor actor;

  @Transactional(readOnly = true)
  public UserResponse profile() {
    return userMapper.user(get(customers, actor.get().id()));
  }

  public UserResponse update(ProfileRequest request) {
    TaiKhoan kh = lock(customers, actor.get().id());
    kh.setName(request.name());
    kh.setPhone(request.phone());
    return userMapper.user(kh);
  }

  public UserResponse avatar(String url) {
    TaiKhoan kh = lock(customers, actor.get().id());
    kh.setAvatar(imageUrl(url));
    return userMapper.user(kh);
  }
}
