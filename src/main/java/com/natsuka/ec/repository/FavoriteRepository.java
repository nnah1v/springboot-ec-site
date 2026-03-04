package com.natsuka.ec.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.natsuka.ec.entity.Favorite;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

	boolean existsByUserIdAndProductId(Integer userId, Integer productId);

	Optional<Favorite> findByUserIdAndProductId(Integer userId, Integer productId);

	List<Favorite> findByUserIdOrderByCreatedAtDesc(Integer userId);
}