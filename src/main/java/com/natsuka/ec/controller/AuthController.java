package com.natsuka.ec.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

	// /login にGETアクセスが来たときの処理
	@GetMapping("/login")
	public String login() {

		// auth/login.html を表示
		return "auth/login";
	}

}