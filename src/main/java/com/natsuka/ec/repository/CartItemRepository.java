package com.natsuka.ec.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.natsuka.ec.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

	@EntityGraph(attributePaths = { "product" }) //：Lazy例外回避
	List<CartItem> findByUserIdOrderByUpdatedAtDesc(Integer userId);

	Optional<CartItem> findByUserIdAndProduct_Id(Integer userId, Integer productId); 
}