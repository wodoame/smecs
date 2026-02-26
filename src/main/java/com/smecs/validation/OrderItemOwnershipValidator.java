package com.smecs.validation;

import com.smecs.entity.Order;
import com.smecs.entity.OrderItem;
import com.smecs.repository.OrderItemRepository;
import com.smecs.exception.ForbiddenException;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Validates ownership for OrderItem resources.
 */
@AllArgsConstructor(onConstructor_ = @Autowired)
@Component
public class OrderItemOwnershipValidator implements OwnershipValidator {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    @Override
    public void validateOwnership(Long resourceId, Long userId) {
        // Find the order item
        Optional<OrderItem> itemOpt = orderItemRepository.findById(resourceId);
        if (itemOpt.isEmpty()) {
            throw new ResourceNotFoundException("OrderItem not found with id: " + resourceId);
        }

        OrderItem item = itemOpt.get();

        Long orderId = item.getOrder() != null ? item.getOrder().getId() : null;
        if (orderId == null) {
            throw new ResourceNotFoundException("Order not found for order item id: " + resourceId);
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }

        Order order = orderOpt.get();
        Long orderUserId = order.getUser() != null ? order.getUser().getId() : null;
        if (orderUserId == null || !orderUserId.equals(userId)) {
            throw new ForbiddenException("You do not have permission to access this order item");
        }
    }

    @Override
    public String getResourceType() {
        return "orderItem";
    }
}
