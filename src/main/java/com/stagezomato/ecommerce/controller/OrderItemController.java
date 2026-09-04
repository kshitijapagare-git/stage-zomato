package com.stagezomato.ecommerce.controller;

import com.stagezomato.ecommerce.dto.OrderItemRequest;
import com.stagezomato.ecommerce.dto.OrderItemResponse;
import com.stagezomato.ecommerce.entity.OrderItem;
import com.stagezomato.ecommerce.service.OrderItemService;
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
@RequestMapping("/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<OrderItemResponse> create(@Valid @RequestBody OrderItemRequest request) {
        OrderItem created = orderItemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderItemResponse.from(created));
    }

    @GetMapping("/{id}")
    public OrderItemResponse getById(@PathVariable Long id) {
        return OrderItemResponse.from(orderItemService.getById(id));
    }

    @GetMapping
    public List<OrderItemResponse> getAll() {
        return orderItemService.getAll().stream().map(OrderItemResponse::from).toList();
    }

    @PutMapping("/{id}")
    public OrderItemResponse update(@PathVariable Long id, @Valid @RequestBody OrderItemRequest request) {
        return OrderItemResponse.from(orderItemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
