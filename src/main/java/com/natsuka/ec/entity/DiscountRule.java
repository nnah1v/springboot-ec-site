package com.natsuka.ec.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "discount_rules")
@Getter
@Setter
public class DiscountRule {

	@Id
	private Integer id;

	private String ruleType;

	private Integer minAmount;

	private Integer percentOff;

	private Boolean isActive;
}