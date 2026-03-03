package com.natsuka.ec.controller;

import java.util.Optional; // 修正（Java）

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication; // 修正（Java）
import org.springframework.security.core.context.SecurityContextHolder; // 修正（Java）
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.natsuka.ec.entity.Product;
import com.natsuka.ec.entity.User; // 修正（Java）
import com.natsuka.ec.repository.UserRepository; // 修正（Java）
import com.natsuka.ec.service.ProductService;

@Controller
public class MyPageController {

	private final ProductService productService;
	private final UserRepository userRepository; // 修正（Java）

	public MyPageController(ProductService productService, UserRepository userRepository) { // 修正（Java）
		this.productService = productService;
		this.userRepository = userRepository; // 修正（Java）
	}

	@GetMapping("/mypage")
	public String myPage(
			@PageableDefault(size = 20) Pageable pageable,
			@RequestParam(name = "sort", defaultValue = "popular") String sort,
			Model model) {

		// 修正（Java）：ログイン中のemailを取得
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();

		// 修正（Java）：ログインユーザーを取得してModelへ
		Optional<User> optionalUser = userRepository.findByEmail(email);
		optionalUser.ifPresent(user -> model.addAttribute("loginUser", user));

		// 既存：商品一覧
		Page<Product> productPage = productService.findActiveProducts(sort, pageable);
		model.addAttribute("productPage", productPage);
		model.addAttribute("sort", sort);

		return "mypage/index";
	}
}