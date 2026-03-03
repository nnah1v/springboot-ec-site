package com.natsuka.ec.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.natsuka.ec.form.SignupForm;
import com.natsuka.ec.service.UserService;

@Controller
public class AuthController {

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/signup")
	public String signupForm(@ModelAttribute SignupForm signupForm) {
		return "auth/signup";
	}

	@PostMapping("/signup")
	public String signup(@Valid @ModelAttribute SignupForm signupForm,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			return "auth/signup";
		}

		// 修正（Java）：確認用パスワード一致チェック
		if (!signupForm.getPassword().equals(signupForm.getPasswordConfirmation())) {
			bindingResult.rejectValue("passwordConfirmation",
					"passwordConfirmation.mismatch",
					"パスワードが一致しません。");
		}

		// 修正（Java）：メール重複チェック
		if (userService.existsByEmail(signupForm.getEmail())) {
			bindingResult.rejectValue("email",
					"email.duplicate",
					"このメールアドレスは既に登録されています。");
		}

		if (bindingResult.hasErrors()) {
			return "auth/signup";
		}

		userService.register(signupForm);

		// 修正（Java）：完了画面へ
		return "redirect:/signup/complete";
	}

	// 修正（Java）：完了画面（GET）
	@GetMapping("/signup/complete")
	public String signupComplete() {
		return "auth/signup_complete";
	}

	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}
}