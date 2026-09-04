package com.stagezomato.ecommerce.repository;

import com.stagezomato.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    boolean existsByOrderIdAndProductId(Long orderId, Long productId);

    boolean existsByOrderIdAndProductIdAndIdNot(Long orderId, Long productId, Long id);
}
