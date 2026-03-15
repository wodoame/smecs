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
import com.smecs.security.SmecsUserPrincipal;
import com.smecs.service.CartItemService;
import com.smecs.service.CartService;
import com.smecs.service.UserService;
import com.smecs.security.OwnershipChecks;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final UserService userService;

    @Override
    @Transactional
    public CartItem addItemToCart(AddToCartRequest request) {
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        SmecsUserPrincipal principal = userService.requirePrincipal();
        Cart cart = cartService.getOrCreateCartForUser(principal.getUserId());
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        Inventory inventory = inventoryRepository.findByProduct_Id(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + product.getId()));

        return addOrUpdateCartItem(cart, product, inventory, request.getQuantity());
    }

    private CartItem addOrUpdateCartItem(Cart cart, Product product, Inventory inventory, int quantityToAdd) {
        CartItem existingItem = cartItemRepository.findByCartIdAndProductIdForUpdate(cart.getCartId(), product.getId())
                .orElse(null);

        if (existingItem != null) {
            return incrementExistingCartItem(existingItem, inventory, quantityToAdd);
        }

        validateInventory(inventory, quantityToAdd, product.getId());

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProduct(product);
        newItem.setQuantity(quantityToAdd);
        newItem.setAddedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());

        try {
            return cartItemRepository.save(newItem);
        } catch (DataIntegrityViolationException exception) {
            CartItem lockedItem = cartItemRepository.findByCartIdAndProductIdForUpdate(cart.getCartId(), product.getId())
                    .orElseThrow(() -> exception);
            return incrementExistingCartItem(lockedItem, inventory, quantityToAdd);
        }
    }

    private CartItem incrementExistingCartItem(CartItem cartItem, Inventory inventory, int quantityToAdd) {
        Long productId = cartItem.getProduct() != null ? cartItem.getProduct().getId() : null;
        int currentCartQty = cartItem.getQuantity();
        int desiredTotal = currentCartQty + quantityToAdd;
        validateInventory(inventory, desiredTotal, productId);
        cartItem.setQuantity(desiredTotal);
        if (cartItem.getCart() != null) {
            cartItem.getCart().setUpdatedAt(LocalDateTime.now());
        }
        return cartItemRepository.save(cartItem);
    }

    private void validateInventory(Inventory inventory, int desiredTotal, Long productId) {
        int available = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
        if (available < desiredTotal) {
            throw new IllegalArgumentException("Not enough inventory for product id: " + productId);
        }
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
