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

@Service
public class StripeService {

	@Value("${stripe.api-key}")
	private String stripeApiKey;

	private final CartService cartService;
	private final OrderService orderService;
	private final ObjectMapper objectMapper;

	public StripeService(CartService cartService, OrderService orderService, ObjectMapper objectMapper) {
		this.cartService = cartService;
		this.orderService = orderService;
		this.objectMapper = objectMapper;
	}

	public String createStripeSession(Integer userId, HttpServletRequest httpServletRequest) {

		// 修正（Java）：入力不正対策
		if (userId == null) {
			return "";
		}

		List<CartItem> cartItems = cartService.findCartItems(userId);

		// 修正（Java）：カートが空ならStripeへ進ませない
		if (cartItems.isEmpty()) {
			return "";
		}

		int totalAmount = cartService.calculateFinalAmount(userId);

		// 修正（Java）：安全のため0円以下は弾く
		if (totalAmount <= 0) {
			return "";
		}

		Stripe.apiKey = stripeApiKey;

		String requestUrl = new String(httpServletRequest.getRequestURL());

		// 修正（Java）：Stripeの商品名。最小構成として固定文言にする
		String productName = "ご注文商品";

		SessionCreateParams params = SessionCreateParams.builder()
				.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
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
				.setSuccessUrl(
						requestUrl.replace("/orders", "") + "/orders/complete")
				.setCancelUrl(
						requestUrl.replace("/orders", "") + "/cart")
				.setPaymentIntentData(
						SessionCreateParams.PaymentIntentData.builder()
								// 修正（Java）：Webhookで注文保存に必要な最小情報だけ持たせる
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

	public void processSessionCompleted(Event event, String payload) {

		Stripe.apiKey = stripeApiKey;

		try {
			JsonNode rootNode = objectMapper.readTree(payload);

			String sessionId = rootNode.path("data").path("object").path("id").asText();

			// 修正（Java）：sessionIdが不正なら終了
			if (sessionId == null || sessionId.isBlank() || !sessionId.startsWith("cs_")) {
				return;
			}

			SessionRetrieveParams params = SessionRetrieveParams.builder()
					.addExpand("payment_intent")
					.build();

			Session session = Session.retrieve(sessionId, params, null);

			Map<String, String> paymentIntentMetadata = session.getPaymentIntentObject().getMetadata();

			// 修正（Java）：metadataからuserId取得
			String userIdText = paymentIntentMetadata.get("userId");
			if (userIdText == null || userIdText.isBlank()) {
				return;
			}

			Integer userId = Integer.valueOf(userIdText);

			// 修正（Java）：決済完了後にDB保存
			orderService.createOrder(userId);

		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}