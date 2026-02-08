package com.smecs.dao;

import com.smecs.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface ProductDAO {
    Product save(Product product);
    Optional<Product> findById(Long id);
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);
    boolean existsById(Long id);
    void deleteById(Long id);
    List<String> findTop5ImagesByCategoryId(Long categoryId);
}

