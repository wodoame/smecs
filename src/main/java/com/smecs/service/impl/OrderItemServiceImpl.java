package com.smecs.service.impl;

import com.smecs.entity.*;
import com.smecs.dto.OrderItemDTO;
import com.smecs.service.OrderItemService;
import com.smecs.service.OrderService;
import com.smecs.service.CartService;
import com.smecs.service.UserService;
import com.smecs.security.OwnershipChecks;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Service
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final InventoryRepository inventoryRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final OwnershipChecks ownershipChecks;

    @Override
    @Transactional
    public List<OrderItem> createOrderItems(List<OrderItemDTO> orderItemDTOs) {
        Long userId = userService.requirePrincipal().getUserId();
        Cart cart = cartRepository.findByCartId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user: " + userId));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getCartId());
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        // createOrder now returns the persisted Order entity so use it directly
        Order order = orderService.createOrder();
        List<OrderItem> orderItems = new ArrayList<>();

        // Verify and decrement inventory per item
        for (OrderItemDTO dto : orderItemDTOs) {
            Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));

            Inventory inventory = inventoryRepository.findByProduct_Id(product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + product.getId()));

            int available = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
            if (available < dto.getQuantity()) {
                throw new IllegalArgumentException("Not enough inventory for product id: " + product.getId());
            }

            inventory.setQuantity(available - dto.getQuantity());
            inventoryRepository.save(inventory);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setOrder(order);
            item.setQuantity(dto.getQuantity());
            if (dto.getPrice() > 0) {
                item.setPriceAtPurchase(dto.getPrice());
            } else {
                Double productPrice = product.getPrice();
                item.setPriceAtPurchase(productPrice != null ? productPrice : 0.0);
            }
            orderItems.add(item);
        }

        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);
        orderService.updateOrderTotalOrThrow(order.getId());

        // Delete cart items for the user after successfully creating order items
        if (cart.getCartId() != null) {
            cartService.clearCart(cart.getCartId());
        }

        return savedItems;
    }

    @Override
    public Optional<OrderItem> getOrderItemById(Long orderItemId) {
        Optional<OrderItem> itemOpt = orderItemRepository.findById(orderItemId);
        itemOpt.ifPresent(ownershipChecks::assertOrderItemOwnership);
        return itemOpt;
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        ownershipChecks.assertOrderOwnership(order);
        return orderItemRepository.findByOrder_Id(orderId);
    }

    @Override
    @Transactional
    public OrderItem updateOrderItem(Long orderItemId, OrderItemDTO orderItemDTO) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + orderItemId));

        Long currentProductId = item.getProduct() != null ? item.getProduct().getId() : null;
        int oldQuantity = item.getQuantity();
        int newQuantity = oldQuantity;

        if (orderItemDTO.getProductId() != null && !orderItemDTO.getProductId().equals(currentProductId)) {
            Product product = productRepository.findById(orderItemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + orderItemDTO.getProductId()));
            item.setProduct(product);

            if (orderItemDTO.getPrice() <= 0) {
                Double productPrice = product.getPrice();
                item.setPriceAtPurchase(productPrice != null ? productPrice : 0.0);
            }
        }

        if (orderItemDTO.getQuantity() > 0) {
            newQuantity = orderItemDTO.getQuantity();
            item.setQuantity(newQuantity);
        }

        if (orderItemDTO.getPrice() > 0) {
            item.setPriceAtPurchase(orderItemDTO.getPrice());
        }

        int diff = newQuantity - oldQuantity; // positive means increase (need to decrement inventory)
        if (diff != 0) {
            Long productId = item.getProduct() != null ? item.getProduct().getId() : null;
            if (productId == null) {
                throw new ResourceNotFoundException("Product not specified for order item");
            }
            Inventory inventory = inventoryRepository.findByProduct_Id(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId));
            int available = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
            if (diff > 0) {
                if (available < diff) {
                    throw new IllegalArgumentException("Not enough inventory for product id: " + productId);
                }
                inventory.setQuantity(available - diff);
            } else {
                inventory.setQuantity(available - diff); // subtract negative -> add
            }
            inventoryRepository.save(inventory);
        }

        OrderItem savedItem = orderItemRepository.save(item);
        Long orderId = savedItem.getOrder() != null ? savedItem.getOrder().getId() : null;
        if (orderId != null) {
            orderService.updateOrderTotalOrThrow(orderId);
        }
        return savedItem;
    }

    @Override
    @Transactional
    public void deleteOrderItem(Long orderItemId) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + orderItemId));
        Long orderId = item.getOrder() != null ? item.getOrder().getId() : null;

        // Restore inventory when removing order items
        Long productId = item.getProduct() != null ? item.getProduct().getId() : null;
        if (productId != null) {
            Inventory inventory = inventoryRepository.findByProduct_Id(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId));
            int available = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
            inventory.setQuantity(available + item.getQuantity());
            inventoryRepository.save(inventory);
        }

        orderItemRepository.deleteById(orderItemId);
        if (orderId != null) {
            orderService.updateOrderTotalOrThrow(orderId);
        }
    }
}
