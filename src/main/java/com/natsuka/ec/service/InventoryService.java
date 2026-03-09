package com.natsuka.ec.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.natsuka.ec.entity.ProductInventoryLog;
import com.natsuka.ec.repository.ProductInventoryLogRepository;

// 在庫管理サービス
// 在庫は products テーブルではなく product_inventory_logs の履歴から計算する方式
// delta_qty を合計して現在在庫を算出する
@Service
public class InventoryService {

	private final ProductInventoryLogRepository productInventoryLogRepository;

	public InventoryService(ProductInventoryLogRepository productInventoryLogRepository) {
		this.productInventoryLogRepository = productInventoryLogRepository;
	}

	// 現在の在庫数を取得
	// product_inventory_logs.delta_qty の合計値を返す
	public int getAvailableStock(Integer productId) {
		if (productId == null) {
			return 0;
		}
		return productInventoryLogRepository.sumDeltaQtyByProductId(productId);
	}

	// 商品登録時の初期在庫をログに記録
	@Transactional
	public void addInitialStock(Integer productId, Integer initialStock) {

		// 不正値チェック
		if (productId == null || initialStock == null || initialStock <= 0) {
			return;
		}

		ProductInventoryLog productInventoryLog = new ProductInventoryLog();
		productInventoryLog.setProductId(productId);

		// 初期在庫なのでプラス
		productInventoryLog.setDeltaQty(initialStock);

		// 理由：商品登録時
		productInventoryLog.setReason("INITIAL_STOCK");

		// 注文IDはないため0
		productInventoryLog.setRelatedOrderId(0);

		productInventoryLog.setCreatedAt(LocalDateTime.now());

		productInventoryLogRepository.save(productInventoryLog);
	}

	// 注文確定時に在庫を減らす
	@Transactional
	public void subtractStock(Integer productId, Integer quantity, Integer relatedOrderId) {

		// 不正値チェック
		if (productId == null || quantity == null || quantity <= 0) {
			return;
		}

		ProductInventoryLog productInventoryLog = new ProductInventoryLog();
		productInventoryLog.setProductId(productId);

		// 在庫減少なのでマイナス値
		productInventoryLog.setDeltaQty(-quantity);

		productInventoryLog.setReason("ORDER");

		productInventoryLog.setRelatedOrderId(relatedOrderId == null ? 0 : relatedOrderId);

		productInventoryLog.setCreatedAt(LocalDateTime.now());

		productInventoryLogRepository.save(productInventoryLog);
	}

	// 管理画面から在庫追加
	@Transactional
	public void addStock(Integer productId, Integer addStock) {

		if (productId == null || addStock == null || addStock <= 0) {
			return;
		}

		ProductInventoryLog productInventoryLog = new ProductInventoryLog();
		productInventoryLog.setProductId(productId);

		// 在庫追加なのでプラス
		productInventoryLog.setDeltaQty(addStock);

		productInventoryLog.setReason("ADMIN_ADD_STOCK");

		productInventoryLog.setRelatedOrderId(0);

		productInventoryLog.setCreatedAt(LocalDateTime.now());

		productInventoryLogRepository.save(productInventoryLog);
	}
}