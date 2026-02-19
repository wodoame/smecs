package com.smecs.validation;

import com.smecs.dao.OrderItemDAO;
import com.smecs.entity.Order;
import com.smecs.entity.OrderItem;
import com.smecs.exception.ForbiddenException;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Validates ownership for OrderItem resources.
 */
@Component
public class OrderItemOwnershipValidator implements OwnershipValidator {

    private final OrderItemDAO orderItemDAO;
    private final OrderRepository orderRepository;

    @Autowired
    public OrderItemOwnershipValidator(OrderItemDAO orderItemDAO, OrderRepository orderRepository) {
        this.orderItemDAO = orderItemDAO;
        this.orderRepository = orderRepository;
    }

    @Override
    public void validateOwnership(Long resourceId, Long userId) {
        // Find the order item
        Optional<OrderItem> itemOpt = orderItemDAO.findById(resourceId);
        if (itemOpt.isEmpty()) {
            throw new ResourceNotFoundException("OrderItem not found with id: " + resourceId);
        }

        OrderItem item = itemOpt.get();

        // Find the associated order
        Long orderId = item.getOrderId();
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        Order order = orderOpt.get();
        if (order.getUserId() == null || !order.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to access this order item");
        }
    }

    @Override
    public String getResourceType() {
        return "orderItem";
    }
}
