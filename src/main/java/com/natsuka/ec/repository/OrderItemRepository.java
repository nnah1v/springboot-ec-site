package com.natsuka.ec.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.natsuka.ec.entity.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

	// 修正（Java）：注文IDから明細取得
	List<OrderItem> findByOrderId(Integer orderId);

}