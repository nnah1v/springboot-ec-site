package com.natsuka.ec.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.natsuka.ec.entity.Product;
import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.ProductRepository;
import com.natsuka.ec.repository.UserRepository;
import com.natsuka.ec.service.CartService;

@Controller
public class CartController {

	private static final String SESSION_CART_KEY = "SESSION_CART";

	private final CartService cartService;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;

	public CartController(CartService cartService, UserRepository userRepository, ProductRepository productRepository) {
		this.cartService = cartService;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
	}

	@GetMapping("/cart")
	public String index(Model model, HttpSession session) {

		Integer userId = getCurrentUserIdOrNull();

		if (userId == null) {
			SessionCart sessionCart = getOrCreateSessionCart(session);

			List<CartLineView> cartLines = convertSessionCartToLines(sessionCart.asUnmodifiableMap());
			model.addAttribute("cartLines", cartLines);
			model.addAttribute("isGuest", true);

			// 修正（Java）：合計（ゲスト）
			int totalAmount = 0;
			for (CartLineView line : cartLines) {
				totalAmount += line.getLineTotal();
			}
			model.addAttribute("totalAmount", totalAmount);

			return "cart/index";
		}

		// 修正（Java）：ログイン時
		model.addAttribute("cartLines", cartService.findCartItems(userId));
		model.addAttribute("isGuest", false);

		// 修正（Java）：合計（ログイン）
		model.addAttribute("totalAmount", cartService.calculateTotalAmount(userId));

		return "cart/index";
	}

	@PostMapping("/cart/add")
	public String add(
			@RequestParam("productId") Integer productId,
			@RequestParam(name = "quantity", defaultValue = "1") Integer quantity,
			HttpSession session) {

		Integer userId = getCurrentUserIdOrNull();

		if (userId == null) {
			SessionCart sessionCart = getOrCreateSessionCart(session);
			cartService.addToSessionCart(sessionCart, productId, quantity);

			
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

		Integer userId = getCurrentUserIdOrNull();

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

		Integer userId = getCurrentUserIdOrNull();

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
	// 修正（Java）：ゲストMapを「商品情報つき」に変換
	// -------------------------
	private List<CartLineView> convertSessionCartToLines(Map<Integer, Integer> cartMap) {

		List<CartLineView> cartLines = new ArrayList<>();

		for (Map.Entry<Integer, Integer> entry : cartMap.entrySet()) {
			Integer productId = entry.getKey();
			Integer quantity = entry.getValue();

			Product product = productRepository.findById(productId).orElse(null);
			if (product == null) {
				continue;
			}

			int unitPrice = product.getPrice();
			int lineTotal = unitPrice * quantity;

			cartLines.add(new CartLineView(
					product.getId(),
					product.getName(),
					unitPrice,
					quantity,
					lineTotal));
		}

		return cartLines;
	}

	// 修正（Java）：テンプレ表示用DTO（ゲスト用）
	public static class CartLineView {
		private final Integer productId;
		private final String productName;
		private final Integer unitPrice;
		private final Integer quantity;
		private final Integer lineTotal;

		public CartLineView(Integer productId, String productName, Integer unitPrice, Integer quantity,
				Integer lineTotal) {
			this.productId = productId;
			this.productName = productName;
			this.unitPrice = unitPrice;
			this.quantity = quantity;
			this.lineTotal = lineTotal;
		}

		public Integer getProductId() {
			return productId;
		}

		public String getProductName() {
			return productName;
		}

		public Integer getUnitPrice() {
			return unitPrice;
		}

		public Integer getQuantity() {
			return quantity;
		}

		public Integer getLineTotal() {
			return lineTotal;
		}
	}

	// -------------------------
	// Auth helper（email→userId）
	// -------------------------
	private Integer getCurrentUserIdOrNull() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return null;
		}

		String email = authentication.getName();
		Optional<User> user = userRepository.findByEmail(email);

		if (user.isEmpty()) {
			return null;
		}

		return user.get().getId();
	}
}