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

	private final LoginSuccessHandler loginSuccessHandler; 

	public SecurityConfig(LoginSuccessHandler loginSuccessHandler) {
		this.loginSuccessHandler = loginSuccessHandler; 
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

		httpSecurity
				// Stripe WebhookはCSRF除外
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/stripe/webhook"))

				.authorizeHttpRequests(authorize -> authorize
						// 静的リソースは全員許可
						.requestMatchers("/css/**", "/images/**", "/js/**").permitAll()

						// 公開ページ
						.requestMatchers("/", "/login", "/signup/**", "/products", "/products/**").permitAll()

						// Stripe Webhookは外部POSTなので許可
						.requestMatchers("/stripe/webhook").permitAll()

						// ゲストでも使える範囲
						.requestMatchers("/cart/**", "/favorites/**", "/history/**").permitAll()

						// 管理画面はADMINのみ
						.requestMatchers("/admin/**").hasRole("ADMIN")

						// ログイン必須ページ
						.requestMatchers("/mypage/**", "/orders/**").authenticated()

						// それ以外はログイン必須
						.anyRequest().authenticated())

				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/login")
						.usernameParameter("email")
						.passwordParameter("password")

						// ログイン成功時にマージしてから遷移
						.successHandler(loginSuccessHandler)

						.failureUrl("/login?error")
						.permitAll())

				.logout(logout -> logout
						.logoutSuccessUrl("/products")
						.permitAll())

				// IDは変えるが属性は保持
				.sessionManagement(session -> session
						.sessionFixation(sessionFixation -> sessionFixation.changeSessionId()));

		return httpSecurity.build();
	}
}