package com.natsuka.ec.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.natsuka.ec.dto.SessionCart;
import com.natsuka.ec.entity.CartItem;
import com.natsuka.ec.repository.CartItemRepository;

@Service
public class CartService {

	private final CartItemRepository cartItemRepository;

	public CartService(CartItemRepository cartItemRepository) { // 修正（Java）
		this.cartItemRepository = cartItemRepository; // 修正（Java）
	}

	public List<CartItem> findCartItems(Integer userId) {
		return cartItemRepository.findByUserIdOrderByUpdatedAtDesc(userId);
	}

	@Transactional
	public void addCartItem(Integer userId, Integer productId, Integer addQuantity) {
		if (userId == null || productId == null || addQuantity == null || addQuantity <= 0) {
			return;
		}

		Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, productId);
		if (existing.isPresent()) {
			CartItem cartItem = existing.get();
			cartItem.setQuantity(cartItem.getQuantity() + addQuantity); // 修正（Java）：数量加算
			cartItemRepository.save(cartItem);
			return;
		}

		CartItem cartItem = new CartItem();
		cartItem.setUserId(userId);
		cartItem.setProductId(productId);
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

		cartItemRepository.findByUserIdAndProductId(userId, productId).ifPresent(cartItem -> {
			cartItem.setQuantity(newQuantity); // 修正（Java）
			cartItemRepository.save(cartItem);
		});
	}

	@Transactional
	public void removeCartItem(Integer userId, Integer productId) {
		if (userId == null || productId == null) {
			return;
		}
		cartItemRepository.findByUserIdAndProductId(userId, productId)
				.ifPresent(cartItemRepository::delete);
	}

	// -------- ログイン前（Session）--------
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

	// -------- ログイン成功時：Session→DB --------
	@Transactional
	public void mergeSessionCartToUser(Integer userId, SessionCart sessionCart) {
		if (userId == null || sessionCart == null || sessionCart.isEmpty()) {
			return;
		}

		for (Map.Entry<Integer, Integer> entry : sessionCart.asUnmodifiableMap().entrySet()) {
			Integer productId = entry.getKey();
			Integer addQuantity = entry.getValue();
			addCartItem(userId, productId, addQuantity); // 修正（Java）
		}
	}
}