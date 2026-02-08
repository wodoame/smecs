package com.smecs.dao.impl;

import com.smecs.dao.ReviewDAO;
import com.smecs.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDAOImpl implements ReviewDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Review save(Review review) {
        if (review.getId() == null) {
            entityManager.persist(review);
            return review;
        } else {
            return entityManager.merge(review);
        }
    }

    @Override
    public Optional<Review> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Review.class, id));
    }

    @Override
    public Page<Review> findByProductId(Long productId, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Review> countRoot = countQuery.from(Review.class);
        countQuery.select(cb.count(countRoot))
                  .where(cb.equal(countRoot.get("product").get("id"), productId));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        // Data query
        CriteriaQuery<Review> query = cb.createQuery(Review.class);
        Root<Review> root = query.from(Review.class);
        query.where(cb.equal(root.get("product").get("id"), productId));
        query.orderBy(cb.desc(root.get("createdAt")));

        TypedQuery<Review> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<Review> content = typedQuery.getResultList();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Review review = entityManager.find(Review.class, id);
        if (review != null) {
            entityManager.remove(review);
        }
    }

    @Override
    public boolean existsById(Long id) {
        Long count = entityManager.createQuery("select count(r) from Review r where r.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }
}

