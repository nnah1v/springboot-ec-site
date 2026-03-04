package com.natsuka.ec.controller;

import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.natsuka.ec.dto.SessionCart;
import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.UserRepository;
import com.natsuka.ec.service.CartService;

@Controller
public class CartController {

	private static final String SESSION_CART_KEY = "SESSION_CART"; // 修正（Java）

	private final CartService cartService;
	private final UserRepository userRepository; // 修正（Java）

	public CartController(CartService cartService, UserRepository userRepository) { // 修正（Java）
		this.cartService = cartService;
		this.userRepository = userRepository; // 修正（Java）
	}

	@GetMapping("/cart")
	public String index(Model model, HttpSession session) {

		Integer userId = getCurrentUserIdOrNull(); // 修正（Java）

		if (userId == null) {
			SessionCart sessionCart = getOrCreateSessionCart(session);
			model.addAttribute("cartMap", sessionCart.asUnmodifiableMap());
			model.addAttribute("isGuest", true);
			return "cart/index";
		}

		model.addAttribute("cartItems", cartService.findCartItems(userId));
		// model.addAttribute("totalAmount", cartService.calculateTotalAmount(userId)); // 修正（Java）：未定義のため削除
		model.addAttribute("isGuest", false);
		return "cart/index";
	}

	@PostMapping("/cart/add")
	public String add(
			@RequestParam("productId") Integer productId,
			@RequestParam(name = "quantity", defaultValue = "1") Integer quantity,
			HttpSession session) {

		Integer userId = getCurrentUserIdOrNull(); // 修正（Java）

		if (userId == null) {
			cartService.addToSessionCart(getOrCreateSessionCart(session), productId, quantity);
			return "redirect:/cart";
		}

		cartService.addCartItem(userId, productId, quantity);
		return "redirect:/cart";
	}

	@PostMapping("/cart/update")
	public String update(
			@RequestParam("productId") Integer productId,
			@RequestParam("quantity") Integer quantity,
			HttpSession session) {

		Integer userId = getCurrentUserIdOrNull(); // 修正（Java）

		if (userId == null) {
			cartService.updateSessionCart(getOrCreateSessionCart(session), productId, quantity);
			return "redirect:/cart";
		}

		cartService.updateCartItemQuantity(userId, productId, quantity);
		return "redirect:/cart";
	}

	@PostMapping("/cart/delete")
	public String delete(
			@RequestParam("productId") Integer productId,
			HttpSession session) {

		Integer userId = getCurrentUserIdOrNull(); // 修正（Java）

		if (userId == null) {
			cartService.removeFromSessionCart(getOrCreateSessionCart(session), productId);
			return "redirect:/cart";
		}

		cartService.removeCartItem(userId, productId);
		return "redirect:/cart";
	}

	// -------------------------
	// Session helper
	// -------------------------
	private SessionCart getOrCreateSessionCart(HttpSession session) {

		SessionCart sessionCart = (SessionCart) session.getAttribute(SESSION_CART_KEY);

		if (sessionCart == null) {
			sessionCart = new SessionCart();
			session.setAttribute(SESSION_CART_KEY, sessionCart);
		}

		return sessionCart;
	}

	// -------------------------
	// Auth helper（email→userId）
	// -------------------------
	private Integer getCurrentUserIdOrNull() { // 修正（Java）

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return null;
		}

		String email = authentication.getName(); // 修正（Java）
		Optional<User> user = userRepository.findByEmail(email); // 修正（Java）

		if (user.isEmpty()) {
			return null;
		}

		return user.get().getId();
	}
}