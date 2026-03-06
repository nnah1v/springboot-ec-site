package com.natsuka.ec.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.natsuka.ec.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

@Controller
public class StripeWebhookController {

	private final StripeService stripeService;

	@Value("${stripe.api-key}")
	private String stripeApiKey;

	@Value("${stripe.webhook-secret}")
	private String webhookSecret;

	public StripeWebhookController(StripeService stripeService) {
		this.stripeService = stripeService;
	}

	@PostMapping("/stripe/webhook")
	public ResponseEntity<String> webhook(
			@RequestBody String payload,
			@RequestHeader("Stripe-Signature") String signatureHeader) {

		Stripe.apiKey = stripeApiKey;

		Event event;

		try {
			event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
		} catch (SignatureVerificationException exception) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}

		if ("checkout.session.completed".equals(event.getType())) {
			stripeService.processSessionCompleted(event, payload);
		}

		return new ResponseEntity<>("Success", HttpStatus.OK);
	}
}