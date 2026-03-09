package com.natsuka.ec.controller;

import java.util.List;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.natsuka.ec.entity.Category;
import com.natsuka.ec.service.CategoryService;

@ControllerAdvice
public class GlobalControllerAdvice {

	private final CategoryService categoryService;

	public GlobalControllerAdvice(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	// 修正（Java）：全ページ共通でcategoriesをModelに追加
	@ModelAttribute("categories")
	public List<Category> categories() {
		return categoryService.findAllCategories();
	}
}