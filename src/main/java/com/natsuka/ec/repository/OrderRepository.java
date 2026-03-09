package com.natsuka.ec.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.natsuka.ec.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

	//ユーザーの注文履歴取得（新しい順）
	List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
}