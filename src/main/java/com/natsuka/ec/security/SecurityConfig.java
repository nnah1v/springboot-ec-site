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
				// 修正（Java）：Stripe WebhookはCSRF除外
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/stripe/webhook"))

				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/css/**", "/images/**", "/js/**").permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN")

						// 修正（Java）：Stripe Webhookは外部POSTなので許可
						.requestMatchers("/stripe/webhook").permitAll()

						// 修正（Java）：ゲストでも閲覧・操作できる範囲
						.requestMatchers("/", "/login", "/signup/**", "/products", "/products/**").permitAll()
						.requestMatchers("/cart/**", "/favorites/**").permitAll()

						// 修正（Java）：ログイン必須
						.requestMatchers("/mypage/**", "/orders/**").authenticated()
						.anyRequest().authenticated())

				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/login")
						.usernameParameter("email")
						.passwordParameter("password")

						// 修正（Java）：ログイン成功時にマージしてから遷移
						.successHandler(loginSuccessHandler)

						.failureUrl("/login?error")
						.permitAll())

				.logout(logout -> logout
						.logoutSuccessUrl("/products")
						.permitAll())

				// 修正（Java）：IDは変えるが属性は保持
				.sessionManagement(session -> session
						.sessionFixation(sessionFixation -> sessionFixation.changeSessionId()));

		return httpSecurity.build();
	}
}