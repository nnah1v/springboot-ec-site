package com.natsuka.ec.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.UserRepository;

@Component
public class CurrentUserResolver {

	private final UserRepository userRepository;

	public CurrentUserResolver(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Integer getCurrentUserIdOrNull() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getPrincipal())) {
			return null;
		}

		String email = authentication.getName(); // usernameParameter("email")でログインしてても、ここはemailになる
		Optional<User> user = userRepository.findByEmail(email);

		if (user.isEmpty()) {
			return null;
		}

		return user.get().getId();
	}
}