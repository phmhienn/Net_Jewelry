package com.example.jewelrystore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.jewelrystore.entity.DanhGia;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BasicType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class JewelryStoreApplicationTests {
  @Autowired private EntityManagerFactory entityManagerFactory;

  @Test
  void contextLoads() {}

  @Test
  void reviewStarsUsesTinyintJdbcMapping() {
    var sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
    var entity = sessionFactory.getMappingMetamodel().getEntityDescriptor(DanhGia.class);
    var starsType = (BasicType<?>) entity.getPropertyType("stars");
    assertEquals(java.sql.Types.TINYINT, starsType.getJdbcType().getJdbcTypeCode());
  }
}
