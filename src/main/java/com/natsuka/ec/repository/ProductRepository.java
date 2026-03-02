package com.natsuka.ec.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.natsuka.ec.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	Page<Product> findByIsActiveTrue(Pageable pageable);

	// 修正（Java）：人気順（注文数量の合計が多い順）をSQLで集計する。OrderItem Entity不要。
	@Query(value = """
			SELECT p.*
			FROM products p
			LEFT JOIN order_items oi ON oi.product_id = p.id
			WHERE p.is_active = 1
			GROUP BY p.id
			ORDER BY COALESCE(SUM(oi.quantity), 0) DESC, p.id DESC
			""", countQuery = """
			SELECT COUNT(*)
			FROM products p
			WHERE p.is_active = 1
			""", nativeQuery = true)
	Page<Product> findActiveProductsOrderByPopularity(Pageable pageable);
}