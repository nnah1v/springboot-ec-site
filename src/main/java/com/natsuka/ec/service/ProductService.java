package com.natsuka.ec.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.natsuka.ec.entity.Category;
import com.natsuka.ec.entity.Product;
import com.natsuka.ec.repository.CategoryRepository;
import com.natsuka.ec.repository.ProductRepository;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository; 

	public ProductService(
			ProductRepository productRepository,
			CategoryRepository categoryRepository) { 
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository; 
	}

	//管理画面用。公開/非公開を含めて全商品を取得
	public List<Product> findAllProducts() {
		return productRepository.findAll();
	}

	//管理画面用。商品を保存（新規登録/更新の両方に使う）
	public Product saveProduct(Product product) {

		//category_id から category 名を補完
		if (product.getCategoryId() != null) {
			Category category = categoryRepository.findById(product.getCategoryId())
					.orElseThrow(() -> new IllegalArgumentException("指定されたカテゴリが存在しません。"));

			product.setCategory(category.getName()); 
		}

		return productRepository.save(product);
	}

	//管理画面用。商品を削除
	public void deleteProduct(Integer id) {
		productRepository.deleteById(id);
	}

	// 商品検索（並び替え/カテゴリ対応）
	public Page<Product> searchProducts(String keyword, String sort, Pageable pageable, Integer categoryId) {

		//keywordなしは通常一覧へ委譲
		if (keyword == null || keyword.isBlank()) {
			return findActiveProducts(sort, pageable, categoryId);
		}

		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();

		//人気順は専用SQLを使う
		if ("popular".equals(sort)) {
			Pageable popularityPageable = PageRequest.of(pageNumber, pageSize);

			if (categoryId == null) {
				return productRepository.searchActiveProductsOrderByPopularity(keyword, popularityPageable);
			}

			return productRepository.searchActiveProductsOrderByPopularityAndCategoryId(
					keyword,
					categoryId,
					popularityPageable);
		}

		Sort sortCondition;

		switch (sort) {
		case "price_asc":
			sortCondition = Sort.by(Sort.Direction.ASC, "price");
			break;
		case "price_desc":
			sortCondition = Sort.by(Sort.Direction.DESC, "price");
			break;
		case "new":
		default:
			sortCondition = Sort.by(Sort.Direction.DESC, "createdAt");
			break;
		}

		Pageable sortedPageable = PageRequest.of(pageNumber, pageSize, sortCondition);

		if (categoryId == null) {
			//active商品を名前検索
			return productRepository.findByIsActiveTrueAndNameContainingIgnoreCase(keyword, sortedPageable);
		}

		//active商品をカテゴリ＋名前検索
		return productRepository.findByIsActiveTrueAndCategoryIdAndNameContainingIgnoreCase(
				categoryId,
				keyword,
				sortedPageable);
	}

	// 互換用
	public Page<Product> searchProducts(String keyword, Pageable pageable) {
		return searchProducts(keyword, "new", pageable, null);
	}

	// カテゴリ一覧は通常一覧ロジックへ委譲
	public Page<Product> findProductsByCategory(String sort, Pageable pageable, Integer categoryId) {
		return findActiveProducts(sort, pageable, categoryId);
	}

	// 通常の商品一覧（並び替え/カテゴリ対応）
	public Page<Product> findActiveProducts(String sort, Pageable pageable, Integer categoryId) {

		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();

		//人気順は専用SQLを使う
		if ("popular".equals(sort)) {
			Pageable popularityPageable = PageRequest.of(pageNumber, pageSize);

			if (categoryId == null) {
				return productRepository.findActiveProductsOrderByPopularity(popularityPageable);
			}

			return productRepository.findActiveProductsOrderByPopularityAndCategoryId(
					categoryId,
					popularityPageable);
		}

		Sort sortCondition;

		switch (sort) {
		case "price_asc":
			sortCondition = Sort.by(Sort.Direction.ASC, "price");
			break;
		case "price_desc":
			sortCondition = Sort.by(Sort.Direction.DESC, "price");
			break;
		case "new":
		default:
			sortCondition = Sort.by(Sort.Direction.DESC, "createdAt");
			break;
		}

		Pageable sortedPageable = PageRequest.of(pageNumber, pageSize, sortCondition);

		if (categoryId == null) {
			//通常一覧（active商品のみ）
			return productRepository.findByIsActiveTrue(sortedPageable);
		}

		//カテゴリ絞り込み一覧（active商品のみ）
		return productRepository.findByIsActiveTrueAndCategoryId(categoryId, sortedPageable);
	}

	// カテゴリなし版
	public Page<Product> findActiveProducts(String sort, Pageable pageable) {
		return findActiveProducts(sort, pageable, null);
	}

	// 閲覧履歴（SessionのproductId一覧→商品一覧）
	public Page<Product> findHistoryProducts(Collection<Integer> historyProductIdList, Pageable pageable) {

		if (historyProductIdList == null || historyProductIdList.isEmpty()) {
			return Page.empty(pageable);
		}

		//active商品のみ取得
		List<Product> products = productRepository.findByIdInAndIsActiveTrue(new ArrayList<>(historyProductIdList));

		//Sessionの順序を優先して並べ替え
		List<Product> orderedProducts = new ArrayList<>();
		for (Integer productId : historyProductIdList) {
			for (Product product : products) {
				if (product.getId().equals(productId)) {
					orderedProducts.add(product);
					break;
				}
			}
		}

		//手動でPage化
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), orderedProducts.size());

		if (start >= orderedProducts.size()) {
			return new PageImpl<>(List.of(), pageable, orderedProducts.size());
		}

		return new PageImpl<>(orderedProducts.subList(start, end), pageable, orderedProducts.size());
	}

	// 商品詳細取得
	public Optional<Product> findProductById(Integer id) {
		return productRepository.findById(id);
	}
}