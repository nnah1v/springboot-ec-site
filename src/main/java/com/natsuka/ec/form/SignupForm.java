package com.natsuka.ec.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class SignupForm {

	@NotBlank(message = "ニックネームを入力してください。")
	@Length(max = 50, message = "ニックネームは50文字以内で入力してください。")
	private String nickname;

	@NotBlank(message = "氏名を入力してください。")
	private String name;

	@NotBlank(message = "フリガナを入力してください。")
	private String furigana;

	@NotBlank(message = "郵便番号を入力してください。")
	@Pattern(regexp = "^\\d{7}$", message = "郵便番号は7桁の数字（ハイフンなし）で入力してください。") // 修正（Java）
	private String postalCode;

	@NotBlank(message = "住所を入力してください。")
	private String address;

	@NotBlank(message = "電話番号を入力してください。")
	@Pattern(regexp = "^(070|080|090)\\d{8}$", message = "携帯電話番号（070/080/090、ハイフンなし）で入力してください。") // 修正（Java）
	private String phoneNumber;

	@NotBlank(message = "メールアドレスを入力してください。")
	@Email(message = "メールアドレスは正しい形式で入力してください。")
	private String email;

	@NotBlank(message = "パスワードを入力してください。")
	@Length(min = 8, message = "パスワードは8文字以上で入力してください。")
	private String password;

	@NotBlank(message = "パスワード（確認用）を入力してください。")
	private String passwordConfirmation;
}