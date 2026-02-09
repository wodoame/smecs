package com.smecs.service.impl;

import com.smecs.entity.OrderItem;
import com.smecs.dao.OrderItemDAO;
import com.smecs.dao.OrderDAO;
import com.smecs.dao.ProductDAO;
import com.smecs.dto.OrderItemDTO;
import com.smecs.entity.Order;
import com.smecs.entity.Product;
import com.smecs.service.OrderItemService;
import com.smecs.service.OrderService;
import com.smecs.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemDAO orderItemDAO;
    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;
    private final OrderService orderService;

    @Autowired
    public OrderItemServiceImpl(OrderItemDAO orderItemDAO, ProductDAO productDAO, OrderDAO orderDAO, OrderService orderService) {
        this.orderItemDAO = orderItemDAO;
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
        this.orderService = orderService;
    }

    @Override
    public List<OrderItem> createOrderItems(Long orderId, List<OrderItemDTO> orderItemDTOs) {
        Order order = orderDAO.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        List<OrderItem> orderItems = new ArrayList<>();
        orderItemDTOs.forEach(dto -> {
            OrderItem item = new OrderItem();
            Product product = productDAO.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));
            item.setProductId(product.getId());
            item.setOrderId(order.getId());

            item.setQuantity(dto.getQuantity());

            if (dto.getPrice() > 0) {
                item.setPriceAtPurchase(dto.getPrice());
            } else {
                Double productPrice = product.getPrice();
                item.setPriceAtPurchase(productPrice != null ? productPrice : 0.0);
            }
            orderItems.add(item);
        });
        List<OrderItem> savedItems = orderItemDAO.saveAll(orderItems);
        orderService.updateOrderTotalOrThrow(orderId);
        return savedItems;
    }

    @Override
    public OrderItem saveOrderItem(OrderItem orderItem) {
        OrderItem savedItem = orderItemDAO.save(orderItem);
        orderService.updateOrderTotalOrThrow(savedItem.getOrderId());
        return savedItem;
    }

    @Override
    public Optional<OrderItem> getOrderItemById(Long orderItemId) {
        return orderItemDAO.findById(orderItemId);
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemDAO.findByOrderId(orderId);
    }

    @Override
    public OrderItem updateOrderItem(Long orderItemId, OrderItemDTO orderItemDTO) {
        OrderItem item = orderItemDAO.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + orderItemId));

        if (orderItemDTO.getProductId() != null && !orderItemDTO.getProductId().equals(item.getProductId())) {
            Product product = productDAO.findById(orderItemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + orderItemDTO.getProductId()));
            item.setProductId(product.getId());

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

        OrderItem savedItem = orderItemDAO.save(item);
        orderService.updateOrderTotalOrThrow(savedItem.getOrderId());
        return savedItem;
    }

    @Override
    public void deleteOrderItem(Long orderItemId) {
        OrderItem item = orderItemDAO.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found with id: " + orderItemId));
        Long orderId = item.getOrderId();
        orderItemDAO.deleteById(orderItemId);
        orderService.updateOrderTotalOrThrow(orderId);
    }
}
