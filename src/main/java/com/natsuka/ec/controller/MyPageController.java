package com.natsuka.ec.controller;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.natsuka.ec.entity.Category;
import com.natsuka.ec.entity.Product;
import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.UserRepository;
import com.natsuka.ec.service.CategoryService;
import com.natsuka.ec.service.FavoriteService;
import com.natsuka.ec.service.InventoryService;
import com.natsuka.ec.service.ProductService;

@Controller
public class MyPageController {

	private final ProductService productService;
	private final UserRepository userRepository;
	private final FavoriteService favoriteService;
	private final CategoryService categoryService;
	private final InventoryService inventoryService; 

	public MyPageController(
			ProductService productService,
			UserRepository userRepository,
			FavoriteService favoriteService,
			CategoryService categoryService,
			InventoryService inventoryService) { 
		this.productService = productService;
		this.userRepository = userRepository;
		this.favoriteService = favoriteService;
		this.categoryService = categoryService;
		this.inventoryService = inventoryService; 
	}

	@GetMapping("/mypage")
	public String myPage(
			@RequestParam(name = "categoryId", required = false) Integer categoryId, // fragmentのURLパラメータ名に合わせる
			@RequestParam(name = "sort", defaultValue = "popular") String sort,
			@PageableDefault(size = 20) Pageable pageable,
			Model model) {

		// ログイン中ユーザーのメールアドレスを取得
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();

		// ログインユーザー情報をModelへ渡す
		Optional<User> optionalUser = userRepository.findByEmail(email);
		optionalUser.ifPresent(user -> model.addAttribute("loginUser", user));

		// お気に入り商品ID一覧を取得して画面へ渡す
		Set<Integer> favoriteProductIdSet = Collections.emptySet();
		if (optionalUser.isPresent()) {
			Integer userId = optionalUser.get().getId();
			favoriteProductIdSet = favoriteService.findFavoriteProductIds(userId);
		}
		model.addAttribute("favoriteProductIdSet", favoriteProductIdSet);

		// ヘッダーfragment用のカテゴリ一覧
		List<Category> categories = categoryService.findAllCategories();
		model.addAttribute("categories", categories);

		// カテゴリ絞り込み込みで商品一覧を取得
		Page<Product> productPage = productService.findActiveProducts(sort, pageable, categoryId);
		model.addAttribute("productPage", productPage);
		model.addAttribute("sort", sort);
		model.addAttribute("selectedCategoryId", categoryId);

		// 商品ごとの在庫Mapを作成して画面へ渡す
		Map<Integer, Integer> stockMap = new LinkedHashMap<>();
		for (Product product : productPage.getContent()) {
			Integer availableStock = inventoryService.getAvailableStock(product.getId());
			stockMap.put(product.getId(), availableStock);
		}
		model.addAttribute("stockMap", stockMap);

		return "mypage/index";
	}
}