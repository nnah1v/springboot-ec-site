package com.natsuka.ec.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.natsuka.ec.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

	List<CartItem> findByUserIdOrderByUpdatedAtDesc(Integer userId);

	Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId);
}