package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.service.GoldPriceService;
import com.example.jewelrystore.util.Pages;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GoldPriceServiceImpl implements GoldPriceService {
  private final GiaVangRepository repo;
  private final GoldPriceQueries queries;
  private final GoldPriceMapper goldPriceMapper;

  @Transactional(readOnly = true)
  public List<GoldPriceResponse> current() {
    return queries.current().stream().map(goldPriceMapper::gold).toList();
  }

  @Transactional(readOnly = true)
  public PageResponse<GoldPriceResponse> history(
      String type, LocalDate from, LocalDate to, int page, int size) {
    require(from == null || to == null || !from.isAfter(to), "Khoảng ngày không hợp lệ");
    return PageResponse.of(
        repo.findAll(
                (root, criteriaQuery, cb) -> {
                  var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
                  if (type != null) predicates.add(cb.equal(root.get("type"), type));
                  if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
                  if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));
                  return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
                },
                Pages.of(page, size, Sort.by("date").descending().and(Sort.by("id").descending())))
            .map(goldPriceMapper::gold));
  }

  public GoldPriceResponse save(Long id, GoldPriceRequest request) {
    require(
        request.buyPrice().compareTo(request.sellPrice()) <= 0,
        "Giá mua không được cao hơn giá bán");
    GiaVang gv = id == null ? new GiaVang() : lock(repo, id);
    if (id == null || !gv.getType().equals(request.type()) || !gv.getDate().equals(request.date()))
      require(
          !repo.existsByTypeAndDate(request.type(), request.date()),
          "Đã có giá vàng loại này trong ngày");
    gv.setType(request.type());
    gv.setBuyPrice(request.buyPrice());
    gv.setSellPrice(request.sellPrice());
    gv.setDate(request.date());
    gv.setUnit(request.unit());
    gv.setUpdatedAt(Instant.now());
    return goldPriceMapper.gold(repo.save(gv));
  }

  public void delete(Long id) {
    repo.delete(get(repo, id));
  }
}
