package com.smecs.service.impl;

import com.smecs.dto.AddToCartRequest;
import com.smecs.dto.UpdateInventoryRequestDTO;
import com.smecs.entity.Cart;
import com.smecs.entity.CartItem;
import com.smecs.entity.Inventory;
import com.smecs.entity.Product;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.CartItemRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.repository.InventoryRepository;
import com.smecs.service.CartItemService;
import com.smecs.service.CartService;
import com.smecs.service.InventoryService;
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
    private final InventoryRepository inventoryRepository;
    private final InventoryService inventoryService;

    private void updateInventoryQuantity(Inventory inventory, int delta) {
        int currentQty = inventory.getQuantity() == null ? 0 : inventory.getQuantity();
        int newQty = currentQty + delta;
        if (newQty < 0) {
            throw new IllegalArgumentException("Resulting inventory cannot be negative");
        }
        UpdateInventoryRequestDTO updatedInventoryDTO = new UpdateInventoryRequestDTO();
        updatedInventoryDTO.setQuantity(newQty);
        inventoryService.updateInventory(inventory.getId(), updatedInventoryDTO);
        inventory.setQuantity(newQty); // keep in-memory state in sync
    }

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
        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + product.getId()));

        if(inventory.getQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Not enough inventory for product id: " + product.getId());
        }
        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getCartId(), product.getId());
        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCartId(cart.getCartId());
            cartItem.setProduct(product);
            cartItem.setQuantity(0);
            cartItem.setAddedAt(LocalDateTime.now());
        }


        // Decrement inventory first so both operations are in the same transaction
        updateInventoryQuantity(inventory, -request.getQuantity());
        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
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

        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + product.getId()));

        int diff = quantity - oldQuantity; // positive if cart quantity increases
        if (diff > 0 && inventory.getQuantity() < diff) {
            throw new IllegalArgumentException("Not enough inventory for product id: " + product.getId());
        }

        updateInventoryQuantity(inventory, -diff); // decrement when diff>0, increment when diff<0
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Override
    public java.util.Optional<CartItem> getCartItemById(Long cartItemId) {
        return cartItemRepository.findById(cartItemId);
    }

    @Override
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    @Override
    @Transactional
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem not found with id: " + cartItemId));

        Product product = cartItem.getProduct();
        if (product == null || product.getId() == null) {
            throw new ResourceNotFoundException("Product not found for cart item id: " + cartItemId);
        }

        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + product.getId()));

        updateInventoryQuantity(inventory, cartItem.getQuantity());
        cartItemRepository.delete(cartItem);
    }

    @Override
    public void deleteCartItemsByCartIdAndProductIds(Long cartId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        productIds.forEach(productId -> {
            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + productId));
            CartItem item = cartItemRepository.findByCartIdAndProductId(cartId, productId);
            if (item != null) {
                updateInventoryQuantity(inventory, item.getQuantity());
                cartItemRepository.deleteById(item.getCartItemId());
            }
        });
    }
    @Override
    @Transactional
    public int deleteAllItems(Long cartId) {
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        if (items == null || items.isEmpty()) {
            return 0;
        }
        AtomicInteger deleted = new AtomicInteger(0);
        items.forEach(item -> {
            Product product = item.getProduct();
            if (product == null || product.getId() == null) {
                throw new ResourceNotFoundException("Product not found for cart item id: " + item.getCartItemId());
            }
            Inventory inventory = inventoryRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product id: " + product.getId()));
            updateInventoryQuantity(inventory, item.getQuantity());
            if (item.getCartItemId() != null) {
                cartItemRepository.deleteById(item.getCartItemId());
                deleted.incrementAndGet();
            }
        });
        return deleted.get();
    }
}
