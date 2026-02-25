package com.smecs.validation;

import com.smecs.repository.CartRepository;
import com.smecs.entity.Cart;
import com.smecs.exception.ForbiddenException;
import com.smecs.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import lombok.AllArgsConstructor;

/**
 * Validates ownership for Cart resources.
 */
@AllArgsConstructor(onConstructor_ = @Autowired)
@Component
public class CartOwnershipValidator implements OwnershipValidator {

    private final CartRepository cartRepository;

    @Override
    public void validateOwnership(Long resourceId, Long userId) {
        // First, try to get cartId from request attributes (set during JWT validation)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Long cartIdFromToken = (Long) request.getAttribute("cartId");

            // If cartId is in the token and matches the requested resource, allow access
            if (cartIdFromToken != null && cartIdFromToken.equals(resourceId)) {
                return; // Ownership validated from token
            }
        }

        // Fallback: check database (for backwards compatibility or if token doesn't have cartId)
        Optional<Cart> cartOpt = cartRepository.findById(resourceId);

        if (cartOpt.isEmpty()) {
            throw new ResourceNotFoundException("Cart not found with id: " + resourceId);
        }

        Cart cart = cartOpt.get();
        if (cart.getUser() == null || !cart.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to access this cart");
        }
    }

    @Override
    public String getResourceType() {
        return "cart";
    }
}
