package com.smecs.dao;

import com.smecs.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface CategoryDAO {
    Category save(Category category);
    Optional<Category> findById(Long id);
    Page<Category> findAll(Specification<Category> spec, Pageable pageable);
    Page<Category> searchCategories(String nameQuery, String descriptionQuery, Pageable pageable);
    boolean existsById(Long id);
    void deleteById(Long id);
}

