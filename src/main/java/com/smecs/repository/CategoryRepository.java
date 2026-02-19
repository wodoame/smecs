package com.smecs.repository;

import com.smecs.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    @Query(value = "SELECT p.image_url FROM products p WHERE p.category_id = :categoryId ORDER BY p.id ASC LIMIT 5", nativeQuery = true)
    List<String> findTop5ProductImageUrlsByCategoryId(@Param("categoryId") Long categoryId);

    @Query(value = "SELECT COUNT(*) FROM products WHERE category_id = :categoryId", nativeQuery = true)
    long countProductsByCategoryId(@Param("categoryId") Long categoryId);
}
