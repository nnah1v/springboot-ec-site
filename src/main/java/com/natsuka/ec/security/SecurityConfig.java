// securityパッケージ（セキュリティ設定を担当する層）
package com.natsuka.ec.security;

// Springの設定クラスであることを示す
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// HttpSecurity（認可/ログイン/ログアウトなどの設定）
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// Webセキュリティを有効化する
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// セキュリティ設定の実体
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// セキュリティルール定義（認可・ログイン・ログアウト）
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

		httpSecurity
				// URLごとのアクセス制御設定
				.authorizeHttpRequests(authorize -> authorize

						// 静的リソースは全員アクセス可能
						.requestMatchers(
								"/css/**",
								"/images/**",
								"/js/**")
						.permitAll()

						// ログイン画面・会員登録・商品閲覧は公開
						.requestMatchers(
								"/",
								"/login",
								"/signup/**",
								"/products",
								"/products/**")
						.permitAll()

						// マイページ・カート・注文はログイン必須（必要に応じて増やす）
						.requestMatchers(
								"/mypage/**",
								"/cart/**",
								"/orders/**")
						.authenticated()

						// それ以外は一旦ログイン必須（開発中はpermitAllにしてもOK）
						.anyRequest().authenticated())

				// フォームログイン設定
				.formLogin(form -> form
						// 自作ログインページ（GET /login → Controller → auth/login.html）
						.loginPage("/login")
						// ログイン処理のPOST先（login.htmlのth:action="@{/login}"と一致）
						.loginProcessingUrl("/login")
						// 成功時の遷移先（直ログインならここへ。直前の保護ページがある場合はそちら優先）
						.defaultSuccessUrl("/mypage/products", false)
						// 失敗時
						.failureUrl("/login?error")
						.permitAll())

				// ログアウト設定
				.logout(logout -> logout
						.logoutSuccessUrl("/products")
						.permitAll())

				// セッション固定攻撃対策：認証時にセッションIDは変えるが属性は引き継ぐ（カート保持に重要）
				.sessionManagement(session -> session
						.sessionFixation(sessionFixation -> sessionFixation.migrateSession()));

		return httpSecurity.build();
	}
}