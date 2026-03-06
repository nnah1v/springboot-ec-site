package com.natsuka.ec.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // 修正（Java）

import com.natsuka.ec.entity.Product;
import com.natsuka.ec.service.ProductService;

@Controller
@RequestMapping("/categories")
public class CategoryController {

	private final ProductService productService;

	public CategoryController(ProductService productService) {
		this.productService = productService;
	}

	// 修正（Java）：カテゴリ別商品一覧（sort対応）
	@GetMapping("/{categoryId}")
	public String categoryProducts(
			@PathVariable Integer categoryId,
			@RequestParam(defaultValue = "new") String sort, // 修正（Java）
			Pageable pageable,
			Model model) {

		// 修正（Java）：一覧と同じロジック（active + sort + category）
		Page<Product> productPage = productService.findActiveProducts(sort, pageable, categoryId);

		model.addAttribute("productPage", productPage);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("sort", sort); // 修正（Java）：画面でリンク維持に使う

		return "categories/index";
	}
}