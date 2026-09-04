package com.stagezomato.ecommerce.repository;

import com.stagezomato.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    boolean existsByCartIdAndProductIdAndIdNot(Long cartId, Long productId, Long id);
}
