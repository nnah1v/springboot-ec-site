package com.natsuka.ec.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.natsuka.ec.dto.SessionFavorites;
import com.natsuka.ec.entity.Product;
import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.ProductRepository;
import com.natsuka.ec.repository.UserRepository;
import com.natsuka.ec.service.FavoriteService;

@Controller
public class FavoriteController {

	private static final String SESSION_FAVORITES_KEY = "SESSION_FAVORITES"; 

	private final FavoriteService favoriteService;
	private final UserRepository userRepository; 
	private final ProductRepository productRepository; 

	public FavoriteController(FavoriteService favoriteService, UserRepository userRepository,
			ProductRepository productRepository) { 
		this.favoriteService = favoriteService;
		this.userRepository = userRepository; 
		this.productRepository = productRepository; 
	}

	@GetMapping("/favorites")
	public String index(Model model, HttpSession session) {

		Integer userId = getCurrentUserIdOrNull(); 

		if (userId == null) {
			SessionFavorites sessionFavorites = getOrCreateSessionFavorites(session);

			List<Product> favoriteProducts = productRepository.findAllById(sessionFavorites.getProductIdSet()); 
			model.addAttribute("favorites", favoriteProducts); 
			model.addAttribute("isGuest", true);
			return "favorites/index";
		}

		// ログイン：DBのproductId→Product取得
		Set<Integer> favoriteProductIdSet = favoriteService.findFavoriteProductIds(userId); 
		List<Product> favoriteProducts = productRepository.findAllById(favoriteProductIdSet); 

		model.addAttribute("favorites", favoriteProducts); 
		model.addAttribute("isGuest", false);
		return "favorites/index";
	}

	@PostMapping("/favorites/{productId}")
	public String add(@PathVariable Integer productId, HttpSession session) {

		Integer userId = getCurrentUserIdOrNull(); 

		if (userId == null) {
			getOrCreateSessionFavorites(session).add(productId);
			return "redirect:/favorites";
		}

		favoriteService.addFavorite(userId, productId);
		return "redirect:/favorites";
	}

	@PostMapping("/favorites/{productId}/delete")
	public String delete(@PathVariable Integer productId, HttpSession session) {

		Integer userId = getCurrentUserIdOrNull(); 

		if (userId == null) {
			getOrCreateSessionFavorites(session).remove(productId);
			return "redirect:/favorites";
		}

		favoriteService.removeFavorite(userId, productId);
		return "redirect:/favorites";
	}

	private SessionFavorites getOrCreateSessionFavorites(HttpSession session) {

		SessionFavorites sessionFavorites = (SessionFavorites) session.getAttribute(SESSION_FAVORITES_KEY);

		if (sessionFavorites == null) {
			sessionFavorites = new SessionFavorites();
			session.setAttribute(SESSION_FAVORITES_KEY, sessionFavorites);
		}

		return sessionFavorites;
	}

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