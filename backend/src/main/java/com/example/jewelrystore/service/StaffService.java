package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;

public interface StaffService {
  PageResponse<StaffResponse> list(String keyword, int page, int size);

  StaffResponse get(Long id);

  StaffResponse create(StaffRequest request);

  StaffResponse update(Long id, StaffUpdateRequest request);

  StaffResponse role(Long id, RoleRequest request);

  StaffResponse status(Long id, AccountStatusRequest request);

  void delete(Long id);
}
