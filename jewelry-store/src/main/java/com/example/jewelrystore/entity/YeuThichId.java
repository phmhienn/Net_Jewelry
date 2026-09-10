package com.example.jewelrystore.entity;

import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YeuThichId implements Serializable {
  private Long customer;
  private Long product;
}
