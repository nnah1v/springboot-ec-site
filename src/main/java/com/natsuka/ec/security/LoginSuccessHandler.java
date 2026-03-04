package com.natsuka.ec.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.natsuka.ec.dto.SessionCart;
import com.natsuka.ec.dto.SessionFavorites;
import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.UserRepository;
import com.natsuka.ec.service.CartService;
import com.natsuka.ec.service.FavoriteService;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

	private static final String SESSION_CART_KEY = "SESSION_CART"; // 修正（Java）
	private static final String SESSION_FAVORITES_KEY = "SESSION_FAVORITES"; // 修正（Java）

	private final UserRepository userRepository;
	private final CartService cartService;
	private final FavoriteService favoriteService;

	public LoginSuccessHandler(UserRepository userRepository,
			CartService cartService,
			FavoriteService favoriteService) {
		this.userRepository = userRepository;
		this.cartService = cartService;
		this.favoriteService = favoriteService;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		HttpSession session = request.getSession(false);

		// 修正（Java）：username = email
		String email = (authentication != null) ? authentication.getName() : null;

		// 修正（Java）：Integerで取得
		Integer userId = userRepository.findByEmail(email)
				.map(User::getId)
				.orElse(null);

		if (session != null && userId != null) {
			SessionCart sessionCart = (SessionCart) session.getAttribute(SESSION_CART_KEY);

			SessionFavorites sessionFavorites = (SessionFavorites) session.getAttribute(SESSION_FAVORITES_KEY);

			cartService.mergeSessionCartToUser(userId, sessionCart); // 修正（Java）
			favoriteService.mergeSessionFavorites(userId, sessionFavorites); // 修正（Java）

			session.removeAttribute(SESSION_CART_KEY);
			session.removeAttribute(SESSION_FAVORITES_KEY);
		}

		response.sendRedirect("/mypage");
	}
}