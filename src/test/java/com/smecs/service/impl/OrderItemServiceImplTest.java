package com.smecs.service.impl;

import com.smecs.dto.OrderItemDTO;
import com.smecs.entity.Cart;
import com.smecs.entity.Inventory;
import com.smecs.entity.Order;
import com.smecs.entity.OrderItem;
import com.smecs.entity.Product;
import com.smecs.entity.User;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.CartRepository;
import com.smecs.repository.InventoryRepository;
import com.smecs.repository.OrderItemRepository;
import com.smecs.repository.OrderRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.security.OwnershipChecks;
import com.smecs.service.CartItemService;
import com.smecs.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemService cartItemService;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OwnershipChecks ownershipChecks;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @Test
    void createOrderItems_createsItemsAndClearsCart() {
        Order order = new Order();
        order.setId(4L);
        User user = new User();
        user.setId(12L);
        order.setUser(user);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));

        Product product = new Product();
        product.setId(9L);
        when(productRepository.findById(9L)).thenReturn(Optional.of(product));

        Inventory inventory = new Inventory();
        inventory.setQuantity(10);
        when(inventoryRepository.findByProduct_Id(9L)).thenReturn(Optional.of(inventory));

        OrderItem saved = new OrderItem();
        saved.setProduct(product);
        saved.setQuantity(2);
        when(orderItemRepository.saveAll(any())).thenReturn(List.of(saved));

        Cart cart = new Cart();
        cart.setCartId(12L);
        when(cartRepository.findById(12L)).thenReturn(Optional.of(cart));

        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductId(9L);
        dto.setQuantity(2);
        dto.setPrice(0);

        orderItemService.createOrderItems(4L, List.of(dto));

        verify(orderService).updateOrderTotalOrThrow(4L);
        verify(inventoryRepository).save(any(Inventory.class));

        ArgumentCaptor<List<Long>> productsCaptor = ArgumentCaptor.forClass(List.class);
        verify(cartItemService).deleteCartItemsByCartIdAndProductIds(eq(12L), productsCaptor.capture());
        assertThat(productsCaptor.getValue()).containsExactly(9L);
    }

    @Test
    void createOrderItems_throwsWhenInventoryInsufficient() {
        Order order = new Order();
        order.setId(4L);
        when(orderRepository.findById(4L)).thenReturn(Optional.of(order));

        Product product = new Product();
        product.setId(9L);
        when(productRepository.findById(9L)).thenReturn(Optional.of(product));

        Inventory inventory = new Inventory();
        inventory.setQuantity(1);
        when(inventoryRepository.findByProduct_Id(9L)).thenReturn(Optional.of(inventory));

        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductId(9L);
        dto.setQuantity(3);

        assertThrows(IllegalArgumentException.class, () -> orderItemService.createOrderItems(4L, List.of(dto)));
    }

    @Test
    void getOrderItemsByOrderId_throwsWhenOrderMissing() {
        when(orderRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderItemService.getOrderItemsByOrderId(55L));
    }
}
