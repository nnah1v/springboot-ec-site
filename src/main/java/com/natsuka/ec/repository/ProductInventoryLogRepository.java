package com.natsuka.ec.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.natsuka.ec.entity.ProductInventoryLog;

public interface ProductInventoryLogRepository extends JpaRepository<ProductInventoryLog, Integer> {

	// 修正（Java）：在庫 = SUM(delta_qty)
	@Query(value = "SELECT COALESCE(SUM(delta_qty), 0) FROM product_inventory_logs WHERE product_id = :productId", nativeQuery = true)
	int sumDeltaQtyByProductId(@Param("productId") Integer productId);
}