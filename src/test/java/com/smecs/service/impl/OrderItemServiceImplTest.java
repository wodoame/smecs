package com.smecs.service.impl;

import com.smecs.dto.OrderDTO;
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
import com.smecs.entity.CartItem;
import com.smecs.repository.CartItemRepository;
import com.smecs.repository.UserRepository;
import com.smecs.security.OwnershipChecks;
import com.smecs.security.SmecsUserPrincipal;
import com.smecs.service.CartService;
import com.smecs.service.OrderService;
import com.smecs.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    private CartService cartService;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OwnershipChecks ownershipChecks;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    @Test
    void createOrderItems_createsItemsAndClearsCart() {
        SmecsUserPrincipal principal = new SmecsUserPrincipal(12L, "test", "test@example.com", "customer");
        when(userService.requirePrincipal()).thenReturn(principal);

        Cart cart = new Cart();
        cart.setCartId(12L);
        when(cartRepository.findByCartId(12L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(12L)).thenReturn(List.of(new CartItem()));

        Order order = new Order();
        order.setId(4L);
        User user = new User();
        user.setId(12L);
        order.setUser(user);
        when(orderService.createOrder()).thenReturn(order);

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

        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductId(9L);
        dto.setQuantity(2);
        dto.setPrice(0);

        orderItemService.createOrderItems(List.of(dto));

        verify(orderService).updateOrderTotalOrThrow(4L);
        verify(inventoryRepository).save(any(Inventory.class));
        verify(cartService).clearCart(12L);
    }

    @Test
    void createOrderItems_throwsWhenInventoryInsufficient() {
        SmecsUserPrincipal principal = new SmecsUserPrincipal(12L, "test", "test@example.com", "customer");
        when(userService.requirePrincipal()).thenReturn(principal);

        Cart cart = new Cart();
        cart.setCartId(12L);
        when(cartRepository.findByCartId(12L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(12L)).thenReturn(List.of(new CartItem()));

        Order order = new Order();
        order.setId(4L);
        when(orderService.createOrder()).thenReturn(order);

        Product product = new Product();
        product.setId(9L);
        when(productRepository.findById(9L)).thenReturn(Optional.of(product));

        Inventory inventory = new Inventory();
        inventory.setQuantity(1);
        when(inventoryRepository.findByProduct_Id(9L)).thenReturn(Optional.of(inventory));

        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductId(9L);
        dto.setQuantity(3);

        assertThrows(IllegalArgumentException.class, () -> orderItemService.createOrderItems(List.of(dto)));
    }

    @Test
    void getOrderItemsByOrderId_throwsWhenOrderMissing() {
        when(orderRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderItemService.getOrderItemsByOrderId(55L));
    }
}
