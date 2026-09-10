package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.service.*;
import com.example.jewelrystore.util.Pages;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentServiceImpl implements ContentService {
  private final BannerRepository banners;
  private final NoiDungTrangRepository contents;

  private BannerResponse banner(Banner bn) {
    return new BannerResponse(
        bn.getId(),
        bn.getTitle(),
        bn.getImage(),
        bn.getLink(),
        bn.getSortOrder(),
        bn.getStatus(),
        bn.getStartsAt(),
        bn.getEndsAt());
  }

  private ContentResponse content(NoiDungTrang nd) {
    return new ContentResponse(
        nd.getId(), nd.getType(), nd.getTitle(), nd.getSlug(), nd.getContent(), nd.getStatus());
  }

  @Transactional(readOnly = true)
  public PageResponse<BannerResponse> banners(boolean admin, int page, int size) {
    var now = Instant.now();
    return PageResponse.of(
        banners
            .findAll(
                (root, query, cb) ->
                    admin
                        ? cb.conjunction()
                        : cb.and(
                            cb.equal(root.get("status"), ReviewStatus.HIEN_THI),
                            cb.or(
                                cb.isNull(root.get("startsAt")),
                                cb.lessThanOrEqualTo(root.get("startsAt"), now)),
                            cb.or(
                                cb.isNull(root.get("endsAt")),
                                cb.greaterThan(root.get("endsAt"), now))),
                Pages.of(page, size, Sort.by("sortOrder", "id")))
            .map(this::banner));
  }

  public BannerResponse saveBanner(Long id, BannerRequest request) {
    require(
        request.startsAt() == null
            || request.endsAt() == null
            || request.endsAt().isAfter(request.startsAt()),
        "Khoảng thời gian banner không hợp lệ");
    String link = request.link();
    require(
        link == null
            || link.isBlank()
            || (link.startsWith("/") && !link.startsWith("//") && !link.contains("\\")),
        "Liên kết banner phải là đường dẫn trong website");
    var bn = id == null ? new Banner() : lock(banners, id);
    bn.setTitle(request.title());
    bn.setImage(imageUrl(request.image()));
    bn.setLink(link);
    bn.setSortOrder(request.sortOrder());
    bn.setStatus(request.status());
    bn.setStartsAt(request.startsAt());
    bn.setEndsAt(request.endsAt());
    return banner(banners.save(bn));
  }

  public void deleteBanner(Long id) {
    banners.delete(get(banners, id));
  }

  @Transactional(readOnly = true)
  public PageResponse<ContentResponse> contents(
      boolean admin, ContentType type, int page, int size) {
    return PageResponse.of(
        contents
            .findAll(
                (root, query, cb) ->
                    cb.and(
                        admin
                            ? cb.conjunction()
                            : cb.equal(root.get("status"), ReviewStatus.HIEN_THI),
                        type == null ? cb.conjunction() : cb.equal(root.get("type"), type)),
                Pages.of(page, size))
            .map(this::content));
  }

  @Transactional(readOnly = true)
  public ContentResponse content(String slug) {
    var nd =
        contents
            .findBySlug(slug)
            .filter(value -> value.getStatus() == ReviewStatus.HIEN_THI)
            .orElseThrow(
                () ->
                    new com.example.jewelrystore.exception.ResourceNotFoundException(
                        "Chưa có nội dung"));
    return content(nd);
  }

  public ContentResponse saveContent(Long id, ContentRequest request) {
    contents
        .findBySlug(request.slug())
        .ifPresent(nd -> require(nd.getId().equals(id), "Slug đã tồn tại"));
    var nd = id == null ? new NoiDungTrang() : lock(contents, id);
    nd.setType(request.type());
    nd.setTitle(request.title());
    nd.setSlug(request.slug());
    nd.setContent(request.content());
    nd.setStatus(request.status());
    return content(contents.save(nd));
  }

  public void deleteContent(Long id) {
    contents.delete(get(contents, id));
  }
}
