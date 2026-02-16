package com.smecs.validation;

import com.smecs.dao.OrderDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public boolean validateOwnership(Long resourceId, Long userId) {
        return orderDAO.findById(resourceId)
                .map(order -> order.getUserId().equals(userId))
                .orElse(false);
    }

    @Override
    public String getResourceType() {
        return "order";
    }
}


