package com.stagezomato.ecommerce.service;

import com.stagezomato.ecommerce.dto.CartItemRequest;
import com.stagezomato.ecommerce.entity.Cart;
import com.stagezomato.ecommerce.entity.CartItem;
import com.stagezomato.ecommerce.entity.Product;
import com.stagezomato.ecommerce.exception.CartItemConflictException;
import com.stagezomato.ecommerce.exception.ResourceNotFoundException;
import com.stagezomato.ecommerce.repository.CartItemRepository;
import com.stagezomato.ecommerce.repository.CartRepository;
import com.stagezomato.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartItem create(Long cartId, CartItemRequest request) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + cartId));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        return cartItemRepository.findByCartIdAndProductId(cartId, request.productId())
                .map(existing -> {
                    existing.setQuantity(request.quantity());
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> {
                    CartItem cartItem = CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(request.quantity())
                            .build();
                    return cartItemRepository.save(cartItem);
                });
    }

    @Transactional(readOnly = true)
    public CartItem getById(Long cartId, Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));
        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new ResourceNotFoundException("Cart item not found with id: " + itemId + " for cartId: " + cartId);
        }
        return cartItem;
    }

    @Transactional(readOnly = true)
    public List<CartItem> getAll(Long cartId) {
        if (!cartRepository.existsById(cartId)) {
            throw new ResourceNotFoundException("Cart not found with id: " + cartId);
        }
        return cartItemRepository.findAll().stream()
                .filter(cartItem -> cartItem.getCart().getId().equals(cartId))
                .toList();
    }

    public CartItem update(Long cartId, Long itemId, CartItemRequest request) {
        CartItem cartItem = getById(cartId, itemId);

        Cart cart = cartRepository.findById(request.cartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + request.cartId()));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        if (cartItemRepository.existsByCartIdAndProductIdAndIdNot(request.cartId(), request.productId(), itemId)) {
            throw new CartItemConflictException(
                    "Cart item already exists for cartId: " + request.cartId() + " and productId: " + request.productId());
        }

        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(request.quantity());
        return cartItemRepository.save(cartItem);
    }

    public void delete(Long cartId, Long itemId) {
        CartItem cartItem = getById(cartId, itemId);
        cartItemRepository.delete(cartItem);
    }
}
