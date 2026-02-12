package com.smecs.dao.impl;

import com.smecs.dao.CartItemDAO;
import com.smecs.entity.CartItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class CartItemDAOImpl implements CartItemDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public CartItem save(CartItem cartItem) {
        if (cartItem.getCartItemId() == null) {
            entityManager.persist(cartItem);
            return cartItem;
        } else {
            return entityManager.merge(cartItem);
        }
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        return Optional.ofNullable(entityManager.find(CartItem.class, id));
    }

    @Override
    public List<CartItem> findByCartId(Long cartId) {
        return entityManager.createQuery("SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId", CartItem.class)
                .setParameter("cartId", cartId)
                .getResultList();
    }

    @Override
    public CartItem findByCartIdAndProductId(Long cartId, Long productId) {
        try {
            return entityManager.createQuery(
                    "SELECT ci FROM CartItem ci WHERE ci.cartId = :cartId AND ci.product.id = :productId", CartItem.class)
                    .setParameter("cartId", cartId)
                    .setParameter("productId", productId)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean existsById(Long id) {
        Long count = entityManager.createQuery("select count(ci) from CartItem ci where ci.cartItemId = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        CartItem cartItem = entityManager.find(CartItem.class, id);
        if (cartItem != null) {
            entityManager.remove(cartItem);
        }
    }

    @Override
    @Transactional
    public void deleteByCartIdAndProductId(Long cartId, Long productId) {
        entityManager.createQuery(
                "DELETE FROM CartItem ci WHERE ci.cartId = :cartId AND ci.product.id = :productId")
                .setParameter("cartId", cartId)
                .setParameter("productId", productId)
                .executeUpdate();
    }
}

