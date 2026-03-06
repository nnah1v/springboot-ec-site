package com.natsuka.ec.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.natsuka.ec.entity.Category;
import com.natsuka.ec.entity.Order;
import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.UserRepository;
import com.natsuka.ec.service.CategoryService;
import com.natsuka.ec.service.OrderService;
import com.natsuka.ec.service.StripeService;

@Controller
@RequestMapping("/orders")
public class OrderController {

	private final StripeService stripeService;
	private final OrderService orderService;
	private final UserRepository userRepository;
	private final CategoryService categoryService; 

	@Value("${stripe.public-key}")
	private String stripePublicKey;

	public OrderController(
			StripeService stripeService,
			OrderService orderService,
			UserRepository userRepository,
			CategoryService categoryService) { 

		this.stripeService = stripeService;
		this.orderService = orderService;
		this.userRepository = userRepository;
		this.categoryService = categoryService; 
	}

	//：注文履歴一覧
	@GetMapping
	public String index(Authentication authentication, Model model) {

		if (authentication == null || authentication.getName() == null) {
			return "redirect:/login";
		}

		String email = authentication.getName();

		User user = userRepository.findByEmail(email).orElse(null);

		if (user == null) {
			return "redirect:/login";
		}

		List<Order> orderList = orderService.findByUserId(user.getId());
		model.addAttribute("orderList", orderList);

		//：header fragment用のカテゴリ一覧
		List<Category> categories = categoryService.findAllCategories();
		model.addAttribute("categories", categories);

		return "orders/index";
	}

	@PostMapping
	public String createOrder(Authentication authentication, HttpServletRequest httpServletRequest) {

		if (authentication == null || authentication.getName() == null) {
			return "redirect:/login";
		}

		String email = authentication.getName();

		User user = userRepository.findByEmail(email).orElse(null);

		if (user == null) {
			return "redirect:/cart";
		}

		String sessionId = stripeService.createStripeSession(user.getId(), httpServletRequest);

		if (sessionId == null || sessionId.isBlank()) {
			return "redirect:/cart";
		}

		return "redirect:/orders/checkout?sessionId=" + sessionId;
	}

	@GetMapping("/checkout")
	public String checkout(@RequestParam("sessionId") String sessionId, Model model) {
		model.addAttribute("sessionId", sessionId);
		model.addAttribute("stripePublicKey", stripePublicKey);
		return "orders/checkout";
	}

	@GetMapping("/complete")
	public String complete() {
		return "orders/complete";
	}
}