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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.natsuka.ec.dto.SessionCart;
import com.natsuka.ec.entity.Product;
import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.ProductRepository;
import com.natsuka.ec.repository.UserRepository;
import com.natsuka.ec.service.CartService;

//MVCのController。HTML(Thymeleaf)を返す画面用コントローラー
@Controller
public class CartController {

	//セッションに保存するカートオブジェクトのキー名
	private static final String SESSION_CART_KEY = "SESSION_CART";

	//カートのビジネスロジック（DBカート・セッションカート計算など）
	private final CartService cartService;

	//ログインユーザー取得（email→User）
	private final UserRepository userRepository;

	//商品情報取得（商品名・価格など）
	private final ProductRepository productRepository;

	//コンストラクタインジェクション（Springが自動DI）
	public CartController(
			CartService cartService,
			UserRepository userRepository,
			ProductRepository productRepository) {

		this.cartService = cartService;
		this.userRepository = userRepository;
		this.productRepository = productRepository;
	}

	// =================================================
	// カート画面表示
	// URL: GET /cart
	// =================================================
	@GetMapping("/cart")
	public String index(Model model, HttpSession session) {

		//ログインユーザーID取得（未ログインならnull）
		Integer userId = getCurrentUserIdOrNull();

		// ===============================
		// 未ログインユーザー（ゲスト）
		// ===============================
		if (userId == null) {

			//セッションカート取得（なければ新規作成）
			SessionCart sessionCart = getOrCreateSessionCart(session);

			//Map<productId, quantity> → 表示用DTOへ変換
			List<CartLineView> cartLines = convertSessionCartToLines(sessionCart.asUnmodifiableMap());

			model.addAttribute("cartLines", cartLines);
			model.addAttribute("isGuest", true);

			//合計金額計算（SessionCart用）
			int totalAmount = cartService.calculateSessionTotalAmount(sessionCart);

			//割引額（例：3点以上10%など）
			int discountAmount = cartService.calculateSessionDiscountAmount(sessionCart);

			//最終支払金額
			int finalAmount = cartService.calculateSessionFinalAmount(sessionCart);

			model.addAttribute("totalAmount", totalAmount);
			model.addAttribute("discountAmount", discountAmount);
			model.addAttribute("finalAmount", finalAmount);

			return "cart/index";
		}

		// ===============================
		// ログインユーザー（DBカート）
		// ===============================

		//DBに保存されたカート商品取得
		model.addAttribute("cartLines", cartService.findCartItems(userId));
		model.addAttribute("isGuest", false);

		//DBカート金額計算
		int totalAmount = cartService.calculateTotalAmount(userId);
		int discountAmount = cartService.calculateDiscountAmount(userId);
		int finalAmount = cartService.calculateFinalAmount(userId);

		model.addAttribute("totalAmount", totalAmount);
		model.addAttribute("discountAmount", discountAmount);
		model.addAttribute("finalAmount", finalAmount);

		return "cart/index";
	}

	// =================================================
	// 商品をカートへ追加
	// URL: POST /cart/add
	// =================================================
	@PostMapping("/cart/add")
	public String add(
			@RequestParam("productId") Integer productId,
			@RequestParam(name = "quantity", defaultValue = "1") Integer quantity,
			HttpSession session,
			RedirectAttributes redirectAttributes) {

		//ログインユーザーID取得
		Integer userId = getCurrentUserIdOrNull();

		try {

			// ===============================
			// 未ログイン（セッションカート）
			// ===============================
			if (userId == null) {

				SessionCart sessionCart = getOrCreateSessionCart(session);

				//セッションカートへ追加
				cartService.addToSessionCart(sessionCart, productId, quantity);

				//画面メッセージ
				redirectAttributes.addFlashAttribute(
						"successMessage", "商品をカートに追加しました。");

				return "redirect:/cart";
			}

			// ===============================
			// ログインユーザー（DBカート）
			// ===============================
			cartService.addCartItem(userId, productId, quantity);

			redirectAttributes.addFlashAttribute(
					"successMessage", "商品をカートに追加しました。");

			return "redirect:/cart";

		} catch (IllegalStateException illegalStateException) {

			//在庫不足などの例外
			redirectAttributes.addFlashAttribute(
					"errorMessage", illegalStateException.getMessage());

			return "redirect:/cart";
		}
	}

	// =================================================
	// セッションカート取得（なければ作成）
	// =================================================
	private SessionCart getOrCreateSessionCart(HttpSession session) {

		//セッションからカート取得
		SessionCart sessionCart = (SessionCart) session.getAttribute(SESSION_CART_KEY);

		//未作成なら新規作成
		if (sessionCart == null) {

			sessionCart = new SessionCart();

			//セッションへ保存
			session.setAttribute(SESSION_CART_KEY, sessionCart);
		}

		return sessionCart;
	}

	// =================================================
	// セッションカート → 表示用DTO変換
	// =================================================
	private List<CartLineView> convertSessionCartToLines(
			Map<Integer, Integer> cartMap) {

		List<CartLineView> cartLines = new ArrayList<>();

		for (Map.Entry<Integer, Integer> entry : cartMap.entrySet()) {

			Integer productId = entry.getKey();
			Integer quantity = entry.getValue();

			//商品情報取得
			Product product = productRepository.findById(productId).orElse(null);

			if (product == null) {
				continue;
			}

			//単価
			int unitPrice = product.getPrice();

			//行合計
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

	// =================================================
	// 表示用DTO（ゲストカート）
	// =================================================
	public static class CartLineView {

		private final Integer productId;
		private final String productName;
		private final Integer unitPrice;
		private final Integer quantity;
		private final Integer lineTotal;

		//DTOコンストラクタ
		public CartLineView(
				Integer productId,
				String productName,
				Integer unitPrice,
				Integer quantity,
				Integer lineTotal) {

			this.productId = productId;
			this.productName = productName;
			this.unitPrice = unitPrice;
			this.quantity = quantity;
			this.lineTotal = lineTotal;
		}

		//Thymeleaf参照用Getter
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

	// =================================================
	// Spring Security認証 → userId取得
	// =================================================
	private Integer getCurrentUserIdOrNull() {

		//SecurityContextから認証情報取得
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		//未ログインチェック
		if (authentication == null ||
				!authentication.isAuthenticated() ||
				"anonymousUser".equals(authentication.getPrincipal())) {

			return null;
		}

		//username = email
		String email = authentication.getName();

		Optional<User> user = userRepository.findByEmail(email);

		if (user.isEmpty()) {
			return null;
		}

		//users.id を返す
		return user.get().getId();
	}
}