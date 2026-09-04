package com.stagezomato.ecommerce.controller;

import com.stagezomato.ecommerce.dto.CartItemRequest;
import com.stagezomato.ecommerce.dto.CartItemResponse;
import com.stagezomato.ecommerce.entity.CartItem;
import com.stagezomato.ecommerce.service.CartItemService;
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
@RequestMapping("/carts/{cartId}/items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping
    public ResponseEntity<CartItemResponse> create(@PathVariable Long cartId, @Valid @RequestBody CartItemRequest request) {
        CartItem created = cartItemService.create(cartId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CartItemResponse.from(created));
    }

    @GetMapping("/{itemId}")
    public CartItemResponse getById(@PathVariable Long cartId, @PathVariable Long itemId) {
        return CartItemResponse.from(cartItemService.getById(cartId, itemId));
    }

    @GetMapping
    public List<CartItemResponse> getAll(@PathVariable Long cartId) {
        return cartItemService.getAll(cartId).stream().map(CartItemResponse::from).toList();
    }

    @PutMapping("/{itemId}")
    public CartItemResponse update(@PathVariable Long cartId, @PathVariable Long itemId, @Valid @RequestBody CartItemRequest request) {
        return CartItemResponse.from(cartItemService.update(cartId, itemId, request));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@PathVariable Long cartId, @PathVariable Long itemId) {
        cartItemService.delete(cartId, itemId);
        return ResponseEntity.noContent().build();
    }
}
