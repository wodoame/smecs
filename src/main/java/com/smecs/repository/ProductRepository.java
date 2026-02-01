package com.smecs.repository;

import com.smecs.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Query("SELECT p.imageUrl FROM Product p WHERE p.category.id = :categoryId AND p.imageUrl IS NOT NULL ORDER BY p.id DESC LIMIT 5")
    List<String> findTop5ImagesByCategoryId(@Param("categoryId") Long categoryId);
}
