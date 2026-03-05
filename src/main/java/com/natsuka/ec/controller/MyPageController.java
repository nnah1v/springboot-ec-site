package com.natsuka.ec.controller;

import java.util.Collections;
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

import com.natsuka.ec.entity.Product;
import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.UserRepository;
import com.natsuka.ec.service.FavoriteService;
import com.natsuka.ec.service.ProductService;

@Controller
public class MyPageController {

	private final ProductService productService;
	private final UserRepository userRepository;
	private final FavoriteService favoriteService;

	public MyPageController(ProductService productService, UserRepository userRepository,
			FavoriteService favoriteService) { 
		this.productService = productService;
		this.userRepository = userRepository;
		this.favoriteService = favoriteService; 
	}

	@GetMapping("/mypage")
	public String myPage(
			@PageableDefault(size = 20) Pageable pageable,
			@RequestParam(name = "sort", defaultValue = "popular") String sort,
			Model model) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();

		Optional<User> optionalUser = userRepository.findByEmail(email);
		optionalUser.ifPresent(user -> model.addAttribute("loginUser", user));

		// ：お気に入りSetをModelへ（戻っても維持の本体）
		Set<Integer> favoriteProductIdSet = Collections.emptySet();
		if (optionalUser.isPresent()) {
			Integer userId = optionalUser.get().getId();
			favoriteProductIdSet = favoriteService.findFavoriteProductIds(userId);
		}
		model.addAttribute("favoriteProductIdSet", favoriteProductIdSet);

		Page<Product> productPage = productService.findActiveProducts(sort, pageable);
		model.addAttribute("productPage", productPage);
		model.addAttribute("sort", sort);

		return "mypage/index";
	}
}