package com.natsuka.ec.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.natsuka.ec.entity.Product;

// Productエンティティ（productsテーブル）を操作するRepository
// JpaRepositoryを継承することで基本CRUD（save/findById/deleteなど）が自動で使える
public interface ProductRepository extends JpaRepository<Product, Integer> {

	// 有効な商品（is_active = true）だけ取得
	// Pageableによりページング対応（商品一覧ページなど）
	Page<Product> findByIsActiveTrue(Pageable pageable);

	// カテゴリIDで絞り込み + 有効商品
	// 商品一覧ページで「カテゴリ別表示」に使用
	Page<Product> findByIsActiveTrueAndCategoryId(Integer categoryId, Pageable pageable);

	// 商品名キーワード検索（部分一致） + 有効商品
	// IgnoreCaseなので大文字小文字を区別しない検索
	Page<Product> findByIsActiveTrueAndNameContainingIgnoreCase(String keyword, Pageable pageable);

	// カテゴリ + 商品名検索
	// 「カテゴリ内検索」の場合に使用
	Page<Product> findByIsActiveTrueAndCategoryIdAndNameContainingIgnoreCase(
			Integer categoryId,
			String keyword,
			Pageable pageable);

	// 閲覧履歴用
	// ID一覧から有効商品だけ取得
	// (セッションに保存しているproductIdListを元に取得する想定)
	List<Product> findByIdInAndIsActiveTrue(List<Integer> productIdList);

	// ==============================================
	// 人気順（全体）
	// ==============================================

	// order_items の quantity を合計して人気順に並べる
	// 売れた数量が多い商品ほど上に表示される
	// LEFT JOINなのでまだ売れていない商品も表示される
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

	// Page<Product>にすることでページング可能
	Page<Product> findActiveProductsOrderByPopularity(Pageable pageable);

	// ==============================================
	// 人気順（カテゴリ絞り込み）
	// ==============================================

	// カテゴリ指定 + 人気順
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

	// ==============================================
	// 人気順 + 検索
	// ==============================================

	// キーワード検索しつつ人気順
	// LOWER + LIKE を使って大文字小文字を区別しない検索
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

	// ==============================================
	// 人気順 + 検索 + カテゴリ
	// ==============================================

	// カテゴリ絞り込み + キーワード検索 + 人気順
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