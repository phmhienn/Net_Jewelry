package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.mapper.*;
import com.example.jewelrystore.repository.*;
import java.util.*;

public interface CustomerService {
  PageResponse<UserResponse> customers(String keyword, int page, int size);

  CustomerSummaryResponse customer(Long id);

  UserResponse createCustomer(CustomerCreateRequest request);

  UserResponse updateCustomer(Long id, CustomerUpdateRequest request);

  UserResponse customerStatus(Long id, AccountStatusRequest request);

  void deleteCustomer(Long id);
}
