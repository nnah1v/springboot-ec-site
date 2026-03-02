package com.natsuka.ec.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.natsuka.ec.entity.Product;
import com.natsuka.ec.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public String index(
			@RequestParam(name = "sort", defaultValue = "new") String sort,
			@PageableDefault(size = 20) Pageable pageable,
			Model model) {

		// 修正（Java）：sortに応じて一覧を取得
		Page<Product> productPage = productService.findActiveProducts(sort, pageable);

		model.addAttribute("productPage", productPage);
		model.addAttribute("sort", sort);

		return "products/index";
	}

	// 修正（Java）：商品詳細（/products/{id}）
	@GetMapping("/{id}")
	public String show(@PathVariable Integer id, Model model) {

		Optional<Product> product = productService.findProductById(id);

		if (product.isEmpty()) {
			return "redirect:/products";
		}

		model.addAttribute("product", product.get());
		return "products/show";
	}
}