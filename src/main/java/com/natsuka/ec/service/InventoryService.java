package com.natsuka.ec.service;

import org.springframework.stereotype.Service;

import com.natsuka.ec.repository.ProductInventoryLogRepository;

@Service
public class InventoryService {

	private final ProductInventoryLogRepository productInventoryLogRepository;

	public InventoryService(ProductInventoryLogRepository productInventoryLogRepository) {
		this.productInventoryLogRepository = productInventoryLogRepository;
	}

	public int getAvailableStock(Integer productId) {
		if (productId == null) {
			return 0;
		}
		return productInventoryLogRepository.sumDeltaQtyByProductId(productId);
	}
}