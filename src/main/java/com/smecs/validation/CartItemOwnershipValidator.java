package com.smecs.validation;

import com.smecs.dao.CartDAO;
import com.smecs.dao.CartItemDAO;
import com.smecs.entity.Cart;
import com.smecs.entity.CartItem;
import com.smecs.exception.ForbiddenException;
import com.smecs.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * Validates ownership for CartItem resources.
 */
@Component
public class CartItemOwnershipValidator implements OwnershipValidator {

    private final CartItemDAO cartItemDAO;
    private final CartDAO cartDAO;

    @Autowired
    public CartItemOwnershipValidator(CartItemDAO cartItemDAO, CartDAO cartDAO) {
        this.cartItemDAO = cartItemDAO;
        this.cartDAO = cartDAO;
    }

    @Override
    public void validateOwnership(Long resourceId, Long userId) {
        // Try to leverage cartId from JWT validation if available on the request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Long cartIdFromToken = null;
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object attr = request.getAttribute("cartId");
            if (attr instanceof Long) {
                cartIdFromToken = (Long) attr;
            } else if (attr instanceof Integer) {
                cartIdFromToken = ((Integer) attr).longValue();
            } else if (attr instanceof String) {
                try {
                    cartIdFromToken = Long.parseLong((String) attr);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Find the cart item
        Optional<CartItem> itemOpt = cartItemDAO.findById(resourceId);
        if (itemOpt.isEmpty()) {
            throw new ResourceNotFoundException("CartItem not found with id: " + resourceId);
        }

        CartItem item = itemOpt.get();

        // If token contained a cartId and it matches the cartItem's cartId, allow
        if (cartIdFromToken != null && cartIdFromToken.equals(item.getCartId())) {
            return;
        }

        // Otherwise verify via cart existence and ownership
        Long cartId = item.getCartId();
        Optional<Cart> cartOpt = cartDAO.findById(cartId);
        if (cartOpt.isEmpty()) {
            throw new ResourceNotFoundException("Cart not found with id: " + cartId);
        }

        Cart cart = cartOpt.get();
        if (cart.getUser() == null || !cart.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to access this cart item");
        }
    }

    @Override
    public String getResourceType() {
        return "cartItem";
    }
}

