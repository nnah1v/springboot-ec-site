package com.natsuka.ec.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.natsuka.ec.entity.Category;
import com.natsuka.ec.repository.CategoryRepository;

//カテゴリのビジネスロジック
@Service
public class CategoryService {
	
	private final CategoryRepository categoryRepository;
	
	//コンストラクタインジェクション
	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}
	
	//カテゴリー覧取得
	public List<Category> findAllCategories(){
		return categoryRepository.findAllByOrderBySortOrderAsc();
		
	}

}
