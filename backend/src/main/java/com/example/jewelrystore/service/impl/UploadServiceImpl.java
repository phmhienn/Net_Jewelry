package com.example.jewelrystore.service.impl;

import static com.example.jewelrystore.util.Checks.*;

import com.example.jewelrystore.dto.request.ImageRequest;
import com.example.jewelrystore.dto.response.*;
import com.example.jewelrystore.exception.*;
import com.example.jewelrystore.service.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UploadServiceImpl implements UploadService {
  private final ProductService catalog;
  private final ReviewService reviews;
  private final UserService accounts;

  @Value("${app.upload-dir}")
  private String directory;

  public ImageResponse product(Long id, MultipartFile file, boolean primary) {
    catalog.product(id, true);
    return catalog.addImage(id, new ImageRequest(store(file), primary, null));
  }

  public ImageResponse review(Long id, MultipartFile file) {
    reviews.checkOwner(id);
    return reviews.addImage(id, store(file));
  }

  public UserResponse avatar(MultipartFile file) {
    accounts.profile();
    return accounts.avatar(store(file));
  }

  private String store(MultipartFile file) {
    require(!file.isEmpty() && file.getSize() <= 5 * 1024 * 1024, "Ảnh phải từ 1 byte đến 5 MB");
    try (var input = ImageIO.createImageInputStream(file.getInputStream())) {
      var readers = ImageIO.getImageReaders(input);
      require(readers.hasNext(), "Tệp không phải ảnh JPEG hoặc PNG");
      var reader = readers.next();
      try {
        String format = reader.getFormatName().toLowerCase(Locale.ROOT);
        require(format.equals("jpeg") || format.equals("png"), "Chỉ hỗ trợ JPEG hoặc PNG");
        reader.setInput(input);
        int width = reader.getWidth(0), height = reader.getHeight(0);
        require(
            width > 0
                && height > 0
                && width <= 6000
                && height <= 6000
                && (long) width * height <= 20000000,
            "Kích thước ảnh tối đa 6000 px và 20 megapixel");
        var image = reader.read(0);
        String extension = format.equals("jpeg") ? "jpg" : "png";
        Path root = Path.of(directory).toAbsolutePath().normalize();
        Files.createDirectories(root);
        String name = UUID.randomUUID() + "." + extension;
        Path target = root.resolve(name);
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
              @Override
              public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED)
                  try {
                    Files.deleteIfExists(target);
                  } catch (Exception e) {
                    org.slf4j.LoggerFactory.getLogger(UploadServiceImpl.class)
                        .warn("Cannot remove rolled-back upload {}", target, e);
                  }
              }
            });
        require(ImageIO.write(image, extension, target.toFile()), "Không thể xử lý ảnh");
        return "/uploads/" + name;
      } finally {
        reader.dispose();
      }
    } catch (java.io.IOException e) {
      throw new BadRequestException("Không thể đọc hoặc lưu ảnh");
    }
  }
}
