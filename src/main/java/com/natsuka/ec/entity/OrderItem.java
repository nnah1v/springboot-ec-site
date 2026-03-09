package com.natsuka.ec.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// 注文
	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	// 商品
	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	// 数量
	private Integer quantity;

	// 購入時の単価
	private Integer unitPrice;

	// 行合計
	private Integer lineTotal;
}