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

	public Page<Product> findActiveProducts(String sort, Pageable pageable) {

		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();

		if ("popular".equals(sort)) {
			Pageable popularityPageable = PageRequest.of(pageNumber, pageSize);
			return productRepository.findActiveProductsOrderByPopularity(popularityPageable);
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
		return productRepository.findByIsActiveTrue(sortedPageable);
	}

	// 修正（Java）：詳細取得
	public Optional<Product> findProductById(Integer id) {
		return productRepository.findById(id);
	}
}