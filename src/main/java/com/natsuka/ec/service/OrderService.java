package com.natsuka.ec.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.natsuka.ec.entity.CartItem;
import com.natsuka.ec.entity.DiscountRule;
import com.natsuka.ec.entity.Order;
import com.natsuka.ec.entity.OrderItem;
import com.natsuka.ec.repository.CartItemRepository;
import com.natsuka.ec.repository.DiscountRuleRepository;
import com.natsuka.ec.repository.OrderItemRepository;
import com.natsuka.ec.repository.OrderRepository;

// 注文処理サービス
// カート内容を元に orders / order_items を生成し在庫を減らす
@Service
public class OrderService {

	private final CartItemRepository cartItemRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final DiscountRuleRepository discountRuleRepository;
	private final InventoryService inventoryService;

	public OrderService(
			CartItemRepository cartItemRepository,
			OrderRepository orderRepository,
			OrderItemRepository orderItemRepository,
			DiscountRuleRepository discountRuleRepository,
			InventoryService inventoryService) {

		this.cartItemRepository = cartItemRepository;
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.discountRuleRepository = discountRuleRepository;
		this.inventoryService = inventoryService;
	}

	// 注文生成処理
	// Webhookから呼ばれる
	@Transactional
	public Order createOrder(Integer userId) {

		if (userId == null) {
			throw new IllegalArgumentException("userIdがnullです。");
		}

		// カート取得
		List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByUpdatedAtDesc(userId);

		if (cartItems.isEmpty()) {
			throw new IllegalStateException("カートが空です。");
		}

		// 最終在庫チェック
		for (CartItem cartItem : cartItems) {

			Integer availableStock = inventoryService.getAvailableStock(cartItem.getProduct().getId());

			if (availableStock == null || cartItem.getQuantity() > availableStock) {
				throw new IllegalStateException("在庫不足の商品があります。");
			}
		}

		// 小計
		int subtotal = cartItems.stream()
				.mapToInt(cartItem -> cartItem.getProduct().getPrice() * cartItem.getQuantity())
				.sum();

		// 割引計算
		int discount = calculateDiscountAmount(subtotal);

		// 最終金額
		int total = subtotal - discount;

		Order order = new Order();
		order.setUserId(userId);
		order.setStatus("PAID");
		order.setSubtotal(subtotal);
		order.setDiscount(discount);
		order.setTotal(total);

		// orders 保存
		Order savedOrder = orderRepository.save(order);

		// order_items 保存
		for (CartItem cartItem : cartItems) {

			OrderItem orderItem = new OrderItem();

			orderItem.setOrder(savedOrder);
			orderItem.setProduct(cartItem.getProduct());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setUnitPrice(cartItem.getProduct().getPrice());

			orderItem.setLineTotal(
					cartItem.getProduct().getPrice() * cartItem.getQuantity());

			orderItemRepository.save(orderItem);

			// 在庫減算ログを追加
			inventoryService.subtractStock(
					cartItem.getProduct().getId(),
					cartItem.getQuantity(),
					savedOrder.getId());
		}

		// カート削除
		cartItemRepository.deleteAll(cartItems);

		return savedOrder;
	}

	// 注文履歴取得
	public List<Order> findByUserId(Integer userId) {
		return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	// 割引計算
	private int calculateDiscountAmount(int subtotal) {

		DiscountRule discountRule = discountRuleRepository
				.findFirstByRuleTypeAndIsActiveTrue("CART_AMOUNT_GTE_PERCENT_OFF");

		if (discountRule == null) {
			return 0;
		}

		if (subtotal >= discountRule.getMinAmount()) {
			return subtotal * discountRule.getPercentOff() / 100;
		}

		return 0;
	}
}