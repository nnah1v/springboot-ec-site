package com.natsuka.ec.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.natsuka.ec.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	Page<Product> findByIsActiveTrue(Pageable pageable);

	//：カテゴリ絞り込み（価格/新着の並び替えはServiceでPageableを渡す）
	Page<Product> findByIsActiveTrueAndCategoryId(Integer category, Pageable pageable); 

	//：人気順（全体）
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

	//：人気順（カテゴリ絞り込み）
	@Query(value = """
			SELECT p.*
			FROM products p
			LEFT JOIN order_items oi ON oi.product_id = p.id
			WHERE p.is_active = 1
			  AND p.category = :category
			GROUP BY p.id
			ORDER BY COALESCE(SUM(oi.quantity), 0) DESC, p.id DESC
			""", countQuery = """
			SELECT COUNT(*)
			FROM products p
			WHERE p.is_active = 1
			  AND p.category = :category
			""", nativeQuery = true)
	Page<Product> findActiveProductsOrderByPopularityAndCategoryId(Integer category, Pageable pageable); 
}