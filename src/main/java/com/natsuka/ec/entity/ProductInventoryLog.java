package com.natsuka.ec.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_inventory_logs")
@Getter
@Setter
public class ProductInventoryLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "product_id", nullable = false)
	private Integer productId;

	@Column(name = "delta_qty", nullable = false)
	private Integer deltaQty;

	@Column(name = "reason", nullable = false)
	private String reason;

	@Column(name = "related_order_id", nullable = false)
	private Integer relatedOrderId;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

}
