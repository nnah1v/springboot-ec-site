package com.natsuka.ec.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cart_items", uniqueConstraints = {
		@UniqueConstraint(name = "uk_cart_items_user_product", columnNames = { "user_id", "product_id" }) // 修正（Java）
})
@Getter
@Setter
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// 修正（Java）：users.id の型に合わせる
	@Column(name = "user_id", nullable = false)
	private Integer userId;

	@Column(name = "product_id", nullable = false)
	private Integer productId;

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	// 修正（Java）：DBに列が無いなら削除してOK
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		if (createdAt == null) {
			createdAt = now;
		}
		updatedAt = now;
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = LocalDateTime.now();
	}
}