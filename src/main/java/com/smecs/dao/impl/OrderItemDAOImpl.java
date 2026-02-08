package com.smecs.dao.impl;

import com.smecs.dao.OrderItemDAO;
import com.smecs.entity.OrderItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderItemDAOImpl implements OrderItemDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public OrderItem save(OrderItem orderItem) {
        if (orderItem.getOrderItemId() == null) {
            entityManager.persist(orderItem);
            return orderItem;
        } else {
            return entityManager.merge(orderItem);
        }
    }

    @Override
    @Transactional
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        List<OrderItem> savedItems = new ArrayList<>();
        for (OrderItem item : orderItems) {
            savedItems.add(save(item));
        }
        return savedItems;
    }

    @Override
    public Optional<OrderItem> findById(Long id) {
        return Optional.ofNullable(entityManager.find(OrderItem.class, id));
    }

    @Override
    public List<OrderItem> findByOrderId(Long orderId) {
        return entityManager.createQuery("SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId", OrderItem.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    @Override
    public boolean existsById(Long id) {
        Long count = entityManager.createQuery("select count(oi) from OrderItem oi where oi.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        OrderItem orderItem = entityManager.find(OrderItem.class, id);
        if (orderItem != null) {
            entityManager.remove(orderItem);
        }
    }
}


