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

@Service
public class OrderService {

	private final CartItemRepository cartItemRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final DiscountRuleRepository discountRuleRepository;

	public OrderService(
			CartItemRepository cartItemRepository,
			OrderRepository orderRepository,
			OrderItemRepository orderItemRepository,
			DiscountRuleRepository discountRuleRepository) {

		this.cartItemRepository = cartItemRepository;
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.discountRuleRepository = discountRuleRepository;
	}

	@Transactional
	public Order createOrder(Integer userId) {

		// 修正（Java）：null防御
		if (userId == null) {
			throw new IllegalArgumentException("userIdがnullです。");
		}

		List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByUpdatedAtDesc(userId);

		// 修正（Java）：空カート対策
		if (cartItems.isEmpty()) {
			throw new IllegalStateException("カートが空です。");
		}

		int subtotal = cartItems.stream()
				.mapToInt(cartItem -> cartItem.getProduct().getPrice() * cartItem.getQuantity())
				.sum();

		int discount = calculateDiscountAmount(subtotal);
		int total = subtotal - discount;

		Order order = new Order();
		order.setUserId(userId);
		order.setStatus("PAID"); // 修正（Java）：Webhook到達後なのでPAIDで保存
		order.setSubtotal(subtotal);
		order.setDiscount(discount);
		order.setTotal(total);

		Order savedOrder = orderRepository.save(order);

		for (CartItem cartItem : cartItems) {
			OrderItem orderItem = new OrderItem();
			orderItem.setOrder(savedOrder);
			orderItem.setProduct(cartItem.getProduct());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setUnitPrice(cartItem.getProduct().getPrice());
			orderItem.setLineTotal(cartItem.getProduct().getPrice() * cartItem.getQuantity());

			orderItemRepository.save(orderItem);
		}

		// 修正（Java）：保存後にカートを空にする
		cartItemRepository.deleteAll(cartItems);

		return savedOrder;
	}

	// 修正（Java）：注文履歴取得
	public List<Order> findByUserId(Integer userId) {
		return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	// 修正（Java）：割引計算
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