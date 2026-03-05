package com.natsuka.ec.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.natsuka.ec.entity.User;
import com.natsuka.ec.form.SignupForm;
import com.natsuka.ec.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	// ：Controllerの重複チェック用に公開
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Transactional
	public void register(SignupForm signupForm) {

		User user = new User();

		user.setName(signupForm.getName());
		user.setFurigana(signupForm.getFurigana());
		user.setNickname(signupForm.getNickname()); 
		user.setPostalCode(signupForm.getPostalCode());
		user.setAddress(signupForm.getAddress());
		user.setPhoneNumber(signupForm.getPhoneNumber());
		user.setEmail(signupForm.getEmail());
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));

		// ：一般ユーザー固定
		user.setRoleId(1); // ROLE_USER
		user.setEnabled(true);

		userRepository.save(user);
	}
}