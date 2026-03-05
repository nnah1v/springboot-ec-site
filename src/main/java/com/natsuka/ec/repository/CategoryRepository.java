package com.natsuka.ec.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.natsuka.ec.entity.Category;

/*
 * categoriesテーブルを操作するRepository
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {

	// sort_order順にカテゴリ取得
	List<Category> findAllByOrderBySortOrderAsc();
}