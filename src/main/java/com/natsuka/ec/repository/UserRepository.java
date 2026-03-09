package com.natsuka.ec.repository;

import java.util.Optional; // 修正（Java）

import org.springframework.data.jpa.repository.JpaRepository;

import com.natsuka.ec.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	boolean existsByEmail(String email);

	//Optionalに変更
	Optional<User> findByEmail(String email);
}