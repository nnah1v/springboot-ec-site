package com.natsuka.ec.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final LoginSuccessHandler loginSuccessHandler; // 修正（Java）

	public SecurityConfig(LoginSuccessHandler loginSuccessHandler) { // 修正（Java）
		this.loginSuccessHandler = loginSuccessHandler; // 修正（Java）
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

		httpSecurity
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/css/**", "/images/**", "/js/**").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")

						// 修正（Java）：ゲストでも閲覧・操作できる範囲
						.requestMatchers("/", "/login", "/signup/**", "/products", "/products/**").permitAll()
						.requestMatchers("/cart/**", "/favorites/**").permitAll() // 修正（Java）

						// 修正（Java）：ログイン必須
						.requestMatchers("/mypage/**", "/orders/**").authenticated()
						.anyRequest().authenticated())

				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/login")
						.usernameParameter("email")
						.passwordParameter("password")

						// 修正（Java）：ログイン成功時にマージしてから遷移（defaultSuccessUrlは使わない）
						.successHandler(loginSuccessHandler) // 修正（Java）

						.failureUrl("/login?error")
						.permitAll())

				.logout(logout -> logout
						.logoutSuccessUrl("/products")
						.permitAll())

				// 修正（Java）：セッション固定攻撃対策。属性は移送されるので引き継ぎに相性が良い
				.sessionManagement(session -> session
						.sessionFixation(sessionFixation -> sessionFixation.migrateSession()));

		return httpSecurity.build();
	}
}