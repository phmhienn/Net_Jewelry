package com.example.jewelrystore.security;

import com.example.jewelrystore.exception.*;
import org.springframework.security.core.context.SecurityContextHolder;

@org.springframework.stereotype.Component
public final class CurrentActor {
  public Actor get() {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof Actor actor))
      throw new UnauthorizedException("Vui lòng đăng nhập");
    return actor;
  }

  public Long customerId() {
    Actor currentActor = get();
    if (!currentActor.customer()) throw new ForbiddenException("Chỉ dành cho khách hàng");
    return currentActor.id();
  }
}
