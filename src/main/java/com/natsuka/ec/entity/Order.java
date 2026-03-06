package com.natsuka.ec.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// 修正（Java）：注文したユーザー
	@Column(name = "user_id", nullable = false)
	private Integer userId;

	// 修正（Java）：注文状態（PENDING / PAID / CANCELLED など）
	@Column(nullable = false)
	private String status;

	// 修正（Java）：割引前合計
	@Column(nullable = false)
	private Integer subtotal;

	// 修正（Java）：割引金額
	@Column(nullable = false)
	private Integer discount;

	// 修正（Java）：最終合計
	@Column(nullable = false)
	private Integer total;

	// 修正（Java）：注文日時
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	// 修正（Java）：決済日時
	@Column(name = "paid_at")
	private LocalDateTime paidAt;
	
	// 修正（Java）：注文に紐づく明細一覧
	@OneToMany(mappedBy = "order")
	private List<OrderItem> orderItems;

}