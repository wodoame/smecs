package com.smecs.service.impl;

import com.smecs.dto.OrderDTO;
import com.smecs.dto.UpdateOrderStatusRequestDTO;
import com.smecs.entity.Order;
import com.smecs.entity.OrderItem;
import com.smecs.entity.User;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.OrderItemRepository;
import com.smecs.repository.OrderRepository;
import com.smecs.repository.UserRepository;
import com.smecs.security.OwnershipChecks;
import com.smecs.security.SmecsUserPrincipal;
import com.smecs.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OwnershipChecks ownershipChecks;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_createsForAuthenticatedUser() {
        SmecsUserPrincipal principal = new SmecsUserPrincipal(10L, "test", "test@example.com", "customer");
        when(userService.requirePrincipal()).thenReturn(principal);

        User user = new User();
        user.setId(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(7L);
            return order;
        });

        OrderDTO result = orderService.createOrder();

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(Order.Status.PENDING.toString());
    }

    @Test
    void updateOrderStatus_throwsForInvalidStatus() {
        UpdateOrderStatusRequestDTO request = new UpdateOrderStatusRequestDTO();
        request.setStatus("unknown");

        Order order = new Order();
        order.setId(4L);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.updateOrderStatus(4L, request));
    }

    @Test
    void updateOrderTotalOrThrow_calculatesTotalFromItems() {
        Order order = new Order();
        order.setId(5L);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        OrderItem item = new OrderItem();
        item.setQuantity(2);
        item.setPriceAtPurchase(10.0);
        when(orderItemRepository.findByOrder_Id(5L)).thenReturn(List.of(item));

        orderService.updateOrderTotalOrThrow(5L);

        assertThat(order.getTotalAmount()).isEqualTo(20.0);
        verify(orderRepository).save(order);
    }

    @Test
    void getOrderById_throwsWhenMissing() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(99L));
    }
}

