package com.natsuka.ec.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.natsuka.ec.entity.Category;
import com.natsuka.ec.entity.Product;
import com.natsuka.ec.service.CategoryService;
import com.natsuka.ec.service.InventoryService;
import com.natsuka.ec.service.ProductService;

// 管理画面の商品管理を担当するController
@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

	private final ProductService productService;
	private final CategoryService categoryService;
	private final InventoryService inventoryService;

	// コンストラクタインジェクション
	public AdminProductController(
			ProductService productService,
			CategoryService categoryService,
			InventoryService inventoryService) {
		this.productService = productService;
		this.categoryService = categoryService;
		this.inventoryService = inventoryService;
	}

	// 商品一覧画面
	@GetMapping
	public String index(Model model) {

		// 全商品一覧を取得
		List<Product> productList = productService.findAllProducts();

		// カテゴリ一覧を取得
		List<Category> categoryList = categoryService.findAllCategories();

		// categoryId → categoryName の変換用Mapを作成
		Map<Integer, String> categoryNameMap = new LinkedHashMap<>();
		for (Category category : categoryList) {
			categoryNameMap.put(category.getId(), category.getName());
		}

		// 修正（Java）：productId → 在庫数 の変換用Mapを作成
		Map<Integer, Integer> stockMap = new LinkedHashMap<>();
		for (Product product : productList) {
			Integer availableStock = inventoryService.getAvailableStock(product.getId());
			stockMap.put(product.getId(), availableStock);
		}

		// 全商品一覧を画面へ渡す
		model.addAttribute("productList", productList);

		// カテゴリ名表示用Mapを画面へ渡す
		model.addAttribute("categoryNameMap", categoryNameMap);

		// 修正（Java）：在庫表示用Mapを画面へ渡す
		model.addAttribute("stockMap", stockMap);

		return "admin/products/index";
	}

	// 商品新規登録画面
	@GetMapping("/create")
	public String create(Model model) {

		// 新規登録用の空Productを画面へ渡す
		model.addAttribute("product", new Product());

		// カテゴリ一覧を画面へ渡す
		model.addAttribute("categories", categoryService.findAllCategories());

		return "admin/products/create";
	}

	// 商品新規登録処理
	@PostMapping("/store")
	public String store(
			Product product,
			@RequestParam(name = "initialStock", required = false) Integer initialStock) {

		// 入力された商品を保存
		productService.saveProduct(product);

		// 修正（Java）：保存後の商品IDを使って初期在庫ログを登録
		inventoryService.addInitialStock(product.getId(), initialStock);

		return "redirect:/admin/products";
	}

	// 商品編集画面
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable Integer id, Model model) {

		// 指定idの商品を取得
		Optional<Product> optionalProduct = productService.findProductById(id);

		// 商品が存在しない場合は一覧へ戻す
		if (optionalProduct.isEmpty()) {
			return "redirect:/admin/products";
		}

		// 取得した商品を画面へ渡す
		model.addAttribute("product", optionalProduct.get());

		// カテゴリ一覧を画面へ渡す
		model.addAttribute("categories", categoryService.findAllCategories());

		// 修正（Java）：現在在庫を画面へ渡す
		model.addAttribute("currentStock", inventoryService.getAvailableStock(id));

		return "admin/products/edit";
	}

	// 商品更新処理
	@PostMapping("/{id}/update")
	public String update(
			@PathVariable Integer id,
			Product product,
			@RequestParam(name = "addStock", required = false) Integer addStock) {

		// URLのidをProductへセットして更新対象を明確にする
		product.setId(id);

		// saveで更新処理を行う
		productService.saveProduct(product);

		// 修正（Java）：追加入庫
		inventoryService.addStock(id, addStock);

		return "redirect:/admin/products";
	}

	// 商品削除処理
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Integer id) {

		// 指定idの商品を削除
		productService.deleteProduct(id);

		return "redirect:/admin/products";
	}
}