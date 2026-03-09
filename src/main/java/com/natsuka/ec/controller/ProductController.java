package com.natsuka.ec.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.natsuka.ec.dto.SessionFavorites;
import com.natsuka.ec.dto.SessionHistory;
import com.natsuka.ec.entity.Category;
import com.natsuka.ec.entity.Product;
import com.natsuka.ec.repository.UserRepository;
import com.natsuka.ec.service.CategoryService;
import com.natsuka.ec.service.FavoriteService;
import com.natsuka.ec.service.InventoryService;
import com.natsuka.ec.service.ProductService;

@Controller
public class ProductController {

	private static final String SESSION_FAVORITES_KEY = "SESSION_FAVORITES";
	private static final String SESSION_HISTORY_KEY = "SESSION_HISTORY";

	private final ProductService productService;
	private final FavoriteService favoriteService;
	private final UserRepository userRepository;
	private final CategoryService categoryService;
	private final InventoryService inventoryService; 

	public ProductController(
			ProductService productService,
			FavoriteService favoriteService,
			UserRepository userRepository,
			CategoryService categoryService,
			InventoryService inventoryService) { 
		this.productService = productService;
		this.favoriteService = favoriteService;
		this.userRepository = userRepository;
		this.categoryService = categoryService;
		this.inventoryService = inventoryService; 
	}

	@GetMapping({ "/", "/products" })
	public String index(
			@RequestParam(required = false) Integer categoryId,
			@RequestParam(required = false) String keyword,
			@RequestParam(name = "sort", defaultValue = "new") String sort,
			@PageableDefault(size = 20) Pageable pageable,
			Model model,
			@AuthenticationPrincipal User loginUser,
			HttpSession session) {

		// カテゴリ一覧（SHOP BY CATEGORY用）
		List<Category> categories = categoryService.findAllCategories();
		model.addAttribute("categories", categories);

		// 一覧・検索・カテゴリ・並び替えを1本化
		Page<Product> productPage = productService.searchProducts(keyword, sort, pageable, categoryId);

		// 商品ごとの在庫Mapを作成
		Map<Integer, Integer> stockMap = new LinkedHashMap<>();
		for (Product product : productPage.getContent()) {
			stockMap.put(product.getId(), inventoryService.getAvailableStock(product.getId()));
		}

		model.addAttribute("productPage", productPage);
		model.addAttribute("sort", sort);
		model.addAttribute("keyword", keyword);
		model.addAttribute("selectedCategoryId", categoryId);
		model.addAttribute("categories", categories);
		model.addAttribute("stockMap", stockMap); 

		// ログイン前後どちらでもお気に入り表示をそろえる
		model.addAttribute("favoriteProductIdSet", resolveFavoriteProductIdSet(loginUser, session));

		return "products/index";
	}

	@GetMapping("/products/{id}")
	public String show(
			@PathVariable Integer id,
			Model model,
			@AuthenticationPrincipal User loginUser,
			HttpSession session) {

		Optional<Product> productOptional = productService.findProductById(id);

		if (productOptional.isEmpty()) {
			return "redirect:/products";
		}

		Product product = productOptional.get();
		model.addAttribute("product", product);

		// 詳細画面用の在庫数を追加
		int stock = inventoryService.getAvailableStock(product.getId());
		model.addAttribute("stock", stock);

		// 詳細表示時に閲覧履歴へ追加
		addProductToHistory(session, product.getId());

		// 詳細でも同じSet
		model.addAttribute("favoriteProductIdSet", resolveFavoriteProductIdSet(loginUser, session));

		return "products/show";
	}

	// ログイン中=DB / ログイン前=Session
	private Set<Integer> resolveFavoriteProductIdSet(User loginUser, HttpSession session) {

		if (loginUser == null) {
			SessionFavorites sessionFavorites = (SessionFavorites) session.getAttribute(SESSION_FAVORITES_KEY);
			if (sessionFavorites == null) {
				return Collections.emptySet();
			}
			return sessionFavorites.getProductIdSet();
		}

		String email = loginUser.getUsername();

		return userRepository.findByEmail(email)
				.map(databaseUser -> favoriteService.findFavoriteProductIds(databaseUser.getId()))
				.orElse(Collections.emptySet());
	}

	// 閲覧履歴をSessionへ保存
	private void addProductToHistory(HttpSession session, Integer productId) {

		SessionHistory sessionHistory = (SessionHistory) session.getAttribute(SESSION_HISTORY_KEY);

		if (sessionHistory == null) {
			sessionHistory = new SessionHistory();
		}

		sessionHistory.add(productId);
		session.setAttribute(SESSION_HISTORY_KEY, sessionHistory);
	}
}