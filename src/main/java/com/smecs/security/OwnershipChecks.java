package com.smecs.security;

import com.smecs.entity.Cart;
import com.smecs.entity.CartItem;
import com.smecs.entity.Order;
import com.smecs.entity.OrderItem;
import com.smecs.entity.Review;
import com.smecs.exception.ForbiddenException;
import com.smecs.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Component
public class OwnershipChecks {
    private final UserService userService;

    public void assertUserMatches(Long userId) {
        SmecsUserPrincipal principal = userService.requirePrincipal();
        if (userService.isAdmin(principal)) {
            return;
        }
        if (userId == null || !userId.equals(principal.getUserId())) {
            throw new ForbiddenException("You do not have permission to access this user's data");
        }
    }

    public void assertOrderOwnership(Order order) {
        SmecsUserPrincipal principal = userService.requirePrincipal();
        if (userService.isAdmin(principal)) {
            return;
        }
        Long orderUserId = order.getUser() != null ? order.getUser().getId() : null;
        if (orderUserId == null || !orderUserId.equals(principal.getUserId())) {
            throw new ForbiddenException("You do not have permission to access this order");
        }
    }

    public void assertOrderItemOwnership(OrderItem item) {
        SmecsUserPrincipal principal = userService.requirePrincipal();
        if (userService.isAdmin(principal)) {
            return;
        }
        Order order = item.getOrder();
        Long orderUserId = order != null && order.getUser() != null ? order.getUser().getId() : null;
        if (orderUserId == null || !orderUserId.equals(principal.getUserId())) {
            throw new ForbiddenException("You do not have permission to access this order item");
        }
    }

    public void assertReviewOwnership(Review review) {
        SmecsUserPrincipal principal = userService.requirePrincipal();
        if (userService.isAdmin(principal)) {
            return;
        }
        Long reviewUserId = review.getUser() != null ? review.getUser().getId() : null;
        if (reviewUserId == null || !reviewUserId.equals(principal.getUserId())) {
            throw new ForbiddenException("You do not have permission to access this review");
        }
    }

    public void assertCartOwnership(Cart cart) {
        SmecsUserPrincipal principal = userService.requirePrincipal();
        if (userService.isAdmin(principal)) {
            return;
        }
        Long cartUserId = cart.getUser() != null ? cart.getUser().getId() : null;
        if (cartUserId == null || !cartUserId.equals(principal.getUserId())) {
            throw new ForbiddenException("You do not have permission to access this cart");
        }
    }

    public void assertCartItemOwnership(CartItem item) {
        SmecsUserPrincipal principal = userService.requirePrincipal();
        if (userService.isAdmin(principal)) {
            return;
        }
        Cart cart = item.getCart();
        Long cartUserId = cart != null && cart.getUser() != null ? cart.getUser().getId() : null;
        if (cartUserId == null || !cartUserId.equals(principal.getUserId())) {
            throw new ForbiddenException("You do not have permission to access this cart item");
        }
    }
}
