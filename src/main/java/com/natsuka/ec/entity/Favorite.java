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
@Table(name = "favorites")
@Getter
@Setter
public class Favorite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "user_id", nullable = false)
	private Integer userId;

	@Column(name = "product_id", nullable = false)
	private Integer productId;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
}