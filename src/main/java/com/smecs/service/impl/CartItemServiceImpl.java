package com.smecs.service.impl;

import com.smecs.dto.AddToCartRequest;
import com.smecs.entity.Cart;
import com.smecs.entity.CartItem;
import com.smecs.entity.Product;
import com.smecs.entity.Inventory;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.CartItemRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.repository.InventoryRepository;
import com.smecs.service.CartItemService;
import com.smecs.service.CartService;
import com.smecs.security.OwnershipChecks;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Service
public class CartItemServiceImpl implements CartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository; // read-only checks only
    private final OwnershipChecks ownershipChecks;

    @Override
    @Transactional
    public CartItem addItemToCart(AddToCartRequest request) {
        // Validate input quantity
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = cartService.getCartById(request.getCartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + request.getCartId()));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getCartId(), product.getId());

        // Check inventory availability (read-only) before updating cart
        Inventory inventory = inventoryRepository.findByProduct_Id(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + product.getId()));
        int available = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
        int currentCartQty = cartItem == null ? 0 : cartItem.getQuantity();
        int desiredTotal = currentCartQty + request.getQuantity();
        if (available < desiredTotal) {
            throw new IllegalArgumentException("Not enough inventory for product id: " + product.getId());
        }

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(0);
            cartItem.setAddedAt(LocalDateTime.now());
        }

        cartItem.setQuantity(desiredTotal);
        return cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public CartItem updateCartItem(Long id, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        CartItem item = getCartItemById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item not found with id " + id));

        int oldQuantity = item.getQuantity();
        if (oldQuantity == quantity) {
            return item;
        }

        Product product = item.getProduct();
        if (product == null || product.getId() == null) {
            throw new ResourceNotFoundException("Product not found for cart item id: " + id);
        }

        // Check inventory availability for the new requested quantity
        Inventory inventory = inventoryRepository.findByProduct_Id(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + product.getId()));
        int available = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
        if (available < quantity) {
            throw new IllegalArgumentException("Not enough inventory for product id: " + product.getId());
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Override
    public java.util.Optional<CartItem> getCartItemById(Long cartItemId) {
        java.util.Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        itemOpt.ifPresent(ownershipChecks::assertCartItemOwnership);
        return itemOpt;
    }

    @Override
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        cartService.getCartById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        return cartItemRepository.findByCartId(cartId);
    }

    @Override
    @Transactional
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem not found with id: " + cartItemId));
        ownershipChecks.assertCartItemOwnership(cartItem);

        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void deleteCartItemsByCartIdAndProductIds(Long cartId, List<Long> productIds) {
        cartService.getCartById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        productIds.forEach(productId -> {
            CartItem item = cartItemRepository.findByCartIdAndProductId(cartId, productId);
            if (item != null) {
                cartItemRepository.deleteById(item.getCartItemId());
            }
        });
    }

    @Override
    @Transactional
    public int deleteAllItems(Long cartId) {
        cartService.getCartById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        if (items == null || items.isEmpty()) {
            return 0;
        }
        AtomicInteger deleted = new AtomicInteger(0);
        items.forEach(item -> {
            if (item.getCartItemId() != null) {
                cartItemRepository.deleteById(item.getCartItemId());
                deleted.incrementAndGet();
            }
        });
        return deleted.get();
    }

}
