package com.natsuka.ec.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set; // 修正（Java）
import java.util.stream.Collectors; // 修正（Java）

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.natsuka.ec.dto.SessionFavorites;
import com.natsuka.ec.entity.Favorite;
import com.natsuka.ec.repository.FavoriteRepository;

@Service
public class FavoriteService {

	private final FavoriteRepository favoriteRepository;

	public FavoriteService(FavoriteRepository favoriteRepository) {
		this.favoriteRepository = favoriteRepository;
	}

	public List<Favorite> findFavorites(Integer userId) {
		return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
	}

	// 修正（Java）：お気に入り商品IDだけをSetで取得
	public Set<Integer> findFavoriteProductIds(Integer userId) {
		return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
				.stream()
				.map(Favorite::getProductId)
				.collect(Collectors.toSet());
	}

	@Transactional
	public void addFavorite(Integer userId, Integer productId) {
		if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
			return;
		}

		Favorite favorite = new Favorite();
		favorite.setUserId(userId);
		favorite.setProductId(productId);
		favorite.setCreatedAt(LocalDateTime.now());

		favoriteRepository.save(favorite);
	}

	@Transactional
	public void removeFavorite(Integer userId, Integer productId) {
		favoriteRepository.findByUserIdAndProductId(userId, productId)
				.ifPresent(favoriteRepository::delete);
	}

	@Transactional
	public void mergeSessionFavorites(Integer userId, SessionFavorites sessionFavorites) {
		if (sessionFavorites == null || sessionFavorites.isEmpty()) {
			return;
		}

		for (Integer productId : sessionFavorites.getProductIdSet()) {
			addFavorite(userId, productId);
		}
	}
}