package com.example.jewelrystore.repository;

import com.example.jewelrystore.dto.response.ReportRow;
import com.example.jewelrystore.entity.DonHang;
import com.example.jewelrystore.entity.enums.DomainEnums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StatisticsRepository extends BaseRepository<DonHang> {
  long countByDateGreaterThanEqualAndDateLessThan(Instant from, Instant to);

  long countByStatusAndDateGreaterThanEqualAndDateLessThan(
      OrderStatus status, Instant from, Instant to);

  @Query(
      "select coalesce(sum(o.subtotal-o.discount),0) from DonHang o where o.status=:status and o.completedAt>=:from and o.completedAt<:to")
  BigDecimal revenue(
      @Param("status") OrderStatus status, @Param("from") Instant from, @Param("to") Instant to);

  @Query(
      value =
          "select new com.example.jewelrystore.dto.response.ReportRow(cast(cast(o.completedAt as date) as string),cast(cast(o.completedAt as date) as string),count(o),sum(o.subtotal-o.discount)) from DonHang o where o.status=:status and o.completedAt>=:from and o.completedAt<:to group by cast(cast(o.completedAt as date) as string) order by cast(cast(o.completedAt as date) as string)",
      countQuery =
          "select count(distinct cast(o.completedAt as date)) from DonHang o where o.status=:status and o.completedAt>=:from and o.completedAt<:to")
  Page<ReportRow> byDate(
      @Param("status") OrderStatus status,
      @Param("from") Instant from,
      @Param("to") Instant to,
      Pageable page);

  @Query(
      value =
          "select new com.example.jewelrystore.dto.response.ReportRow(cast(i.variant.product.category.id as string),i.variant.product.category.name,sum(i.quantity),sum(i.total * (i.order.subtotal-i.order.discount)/i.order.subtotal)) from ChiTietDonHang i where i.order.status=:status and i.order.completedAt>=:from and i.order.completedAt<:to group by i.variant.product.category.id,i.variant.product.category.name order by sum(i.total * (i.order.subtotal-i.order.discount)/i.order.subtotal) desc",
      countQuery =
          "select count(distinct i.variant.product.category.id) from ChiTietDonHang i where i.order.status=:status and i.order.completedAt>=:from and i.order.completedAt<:to")
  Page<ReportRow> byCategory(
      @Param("status") OrderStatus status,
      @Param("from") Instant from,
      @Param("to") Instant to,
      Pageable page);

  @Query(
      value =
          "select new com.example.jewelrystore.dto.response.ReportRow(cast(p.id as string),p.name,coalesce(sum(case when o.id is not null then i.quantity else 0 end),0),coalesce(sum(case when o.id is not null then i.total * (o.subtotal-o.discount)/o.subtotal else 0 end),0)) from SanPham p left join BienTheSanPham v on v.product=p left join ChiTietDonHang i on i.variant=v left join DonHang o on o=i.order and o.status=:status and o.completedAt>=:from and o.completedAt<:to group by p.id,p.name order by case when :slow=true then coalesce(sum(case when o.id is not null then i.quantity else 0 end),0) else -coalesce(sum(case when o.id is not null then i.quantity else 0 end),0) end,p.id",
      countQuery = "select count(p) from SanPham p")
  Page<ReportRow> byProduct(
      @Param("status") OrderStatus status,
      @Param("from") Instant from,
      @Param("to") Instant to,
      @Param("slow") boolean slow,
      Pageable page);

  @Query(
      "select count(c) from TaiKhoan c where c.authority.name='KHACH_HANG' and c.createdAt>=:from and c.createdAt<:to")
  long newCustomers(@Param("from") Instant from, @Param("to") Instant to);

  @Query(
      "select count(c) from TaiKhoan c where (select count(o) from DonHang o where o.customer=c and o.status=:status and o.completedAt<:to)>=2 and exists(select o2.id from DonHang o2 where o2.customer=c and o2.status=:status and o2.completedAt>=:from and o2.completedAt<:to)")
  long returningCustomers(
      @Param("status") OrderStatus status, @Param("from") Instant from, @Param("to") Instant to);

  @Query(
      value =
          "select new com.example.jewelrystore.dto.response.ReportRow(cast(o.customer.id as string),o.customer.name,count(o),sum(o.total)) from DonHang o where o.status=:status and o.completedAt>=:from and o.completedAt<:to group by o.customer.id,o.customer.name order by sum(o.total) desc,o.customer.id",
      countQuery =
          "select count(distinct o.customer.id) from DonHang o where o.status=:status and o.completedAt>=:from and o.completedAt<:to")
  Page<ReportRow> topCustomers(
      @Param("status") OrderStatus status,
      @Param("from") Instant from,
      @Param("to") Instant to,
      Pageable page);
}
