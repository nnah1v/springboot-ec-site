package com.natsuka.ec.controller;

import java.util.Optional;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.natsuka.ec.dto.SessionFavorites;
import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.UserRepository;
import com.natsuka.ec.service.FavoriteService;

@Controller
public class FavoriteController {

	private static final String SESSION_FAVORITES_KEY = "SESSION_FAVORITES"; // 修正（Java）

	private final FavoriteService favoriteService;
	private final UserRepository userRepository; // 修正（Java）

	public FavoriteController(FavoriteService favoriteService, UserRepository userRepository) { // 修正（Java）
		this.favoriteService = favoriteService;
		this.userRepository = userRepository; // 修正（Java）
	}

	// =========================
	// 一覧
	// GET /favorites
	// =========================
	@GetMapping("/favorites")
	public String index(Model model, HttpSession session) {

		Integer userId = getCurrentUserIdOrNull(); // 修正（Java）

		if (userId == null) {
			// ゲスト：Session保持（productIdの集合）
			SessionFavorites sessionFavorites = getOrCreateSessionFavorites(session);
			model.addAttribute("favoriteProductIds", sessionFavorites.getProductIdSet());
			model.addAttribute("isGuest", true);
			return "favorites/index";
		}

		// ログイン：DB保持（favoritesテーブル）
		model.addAttribute("favorites", favoriteService.findFavorites(userId));
		model.addAttribute("isGuest", false);
		return "favorites/index";
	}

	// =========================
	// 追加
	// POST /favorites/{productId}
	// =========================
	@PostMapping("/favorites/{productId}")
	public String add(@PathVariable Integer productId, HttpSession session) {

		Integer userId = getCurrentUserIdOrNull(); // 修正（Java）

		if (userId == null) {
			// ゲスト：Session追加
			getOrCreateSessionFavorites(session).add(productId);
			return "redirect:/favorites";
		}

		// ログイン：DB追加（重複は無視する実装にする）
		favoriteService.addFavorite(userId, productId);
		return "redirect:/favorites";
	}

	// =========================
	// 削除
	// POST /favorites/{productId}/delete
	// =========================
	@PostMapping("/favorites/{productId}/delete")
	public String delete(@PathVariable Integer productId, HttpSession session) {

		Integer userId = getCurrentUserIdOrNull(); // 修正（Java）

		if (userId == null) {
			// ゲスト：Session削除
			getOrCreateSessionFavorites(session).remove(productId);
			return "redirect:/favorites";
		}

		// ログイン：DB削除（存在しなくても例外にしない実装にする）
		favoriteService.removeFavorite(userId, productId);
		return "redirect:/favorites";
	}

	// -------------------------
	// Session helper
	// -------------------------
	private SessionFavorites getOrCreateSessionFavorites(HttpSession session) {

		SessionFavorites sessionFavorites = (SessionFavorites) session.getAttribute(SESSION_FAVORITES_KEY);

		if (sessionFavorites == null) {
			sessionFavorites = new SessionFavorites();
			session.setAttribute(SESSION_FAVORITES_KEY, sessionFavorites);
		}

		return sessionFavorites;
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