package com.natsuka.ec.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.natsuka.ec.entity.Product;
import com.natsuka.ec.service.ProductService;

@Controller
public class ProductController {

	// 商品取得の業務ロジック（Service）
	private final ProductService productService;

	// コンストラクタインジェクション
	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	// 一覧（メイン画面）：GET / と GET /products を同じ画面にする
	@GetMapping({ "/", "/products" })
	public String index(
			// sortクエリ（未指定はnew）
			@RequestParam(name = "sort", defaultValue = "new") String sort,
			// ページング（未指定はsize=20）
			@PageableDefault(size = 20) Pageable pageable,
			// 画面へ渡す入れ物
			Model model) {


		// sortに応じて有効商品をページング取得
		Page<Product> productPage = productService.findActiveProducts(sort, pageable);

		// 画面へ渡す
		model.addAttribute("productPage", productPage);
		model.addAttribute("sort", sort);

		// templates/products/index.html
		return "products/index";
	}

	// 商品詳細：GET /products/{id}
	@GetMapping("/products/{id}")
	public String show(
			// パスのid
			@PathVariable Integer id,
			// 画面へ渡す入れ物
			Model model) {

		// idで商品を1件取得
		Optional<Product> product = productService.findProductById(id);

		// 存在しないならメインへ戻す
		if (product.isEmpty()) {
			return "redirect:/";
		}

		// 画面へ渡す
		model.addAttribute("product", product.get());

		// templates/products/show.html
		return "products/show";
	}
}