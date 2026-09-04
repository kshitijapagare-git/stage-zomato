package com.stagezomato.ecommerce.service;

import com.stagezomato.ecommerce.dto.CartRequest;
import com.stagezomato.ecommerce.entity.Cart;
import com.stagezomato.ecommerce.exception.ResourceNotFoundException;
import com.stagezomato.ecommerce.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;

    public Cart create(CartRequest request) {
        Cart cart = Cart.builder()
                .customerName(request.customerName().trim())
                .status(request.status())
                .build();
        return cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public Cart getById(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Cart> getAll() {
        return cartRepository.findAll();
    }

    public Cart update(Long id, CartRequest request) {
        Cart cart = getById(id);
        cart.setCustomerName(request.customerName().trim());
        cart.setStatus(request.status());
        return cartRepository.save(cart);
    }

    public void delete(Long id) {
        Cart cart = getById(id);
        cartRepository.delete(cart);
    }
}
