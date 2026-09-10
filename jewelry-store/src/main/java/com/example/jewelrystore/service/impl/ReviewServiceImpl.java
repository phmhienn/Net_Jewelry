package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.ReviewService;
import com.example.jewelrystore.util.Pages;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {
  private final DanhGiaRepository reviews;
  private final HinhAnhDanhGiaRepository images;
  private final SanPhamRepository products;
  private final TaiKhoanRepository customers;
  private final ChiTietDonHangRepository orderItems;
  private final ReviewMapper reviewMapper;
  private final CurrentActor actor;

  @Transactional(readOnly = true)
  public PageResponse<ReviewResponse> list(Long productId, boolean admin, int page, int size) {
    if (productId != null) get(products, productId);
    return PageResponse.of(
        reviews
            .findAll(
                (root, criteriaQuery, cb) -> {
                  var predicates =
                      new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
                  if (productId != null)
                    predicates.add(cb.equal(root.get("product").get("id"), productId));
                  if (!admin) predicates.add(cb.equal(root.get("status"), ReviewStatus.HIEN_THI));
                  return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
                },
                Pages.of(page, size))
            .map(reviewMapper::review));
  }

  public ReviewResponse create(Long productId, ReviewRequest request) {
    var customer = lock(customers, actor.customerId());
    var product = get(products, productId);
    require(
        orderItems.existsByVariantProductIdAndOrderCustomerIdAndOrderStatus(
            productId, customer.getId(), OrderStatus.HOAN_THANH),
        "Chỉ đánh giá sản phẩm trong đơn đã hoàn tất");
    require(
        !reviews.existsByProductIdAndCustomerId(productId, customer.getId()),
        "Bạn đã đánh giá sản phẩm này");
    DanhGia dg = new DanhGia();
    dg.setCustomer(customer);
    dg.setProduct(product);
    dg.setOrder(
        orderItems
            .findFirstByVariantProductIdAndOrderCustomerIdAndOrderStatusOrderByIdDesc(
                productId, customer.getId(), OrderStatus.HOAN_THANH)
            .orElseThrow()
            .getOrder());
    dg.setStars((byte) request.stars());
    dg.setContent(request.content());
    return reviewMapper.review(reviews.save(dg));
  }

  public void checkOwner(Long id) {
    owner(get(reviews, id).getCustomer().getId(), actor.customerId());
  }

  public ReviewResponse update(Long id, ReviewRequest request) {
    DanhGia dg = lock(reviews, id);
    owner(dg.getCustomer().getId(), actor.customerId());
    dg.setStars((byte) request.stars());
    dg.setContent(request.content());
    dg.setUpdatedAt(Instant.now());
    return reviewMapper.review(dg);
  }

  public void delete(Long id, boolean admin) {
    DanhGia dg = lock(reviews, id);
    if (!admin) owner(dg.getCustomer().getId(), actor.customerId());
    images.deleteByReviewId(id);
    reviews.delete(dg);
  }

  public ReviewResponse reply(Long id, ReplyRequest request) {
    DanhGia dg = lock(reviews, id);
    dg.setReply(request.reply());
    dg.setUpdatedAt(Instant.now());
    return reviewMapper.review(dg);
  }

  public ReviewResponse status(Long id, ReviewStatusRequest request) {
    DanhGia dg = lock(reviews, id);
    dg.setStatus(request.status());
    dg.setUpdatedAt(Instant.now());
    return reviewMapper.review(dg);
  }

  public ImageResponse addImage(Long id, String url) {
    var review = lock(reviews, id);
    owner(review.getCustomer().getId(), actor.customerId());
    require(images.findByReviewId(id).size() < 5, "Tối đa 5 ảnh đánh giá");
    HinhAnhDanhGia hadg = new HinhAnhDanhGia();
    hadg.setReview(review);
    hadg.setUrl(imageUrl(url));
    return reviewMapper.image(images.save(hadg));
  }

  public void deleteImage(Long id) {
    HinhAnhDanhGia hadg = get(images, id);
    lock(reviews, hadg.getReview().getId());
    owner(hadg.getReview().getCustomer().getId(), actor.customerId());
    images.delete(hadg);
  }
}
