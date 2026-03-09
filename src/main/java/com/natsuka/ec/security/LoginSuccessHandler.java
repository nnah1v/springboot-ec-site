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

	private static final String SESSION_CART_KEY = "SESSION_CART"; 
	private static final String SESSION_FAVORITES_KEY = "SESSION_FAVORITES"; 

	private final UserRepository userRepository;
	private final CartService cartService;
	private final FavoriteService favoriteService;

	public LoginSuccessHandler(
			UserRepository userRepository,
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

		//authentication/emailが取れないなら通常遷移
		if (authentication == null || authentication.getName() == null) {
			response.sendRedirect("/mypage");
			return;
		}

		String email = authentication.getName();

		Integer userId = userRepository.findByEmail(email)
				.map(User::getId)
				.orElse(null);

		if (userId == null) {
			response.sendRedirect("/mypage");
			return;
		}

		//migrateSession後の新セッションを確実に掴む
		HttpSession session = request.getSession();

		//型安全に取得（ClassCastException回避）
		Object cartObj = session.getAttribute(SESSION_CART_KEY);
		SessionCart sessionCart = (cartObj instanceof SessionCart) ? (SessionCart) cartObj : null;

		Object favObj = session.getAttribute(SESSION_FAVORITES_KEY);
		SessionFavorites sessionFavorites = (favObj instanceof SessionFavorites) ? (SessionFavorites) favObj : null;

		//存在する時だけマージ
		if (sessionCart != null) {
			cartService.mergeSessionCartToUser(userId, sessionCart);
			session.removeAttribute(SESSION_CART_KEY); //二重加算防止
		}

		if (sessionFavorites != null) {
			favoriteService.mergeSessionFavorites(userId, sessionFavorites);
			session.removeAttribute(SESSION_FAVORITES_KEY); //二重登録防止
		}

		response.sendRedirect("/mypage");

		//デバッグ（確認できたら消してOK）
		System.out.println("LOGIN success sessionId = " + session.getId());
		System.out.println("LOGIN success SESSION_CART attr = " + session.getAttribute("SESSION_CART"));
		System.out.println("LOGIN success SESSION_FAVORITES attr = " + session.getAttribute("SESSION_FAVORITES"));
	}
}