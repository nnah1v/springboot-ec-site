package com.natsuka.ec.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public class SessionCart implements Serializable {

	private static final long serialVersionUID = 1L;

	// 修正（Java）：商品ID→数量
	private final Map<Integer, Integer> productIdToQuantityMap = new LinkedHashMap<>();

	// 修正（Java）：追加（数量加算）
	public void add(Integer productId, Integer addQuantity) {
		if (productId == null || addQuantity == null || addQuantity <= 0) {
			return;
		}
		int currentQuantity = productIdToQuantityMap.getOrDefault(productId, 0);
		productIdToQuantityMap.put(productId, currentQuantity + addQuantity);
	}

	// 修正（Java）：更新（0以下なら削除）
	public void update(Integer productId, Integer newQuantity) {
		if (productId == null || newQuantity == null) {
			return;
		}
		if (newQuantity <= 0) {
			productIdToQuantityMap.remove(productId);
			return;
		}
		productIdToQuantityMap.put(productId, newQuantity);
	}

	// 修正（Java）：削除
	public void remove(Integer productId) {
		if (productId == null) {
			return;
		}
		productIdToQuantityMap.remove(productId);
	}

	public boolean isEmpty() {
		return productIdToQuantityMap.isEmpty();
	}

	// 修正（Java）：外部から変更不可で渡す
	public Map<Integer, Integer> asUnmodifiableMap() {
		return Collections.unmodifiableMap(productIdToQuantityMap);
	}
}