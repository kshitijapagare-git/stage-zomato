package com.stagezomato.ecommerce.service;

import com.stagezomato.ecommerce.dto.OrderItemRequest;
import com.stagezomato.ecommerce.entity.Order;
import com.stagezomato.ecommerce.entity.OrderItem;
import com.stagezomato.ecommerce.entity.Product;
import com.stagezomato.ecommerce.exception.OrderItemConflictException;
import com.stagezomato.ecommerce.exception.ResourceNotFoundException;
import com.stagezomato.ecommerce.repository.OrderItemRepository;
import com.stagezomato.ecommerce.repository.OrderRepository;
import com.stagezomato.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderItem create(OrderItemRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.orderId()));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        if (orderItemRepository.existsByOrderIdAndProductId(request.orderId(), request.productId())) {
            throw new OrderItemConflictException(
                    "Order item already exists for orderId: " + request.orderId() + " and productId: " + request.productId());
        }

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .build();
        return orderItemRepository.save(orderItem);
    }

    @Transactional(readOnly = true)
    public OrderItem getById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getAll() {
        return orderItemRepository.findAll();
    }

    public OrderItem update(Long id, OrderItemRequest request) {
        OrderItem orderItem = getById(id);

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.orderId()));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        if (orderItemRepository.existsByOrderIdAndProductIdAndIdNot(request.orderId(), request.productId(), id)) {
            throw new OrderItemConflictException(
                    "Order item already exists for orderId: " + request.orderId() + " and productId: " + request.productId());
        }

        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(request.quantity());
        orderItem.setUnitPrice(request.unitPrice());
        return orderItemRepository.save(orderItem);
    }

    public void delete(Long id) {
        OrderItem orderItem = getById(id);
        orderItemRepository.delete(orderItem);
    }
}
