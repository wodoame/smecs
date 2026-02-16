package com.smecs.validation;

import com.smecs.dao.OrderDAO;
import com.smecs.entity.Order;
import com.smecs.exception.ForbiddenException;
import com.smecs.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Validates ownership for Order resources.
 */
@Component
public class OrderOwnershipValidator implements OwnershipValidator {

    private final OrderDAO orderDAO;

    @Autowired
    public OrderOwnershipValidator(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    @Override
    public void validateOwnership(Long resourceId, Long userId) {
        Optional<Order> orderOpt = orderDAO.findById(resourceId);

        if (orderOpt.isEmpty()) {
            throw new ResourceNotFoundException("Order not found with id: " + resourceId);
        }

        Order order = orderOpt.get();
        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to access this order");
        }
    }

    @Override
    public String getResourceType() {
        return "order";
    }
}


