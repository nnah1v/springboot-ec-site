package com.natsuka.ec.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.natsuka.ec.entity.DiscountRule;

public interface DiscountRuleRepository extends JpaRepository<DiscountRule, Integer> {

	DiscountRule findFirstByRuleTypeAndIsActiveTrue(String ruleType);
}