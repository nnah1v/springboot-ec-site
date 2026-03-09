package com.natsuka.ec.security;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

		// role_id=2 を管理者、それ以外を一般ユーザーとして扱う
		String roleName = (user.getRoleId() != null && user.getRoleId().equals(2)) ? "ROLE_ADMIN" : "ROLE_USER";

		// Spring Security用の権限リストを作成
		List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleName));

		return org.springframework.security.core.userdetails.User
				// ログイン識別子としてメールアドレスを使用
				.withUsername(user.getEmail())
				.password(user.getPassword())
				.authorities(authorities)
				// enabled=false のユーザーはログイン不可
				.disabled(user.getEnabled() != null && !user.getEnabled())
				.build();
	}
}