package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.request.*;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.entity.*;
import com.example.jewelrystore.entity.enums.DomainEnums.*;
import com.example.jewelrystore.repository.*;
import java.util.*;

public interface ContentService {
  PageResponse<BannerResponse> banners(boolean admin, int page, int size);

  BannerResponse saveBanner(Long id, BannerRequest request);

  void deleteBanner(Long id);

  PageResponse<ContentResponse> contents(boolean admin, ContentType type, int page, int size);

  ContentResponse content(String slug);

  ContentResponse saveContent(Long id, ContentRequest request);

  void deleteContent(Long id);
}
