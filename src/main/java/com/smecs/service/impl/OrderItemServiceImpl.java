package com.smecs.service.impl;

import com.smecs.entity.OrderItem;
import com.smecs.dto.OrderItemDTO;
import com.smecs.entity.Order;
import com.smecs.entity.Product;
import com.smecs.entity.Cart;
import com.smecs.service.OrderItemService;
import com.smecs.service.OrderService;
import com.smecs.service.CartItemService;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.OrderRepository;
import com.smecs.repository.OrderItemRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.repository.CartRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Service
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final CartRepository cartRepository;
    private final CartItemService cartItemService;

    @Override
    public List<OrderItem> createOrderItems(Long orderId, List<OrderItemDTO> orderItemDTOs) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItemDTOs.forEach(dto -> {
            OrderItem item = new OrderItem();
            Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));
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
        });
        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);
        orderService.updateOrderTotalOrThrow(orderId);

        // Delete cart items for the user after successfully creating order items
        Long userId = order.getUser() != null ? order.getUser().getId() : null;
        Cart userCart = userId != null ? cartRepository.findByUserId(userId) : null;
        if (userCart != null) {
            List<Long> productIds = savedItems.stream()
                    .map(item -> item.getProduct() != null ? item.getProduct().getId() : null)
                    .filter(productId -> productId != null)
                    .collect(Collectors.toList());
            cartItemService.deleteCartItemsByCartIdAndProductIds(userCart.getCartId(), productIds);
        }

        return savedItems;
    }

    @Override
    public OrderItem saveOrderItem(OrderItem orderItem) {
        OrderItem savedItem = orderItemRepository.save(orderItem);
        Long orderId = savedItem.getOrder() != null ? savedItem.getOrder().getId() : null;
        if (orderId != null) {
            orderService.updateOrderTotalOrThrow(orderId);
        }
        return savedItem;
    }

    @Override
    public Optional<OrderItem> getOrderItemById(Long orderItemId) {
        return orderItemRepository.findById(orderItemId);
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrder_Id(orderId);
    }

    @Override
    public OrderItem updateOrderItem(Long orderItemId, OrderItemDTO orderItemDTO) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + orderItemId));

        Long currentProductId = item.getProduct() != null ? item.getProduct().getId() : null;
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
            item.setQuantity(orderItemDTO.getQuantity());
        }

        if (orderItemDTO.getPrice() > 0) {
            item.setPriceAtPurchase(orderItemDTO.getPrice());
        }

        OrderItem savedItem = orderItemRepository.save(item);
        Long orderId = savedItem.getOrder() != null ? savedItem.getOrder().getId() : null;
        if (orderId != null) {
            orderService.updateOrderTotalOrThrow(orderId);
        }
        return savedItem;
    }

    @Override
    public void deleteOrderItem(Long orderItemId) {
        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + orderItemId));
        Long orderId = item.getOrder() != null ? item.getOrder().getId() : null;
        orderItemRepository.deleteById(orderItemId);
        if (orderId != null) {
            orderService.updateOrderTotalOrThrow(orderId);
        }
    }
}
