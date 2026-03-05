package com.natsuka.ec.controller;

import java.util.Collections;
import java.util.List;
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
import com.natsuka.ec.entity.Category;
import com.natsuka.ec.entity.Product;
import com.natsuka.ec.repository.UserRepository;
import com.natsuka.ec.service.CategoryService;
import com.natsuka.ec.service.FavoriteService;
import com.natsuka.ec.service.ProductService;

@Controller
public class ProductController {

	private static final String SESSION_FAVORITES_KEY = "SESSION_FAVORITES";

	private final ProductService productService;
	private final FavoriteService favoriteService;
	private final UserRepository userRepository;
	private final CategoryService categoryService;

	public ProductController(ProductService productService, FavoriteService favoriteService,
			UserRepository userRepository, CategoryService categoryService) {
		this.productService = productService;
		this.favoriteService = favoriteService;
		this.userRepository = userRepository;
		this.categoryService = categoryService;
	}

	@GetMapping({ "/", "/products" })
	public String index(
			@RequestParam(required = false) Integer category, // 修正（Java）
			@RequestParam(name = "sort", defaultValue = "new") String sort,
			@PageableDefault(size = 20) Pageable pageable,
			Model model,
			@AuthenticationPrincipal User loginUser,
			HttpSession session) {

		// カテゴリ一覧（SHOP BY CATEGORY用）
		List<Category> categories = categoryService.findAllCategories();
		model.addAttribute("categories", categories);

		// 修正（Java）：categoryをServiceに渡す
		Page<Product> productPage = productService.findActiveProducts(sort, pageable, category); // 修正（Java）

		model.addAttribute("productPage", productPage);
		model.addAttribute("sort", sort);
		model.addAttribute("selectedCategoryId", category); // 修正（Java）：名前はそのままでも動くが実態はString

		// ログイン前も反映できるSetを渡す
		model.addAttribute("favoriteProductIdSet", resolveFavoriteProductIdSet(loginUser, session));

		return "products/index";
	}

	@GetMapping("/products/{id}")
	public String show(
			@PathVariable Integer id,
			Model model,
			@AuthenticationPrincipal User loginUser,
			HttpSession session) {

		Optional<Product> product = productService.findProductById(id);

		if (product.isEmpty()) {
			return "redirect:/";
		}

		model.addAttribute("product", product.get());

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
				.map(dbUser -> favoriteService.findFavoriteProductIds(dbUser.getId()))
				.orElse(Collections.emptySet());
	}
}