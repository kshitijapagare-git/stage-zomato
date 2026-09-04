package com.stagezomato.ecommerce.service;

import com.stagezomato.ecommerce.dto.OrderRequest;
import com.stagezomato.ecommerce.entity.Order;
import com.stagezomato.ecommerce.entity.OrderStatus;
import com.stagezomato.ecommerce.entity.Product;
import com.stagezomato.ecommerce.exception.ResourceNotFoundException;
import com.stagezomato.ecommerce.repository.OrderRepository;
import com.stagezomato.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public Order create(OrderRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        Order order = Order.builder()
                .customerName(request.customerName())
                .product(product)
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .status(request.status() != null ? request.status() : OrderStatus.PENDING)
                .build();
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    public Order update(Long id, OrderRequest request) {
        Order order = getById(id);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.productId()));

        order.setCustomerName(request.customerName());
        order.setProduct(product);
        order.setQuantity(request.quantity());
        order.setUnitPrice(request.unitPrice());
        if (request.status() != null) {
            order.setStatus(request.status());
        }
        return orderRepository.save(order);
    }

    public void delete(Long id) {
        Order order = getById(id);
        orderRepository.delete(order);
    }
}
