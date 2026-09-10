package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import com.example.jewelrystore.security.CurrentActor;
import com.example.jewelrystore.service.AddressService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {
  private final TaiKhoanRepository customers;
  private final DiaChiRepository addresses;
  private final AddressMapper addressMapper;
  private final CurrentActor actor;

  private Long customerId(Long requested) {
    if (requested == null) return actor.customerId();
    require(actor.get().role() == Role.QUAN_LY, "Chỉ quản lý được truy cập khách hàng khác");
    require(
        get(customers, requested).getRole() == Role.KHACH_HANG, "Tài khoản không phải khách hàng");
    return requested;
  }

  @Transactional(readOnly = true)
  public List<AddressResponse> addresses(Long customerId) {
    Long id = customerId(customerId);
    get(customers, id);
    return addresses.findByCustomerIdOrderById(id).stream().map(addressMapper::address).toList();
  }

  public AddressResponse saveAddress(Long customerId, Long id, AddressRequest request) {
    Long ownerId = customerId(customerId);
    var customer = lock(customers, ownerId);
    var all = addresses.findByCustomerIdOrderById(ownerId);
    DiaChi dc = id == null ? new DiaChi() : get(addresses, id);
    if (id != null) owner(dc.getCustomer().getId(), ownerId);
    require(id != null || all.size() < 20, "Tối đa 20 địa chỉ");
    boolean primary = request.defaultAddress() || all.isEmpty();
    if (primary) all.forEach(diaChiKhac -> diaChiKhac.setDefaultAddress(false));
    else if (dc.isDefaultAddress())
      require(
          all.stream()
              .anyMatch(
                  diaChiKhac -> !diaChiKhac.getId().equals(id) && diaChiKhac.isDefaultAddress()),
          "Hãy chọn địa chỉ mặc định khác trước");
    dc.setCustomer(customer);
    dc.setName(request.name());
    dc.setPhone(request.phone());
    dc.setCity(request.city());
    dc.setDistrict(request.district());
    dc.setWard(request.ward());
    dc.setStreet(request.street());
    dc.setDefaultAddress(primary);
    return addressMapper.address(addresses.save(dc));
  }

  public void deleteAddress(Long customerId, Long id) {
    Long ownerId = customerId(customerId);
    lock(customers, ownerId);
    DiaChi dc = get(addresses, id);
    owner(dc.getCustomer().getId(), ownerId);
    boolean primary = dc.isDefaultAddress();
    addresses.delete(dc);
    addresses.flush();
    if (primary)
      addresses.findByCustomerIdOrderById(ownerId).stream()
          .findFirst()
          .ifPresent(diaChiMacDinh -> diaChiMacDinh.setDefaultAddress(true));
  }

  public AddressResponse defaultAddress(Long customerId, Long id) {
    Long ownerId = customerId(customerId);
    lock(customers, ownerId);
    DiaChi dc = get(addresses, id);
    owner(dc.getCustomer().getId(), ownerId);
    addresses
        .findByCustomerIdOrderById(ownerId)
        .forEach(diaChiKhac -> diaChiKhac.setDefaultAddress(diaChiKhac.getId().equals(id)));
    return addressMapper.address(dc);
  }
}
