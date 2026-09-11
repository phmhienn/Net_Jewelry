package com.example.jewelrystore.service;

import com.example.jewelrystore.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
  ImageResponse product(Long id, MultipartFile file, boolean primary);

  ImageResponse review(Long id, MultipartFile file);

  UserResponse avatar(MultipartFile file);
}
