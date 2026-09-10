package com.example.jewelrystore.util;

import com.example.jewelrystore.exception.BadRequestException;
import org.springframework.data.domain.*;

public final class Pages {
  private Pages() {}

  public static Pageable of(int page, int size) {
    return of(page, size, Sort.by(Sort.Direction.DESC, "id"));
  }

  public static Pageable of(int page, int size, Sort sort) {
    if (page < 0 || size < 1 || size > 100)
      throw new BadRequestException("page >= 0, size từ 1 đến 100");
    return PageRequest.of(page, size, sort);
  }
}
