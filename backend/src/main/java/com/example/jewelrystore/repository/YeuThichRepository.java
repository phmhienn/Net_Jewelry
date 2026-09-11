package com.example.jewelrystore.repository;

import com.example.jewelrystore.entity.*;
import org.springframework.data.jpa.repository.*;

public interface YeuThichRepository
    extends JpaRepository<YeuThich, YeuThichId>, JpaSpecificationExecutor<YeuThich> {}
