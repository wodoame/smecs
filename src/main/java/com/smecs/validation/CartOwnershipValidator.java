package com.smecs.validation;

import com.smecs.dao.CartDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validates ownership for Cart resources.
 */
@Component
public class CartOwnershipValidator implements OwnershipValidator {

    private final CartDAO cartDAO;

    @Autowired
    public CartOwnershipValidator(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @Override
    public boolean validateOwnership(Long resourceId, Long userId) {
        return cartDAO.findById(resourceId)
                .map(cart -> cart.getUser() != null && cart.getUser().getId().equals(userId))
                .orElse(false);
    }

    @Override
    public String getResourceType() {
        return "cart";
    }
}

