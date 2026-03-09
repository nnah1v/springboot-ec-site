package com.natsuka.ec.service;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.natsuka.ec.entity.CartItem;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;

// Stripe決済処理サービス
// Checkout作成とWebhook処理を担当
@Service
public class StripeService {

	@Value("${stripe.api-key}")
	private String stripeApiKey;

	private final CartService cartService;
	private final OrderService orderService;
	private final ObjectMapper objectMapper;
	private final InventoryService inventoryService;

	public StripeService(
			CartService cartService,
			OrderService orderService,
			ObjectMapper objectMapper,
			InventoryService inventoryService) {

		this.cartService = cartService;
		this.orderService = orderService;
		this.objectMapper = objectMapper;
		this.inventoryService = inventoryService;
	}

	// Stripe Checkout Session作成
	public String createStripeSession(Integer userId, HttpServletRequest httpServletRequest) {

		if (userId == null) {
			return "";
		}

		// カート取得
		List<CartItem> cartItems = cartService.findCartItems(userId);

		if (cartItems.isEmpty()) {
			return "";
		}

		// 決済前の在庫チェック
		for (CartItem cartItem : cartItems) {

			Integer availableStock = inventoryService.getAvailableStock(cartItem.getProduct().getId());

			if (availableStock == null || cartItem.getQuantity() > availableStock) {
				return "";
			}
		}

		int totalAmount = cartService.calculateFinalAmount(userId);

		if (totalAmount <= 0) {
			return "";
		}

		Stripe.apiKey = stripeApiKey;

		String requestUrl = new String(httpServletRequest.getRequestURL());
		String productName = "ご注文商品";

		SessionCreateParams params = SessionCreateParams.builder()
				.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)

				// Stripe表示用の注文データ
				.addLineItem(
						SessionCreateParams.LineItem.builder()
								.setPriceData(
										SessionCreateParams.LineItem.PriceData.builder()
												.setProductData(
														SessionCreateParams.LineItem.PriceData.ProductData.builder()
																.setName(productName)
																.build())
												.setUnitAmount((long) totalAmount)
												.setCurrency("jpy")
												.build())
								.setQuantity(1L)
								.build())

				.setMode(SessionCreateParams.Mode.PAYMENT)

				// 決済成功時
				.setSuccessUrl(requestUrl.replace("/orders", "") + "/orders/complete")

				// 決済キャンセル時
				.setCancelUrl(requestUrl.replace("/orders", "") + "/cart")

				// Webhookで使用するユーザーID
				.setPaymentIntentData(
						SessionCreateParams.PaymentIntentData.builder()
								.putMetadata("userId", userId.toString())
								.build())

				.build();

		try {
			Session session = Session.create(params);
			return session.getId();
		} catch (StripeException exception) {
			exception.printStackTrace();
			return "";
		}
	}

	// Stripe Webhook処理
	public void processSessionCompleted(Event event, String payload) {

		Stripe.apiKey = stripeApiKey;

		try {

			// JSON payload 解析
			JsonNode rootNode = objectMapper.readTree(payload);

			String sessionId = rootNode.path("data").path("object").path("id").asText();

			if (sessionId == null || sessionId.isBlank() || !sessionId.startsWith("cs_")) {
				return;
			}

			// payment_intent 展開取得
			SessionRetrieveParams params = SessionRetrieveParams.builder()
					.addExpand("payment_intent")
					.build();

			Session session = Session.retrieve(sessionId, params, null);

			// metadata取得
			Map<String, String> paymentIntentMetadata = session.getPaymentIntentObject().getMetadata();

			String userIdText = paymentIntentMetadata.get("userId");

			if (userIdText == null || userIdText.isBlank()) {
				return;
			}

			Integer userId = Integer.valueOf(userIdText);

			// 注文生成
			orderService.createOrder(userId);

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}