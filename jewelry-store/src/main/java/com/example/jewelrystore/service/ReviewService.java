package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;

public interface ReviewService {
  PageResponse<ReviewResponse> list(Long productId, boolean admin, int page, int size);

  ReviewResponse create(Long productId, ReviewRequest request);

  ReviewResponse update(Long id, ReviewRequest request);

  void delete(Long id, boolean admin);

  ReviewResponse reply(Long id, ReplyRequest request);

  ReviewResponse status(Long id, ReviewStatusRequest request);

  ImageResponse addImage(Long id, String url);

  void deleteImage(Long id);

  void checkOwner(Long id);
}
