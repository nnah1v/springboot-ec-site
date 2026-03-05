package com.natsuka.ec.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.natsuka.ec.entity.Product;
import com.natsuka.ec.repository.ProductRepository;

@Service
public class ProductService {

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	// 修正（Java）：カテゴリ指定あり版（categoryId）
	public Page<Product> findActiveProducts(String sort, Pageable pageable, Integer categoryId) {

		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();

		if ("popular".equals(sort)) {
			Pageable popularityPageable = PageRequest.of(pageNumber, pageSize);

			if (categoryId == null) { // 修正（Java）
				return productRepository.findActiveProductsOrderByPopularity(popularityPageable);
			}

			return productRepository.findActiveProductsOrderByPopularityAndCategoryId(categoryId, popularityPageable); // 修正（Java）
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

		if (categoryId == null) { // 修正（Java）
			return productRepository.findByIsActiveTrue(sortedPageable);
		}

		return productRepository.findByIsActiveTrueAndCategoryId(categoryId, sortedPageable); // 修正（Java）
	}

	// 既存：カテゴリなし版（互換のため残す）
	public Page<Product> findActiveProducts(String sort, Pageable pageable) {
		return findActiveProducts(sort, pageable, null); // 修正（Java）
	}

	// ：詳細取得
	public Optional<Product> findProductById(Integer id) {
		return productRepository.findById(id);
	}
}