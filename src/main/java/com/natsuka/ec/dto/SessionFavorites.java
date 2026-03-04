package com.natsuka.ec.dto;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;

@Getter
public class SessionFavorites implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Set<Integer> productIdSet = new LinkedHashSet<>();

	// 修正（Java）：追加（重複はSetで自動排除）
	public void add(Integer productId) {
		productIdSet.add(productId);
	}

	// 修正（Java）：削除
	public void remove(Integer productId) {
		productIdSet.remove(productId);
	}

	public boolean contains(Integer productId) {
		return productIdSet.contains(productId);
	}

	public boolean isEmpty() {
		return productIdSet.isEmpty();
	}
}