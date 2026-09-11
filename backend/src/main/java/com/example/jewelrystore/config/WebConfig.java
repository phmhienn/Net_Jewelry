package com.example.jewelrystore.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Value("${app.upload-dir}")
  private String directory;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String uri = Path.of(directory).toAbsolutePath().normalize().toUri().toString();
    registry
        .addResourceHandler("/uploads/**")
        .addResourceLocations(uri.endsWith("/") ? uri : uri + "/")
        .setCachePeriod(86400);
  }
}
