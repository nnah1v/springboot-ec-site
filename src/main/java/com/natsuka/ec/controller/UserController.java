package com.natsuka.ec.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.natsuka.ec.entity.User;
import com.natsuka.ec.repository.UserRepository;

// 会員情報画面を担当するController
@Controller
@RequestMapping("/mypage")
public class UserController {

	private final UserRepository userRepository;

	// コンストラクタインジェクション
	public UserController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	// 会員情報表示
	@GetMapping("/profile")
	public String profile(Authentication authentication, Model model) {

		// ログイン中ユーザーのメールアドレスを取得
		String email = authentication.getName();

		// メールアドレスでusersテーブルから会員情報を取得
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。再ログインしてください。"));

		// 画面に会員情報を渡す
		model.addAttribute("user", user);

		return "mypage/profile";
	}

	// 会員情報編集画面
	@GetMapping("/profile/edit")
	public String edit(Authentication authentication, Model model) {

		// ログイン中ユーザーのメールアドレスを取得
		String email = authentication.getName();

		// DBから最新の会員情報を取得
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。再ログインしてください。"));

		// 編集画面へ会員情報を渡す
		model.addAttribute("user", user);

		return "mypage/edit";
	}

	// 会員情報更新処理
	@PostMapping("/profile/update")
	public String update(
			Authentication authentication,
			@RequestParam("nickname") String nickname,
			@RequestParam("name") String name,
			@RequestParam("furigana") String furigana,
			@RequestParam("postalCode") String postalCode,
			@RequestParam("address") String address,
			@RequestParam("phoneNumber") String phoneNumber,
			@RequestParam("email") String email,
			RedirectAttributes redirectAttributes) {

		// ログイン中ユーザーの現在メールアドレスを取得
		String loginUserEmail = authentication.getName();

		// 更新対象は必ずログイン中ユーザー本人
		User user = userRepository.findByEmail(loginUserEmail)
				.orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません。"));

		// 自分以外で同じメールアドレスが使われていたら更新不可
		if (userRepository.existsByEmail(email) && !email.equals(user.getEmail())) {
			redirectAttributes.addFlashAttribute("errorMessage", "そのメールアドレスは既に使用されています。");
			return "redirect:/mypage/profile/edit";
		}

		// 更新してよい項目だけ上書き
		user.setNickname(nickname);
		user.setName(name);
		user.setFurigana(furigana);
		user.setPostalCode(postalCode);
		user.setAddress(address);
		user.setPhoneNumber(phoneNumber);
		user.setEmail(email);

		// 保存
		userRepository.save(user);

		// メールアドレスを変更した場合は認証情報が古くなるので再ログインさせる
		if (!loginUserEmail.equals(email)) {
			SecurityContextHolder.clearContext();
			redirectAttributes.addFlashAttribute("successMessage", "メールアドレスを変更しました。再ログインしてください。");
			return "redirect:/login";
		}

		// 通常更新完了
		redirectAttributes.addFlashAttribute("successMessage", "会員情報を更新しました。");

		return "redirect:/mypage/profile";
	}
}