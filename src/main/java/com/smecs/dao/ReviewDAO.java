package com.smecs.dao;

import com.smecs.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReviewDAO {
    Review save(Review review);
    Optional<Review> findById(Long id);
    Page<Review> findByProductId(Long productId, Pageable pageable);
    void deleteById(Long id);
    boolean existsById(Long id);
}

