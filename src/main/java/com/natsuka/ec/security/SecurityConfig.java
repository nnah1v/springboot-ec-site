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
						.requestMatchers("/", "/login", "/signup/**", "/products", "/products/**").permitAll()
						.requestMatchers("/mypage/**", "/cart/**", "/orders/**").authenticated()
						.anyRequest().authenticated())

				.formLogin(form -> form
						.loginPage("/login")
						.loginProcessingUrl("/login")
						.usernameParameter("email") // 修正（Java）
						.passwordParameter("password") // 修正（Java）
						.defaultSuccessUrl("/mypage", true) // 修正（Java）
						.failureUrl("/login?error")
						.permitAll())

				.logout(logout -> logout
						.logoutSuccessUrl("/products")
						.permitAll())

				.sessionManagement(session -> session
						.sessionFixation(sessionFixation -> sessionFixation.migrateSession()));

		return httpSecurity.build();
	}
}