package com.natsuka.ec.repository;

import java.util.List; // 修正（Java）

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.natsuka.ec.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

	Page<Product> findByIsActiveTrue(Pageable pageable);

	// 修正（Java）：categoryIdで絞り込み
	Page<Product> findByIsActiveTrueAndCategoryId(Integer categoryId, Pageable pageable);

	// 修正（Java）：active商品の名前検索
	Page<Product> findByIsActiveTrueAndNameContainingIgnoreCase(String keyword, Pageable pageable);

	// 修正（Java）：active商品をカテゴリ＋名前で検索
	Page<Product> findByIsActiveTrueAndCategoryIdAndNameContainingIgnoreCase(
			Integer categoryId,
			String keyword,
			Pageable pageable);

	// 修正（Java）：閲覧履歴用（ID一覧からactive商品を取得）
	List<Product> findByIdInAndIsActiveTrue(List<Integer> productIdList);

	
	// 人気順（全体）
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

	// 人気順（カテゴリ絞り込み）
	@Query(value = """
			SELECT p.*
			FROM products p
			LEFT JOIN order_items oi ON oi.product_id = p.id
			WHERE p.is_active = 1
			  AND p.category_id = :categoryId
			GROUP BY p.id
			ORDER BY COALESCE(SUM(oi.quantity), 0) DESC, p.id DESC
			""", countQuery = """
			SELECT COUNT(*)
			FROM products p
			WHERE p.is_active = 1
			  AND p.category_id = :categoryId
			""", nativeQuery = true)
	Page<Product> findActiveProductsOrderByPopularityAndCategoryId(
			@Param("categoryId") Integer categoryId,
			Pageable pageable);

	// 修正（Java）：人気順＋検索（カテゴリなし）
	@Query(value = """
			SELECT p.*
			FROM products p
			LEFT JOIN order_items oi ON oi.product_id = p.id
			WHERE p.is_active = 1
			  AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			GROUP BY p.id
			ORDER BY COALESCE(SUM(oi.quantity), 0) DESC, p.id DESC
			""", countQuery = """
			SELECT COUNT(*)
			FROM products p
			WHERE p.is_active = 1
			  AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			""", nativeQuery = true)
	Page<Product> searchActiveProductsOrderByPopularity(
			@Param("keyword") String keyword,
			Pageable pageable);

	// 修正（Java）：人気順＋検索＋カテゴリ
	@Query(value = """
			SELECT p.*
			FROM products p
			LEFT JOIN order_items oi ON oi.product_id = p.id
			WHERE p.is_active = 1
			  AND p.category_id = :categoryId
			  AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			GROUP BY p.id
			ORDER BY COALESCE(SUM(oi.quantity), 0) DESC, p.id DESC
			""", countQuery = """
			SELECT COUNT(*)
			FROM products p
			WHERE p.is_active = 1
			  AND p.category_id = :categoryId
			  AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
			""", nativeQuery = true)
	Page<Product> searchActiveProductsOrderByPopularityAndCategoryId(
			@Param("keyword") String keyword,
			@Param("categoryId") Integer categoryId,
			Pageable pageable);
	
	
}