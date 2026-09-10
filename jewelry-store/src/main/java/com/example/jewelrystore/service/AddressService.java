package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import java.util.*;

public interface AddressService {
  List<AddressResponse> addresses(Long customerId);

  AddressResponse saveAddress(Long customerId, Long id, AddressRequest request);

  void deleteAddress(Long customerId, Long id);

  AddressResponse defaultAddress(Long customerId, Long id);
}
