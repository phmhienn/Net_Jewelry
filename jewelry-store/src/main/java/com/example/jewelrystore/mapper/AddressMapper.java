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
public class AddressMapper {

  public AddressResponse address(DiaChi dc) {
    return new AddressResponse(
        dc.getId(),
        dc.getName(),
        dc.getPhone(),
        dc.getCity(),
        dc.getDistrict(),
        dc.getWard(),
        dc.getStreet(),
        dc.isDefaultAddress());
  }
}
