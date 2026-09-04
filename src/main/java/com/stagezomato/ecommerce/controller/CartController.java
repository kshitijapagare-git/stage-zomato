package com.stagezomato.ecommerce.controller;

import com.stagezomato.ecommerce.dto.CartRequest;
import com.stagezomato.ecommerce.dto.CartResponse;
import com.stagezomato.ecommerce.entity.Cart;
import com.stagezomato.ecommerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> create(@Valid @RequestBody CartRequest request) {
        Cart created = cartService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CartResponse.from(created));
    }

    @GetMapping("/{id}")
    public CartResponse getById(@PathVariable Long id) {
        return CartResponse.from(cartService.getById(id));
    }

    @GetMapping
    public List<CartResponse> getAll() {
        return cartService.getAll().stream().map(CartResponse::from).toList();
    }

    @PutMapping("/{id}")
    public CartResponse update(@PathVariable Long id, @Valid @RequestBody CartRequest request) {
        return CartResponse.from(cartService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cartService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
