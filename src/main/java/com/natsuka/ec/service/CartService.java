package com.natsuka.ec.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.natsuka.ec.dto.SessionCart;
import com.natsuka.ec.entity.CartItem;
import com.natsuka.ec.entity.DiscountRule;//割引ルールエンティティを追加
import com.natsuka.ec.entity.Product;
import com.natsuka.ec.repository.CartItemRepository;
import com.natsuka.ec.repository.DiscountRuleRepository;//：割引ルールRepositoryを追加
import com.natsuka.ec.repository.ProductRepository;

@Service
public class CartService {

	private final CartItemRepository cartItemRepository;
	private final ProductRepository productRepository;
	private final DiscountRuleRepository discountRuleRepository;//：割引ルール取得用

	public CartService(
			CartItemRepository cartItemRepository,
			ProductRepository productRepository,
			DiscountRuleRepository discountRuleRepository) {

		this.cartItemRepository = cartItemRepository;
		this.productRepository = productRepository;
		this.discountRuleRepository = discountRuleRepository;
	}

	public List<CartItem> findCartItems(Integer userId) {
		return cartItemRepository.findByUserIdOrderByUpdatedAtDesc(userId);
	}

	//：合計金額（DB）
	public int calculateTotalAmount(Integer userId) {
		List<CartItem> cartItems = findCartItems(userId);
		int totalAmount = 0;

		for (CartItem cartItem : cartItems) {
			totalAmount += cartItem.getProduct().getPrice() * cartItem.getQuantity();
		}

		return totalAmount;
	}

	//：割引金額（DB）
	public int calculateDiscountAmount(Integer userId) {
		int totalAmount = calculateTotalAmount(userId);

		//：有効な金額条件割引を1件取得
		DiscountRule discountRule = discountRuleRepository
				.findFirstByRuleTypeAndIsActiveTrue("CART_AMOUNT_GTE_PERCENT_OFF");

		if (discountRule == null) {
			return 0;
		}

		if (totalAmount >= discountRule.getMinAmount()) {
			return totalAmount * discountRule.getPercentOff() / 100;
		}

		return 0;
	}

	//：割引後合計（DB）
	public int calculateFinalAmount(Integer userId) {
		int totalAmount = calculateTotalAmount(userId);
		int discountAmount = calculateDiscountAmount(userId);

		return totalAmount - discountAmount;
	}

	@Transactional
	public void addCartItem(Integer userId, Integer productId, Integer addQuantity) {
		if (userId == null || productId == null || addQuantity == null || addQuantity <= 0) {
			return;
		}

		Optional<CartItem> existing = cartItemRepository.findByUserIdAndProduct_Id(userId, productId);
		if (existing.isPresent()) {
			CartItem cartItem = existing.get();
			cartItem.setQuantity(cartItem.getQuantity() + addQuantity);
			cartItemRepository.save(cartItem);
			return;
		}

		Product product = productRepository.findById(productId).orElse(null);
		if (product == null) {
			return;
		}

		CartItem cartItem = new CartItem();
		cartItem.setUserId(userId);
		cartItem.setProduct(product);
		cartItem.setQuantity(addQuantity);
		cartItemRepository.save(cartItem);
	}

	@Transactional
	public void updateCartItemQuantity(Integer userId, Integer productId, Integer newQuantity) {
		if (userId == null || productId == null || newQuantity == null) {
			return;
		}

		if (newQuantity <= 0) {
			removeCartItem(userId, productId);
			return;
		}

		cartItemRepository.findByUserIdAndProduct_Id(userId, productId).ifPresent(cartItem -> {
			cartItem.setQuantity(newQuantity);
			cartItemRepository.save(cartItem);
		});
	}

	@Transactional
	public void removeCartItem(Integer userId, Integer productId) {
		if (userId == null || productId == null) {
			return;
		}

		cartItemRepository.findByUserIdAndProduct_Id(userId, productId)
				.ifPresent(cartItemRepository::delete);
	}

	//--------ログイン前（Session）--------

	public void addToSessionCart(SessionCart sessionCart, Integer productId, Integer addQuantity) {
		if (sessionCart == null) {
			return;
		}
		sessionCart.add(productId, addQuantity);
	}

	public void updateSessionCart(SessionCart sessionCart, Integer productId, Integer newQuantity) {
		if (sessionCart == null) {
			return;
		}
		sessionCart.update(productId, newQuantity);
	}

	public void removeFromSessionCart(SessionCart sessionCart, Integer productId) {
		if (sessionCart == null) {
			return;
		}
		sessionCart.remove(productId);
	}

	//：合計金額（Session）
	public int calculateSessionTotalAmount(SessionCart sessionCart) {
		if (sessionCart == null || sessionCart.isEmpty()) {
			return 0;
		}

		int totalAmount = 0;

		for (Map.Entry<Integer, Integer> entry : sessionCart.asUnmodifiableMap().entrySet()) {
			Integer productId = entry.getKey();
			Integer quantity = entry.getValue();

			Product product = productRepository.findById(productId).orElse(null);
			if (product == null) {
				continue;
			}

			totalAmount += product.getPrice() * quantity;
		}

		return totalAmount;
	}

	//：割引金額（Session）
	public int calculateSessionDiscountAmount(SessionCart sessionCart) {
		int totalAmount = calculateSessionTotalAmount(sessionCart);

		//：有効な金額条件割引を1件取得
		DiscountRule discountRule = discountRuleRepository
				.findFirstByRuleTypeAndIsActiveTrue("CART_AMOUNT_GTE_PERCENT_OFF");

		if (discountRule == null) {
			return 0;
		}

		if (totalAmount >= discountRule.getMinAmount()) {
			return totalAmount * discountRule.getPercentOff() / 100;
		}

		return 0;
	}

	//：割引後合計（Session）
	public int calculateSessionFinalAmount(SessionCart sessionCart) {
		int totalAmount = calculateSessionTotalAmount(sessionCart);
		int discountAmount = calculateSessionDiscountAmount(sessionCart);

		return totalAmount - discountAmount;
	}

	@Transactional
	public void mergeSessionCartToUser(Integer userId, SessionCart sessionCart) {
		if (userId == null || sessionCart == null || sessionCart.isEmpty()) {
			return;
		}

		for (Map.Entry<Integer, Integer> entry : sessionCart.asUnmodifiableMap().entrySet()) {
			Integer productId = entry.getKey();
			Integer addQuantity = entry.getValue();
			addCartItem(userId, productId, addQuantity);
		}
	}
}