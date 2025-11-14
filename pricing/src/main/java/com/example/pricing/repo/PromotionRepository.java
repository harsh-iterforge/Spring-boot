// src/main/java/com/example/pricing/repo/PromotionRepository.java
package com.example.pricing.repo;

import com.example.pricing.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByActiveTrueAndStartAtBeforeAndEndAtAfter(Instant now1, Instant now2);
}
