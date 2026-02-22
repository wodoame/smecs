package com.smecs.repository;

import com.smecs.entity.Category;
import com.smecs.entity.Product;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterByCriteria(String name, String description) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> textPredicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                textPredicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
                ));
            }

            if (description != null && !description.isBlank()) {
                textPredicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + description.toLowerCase() + "%"
                ));
            }

            if (textPredicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            // Include category name/description matching by checking the linked category row.
            Subquery<Long> categoryMatchSubquery = query.subquery(Long.class);
            Root<Category> categoryRoot = categoryMatchSubquery.from(Category.class);

            List<Predicate> categoryTextPredicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                categoryTextPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(categoryRoot.get("name")),
                        "%" + name.toLowerCase() + "%"
                ));
            }
            if (description != null && !description.isBlank()) {
                categoryTextPredicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(categoryRoot.get("description")),
                        "%" + description.toLowerCase() + "%"
                ));
            }

            categoryMatchSubquery.select(categoryRoot.get("id")).where(
                    criteriaBuilder.and(
                            criteriaBuilder.equal(categoryRoot.get("id"), root.get("categoryId")),
                            criteriaBuilder.or(categoryTextPredicates.toArray(new Predicate[0]))
                    )
            );

            textPredicates.add(criteriaBuilder.exists(categoryMatchSubquery));
            return criteriaBuilder.or(textPredicates.toArray(new Predicate[0]));
        };
    }
}
