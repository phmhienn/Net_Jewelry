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
public class ReviewMapper {
  private final HinhAnhDanhGiaRepository reviewImages;

  public ImageResponse image(HinhAnhDanhGia hadg) {
    return new ImageResponse(hadg.getId(), hadg.getUrl(), false, hadg.getSortOrder());
  }

  public ReviewResponse review(DanhGia dg) {
    return new ReviewResponse(
        dg.getId(),
        dg.getProduct().getId(),
        dg.getCustomer().getId(),
        dg.getCustomer().getName(),
        dg.getStars(),
        dg.getContent(),
        dg.getCreatedAt(),
        dg.getUpdatedAt(),
        dg.getStatus(),
        dg.getReply(),
        reviewImages.findByReviewId(dg.getId()).stream().map(this::image).toList());
  }
}
