package com.natsuka.ec.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.natsuka.ec.entity.Category;
import com.natsuka.ec.service.CategoryService;

@Controller
@RequestMapping("/categories")
public class CategoryController {
	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public String index(Model model) {

		List<Category> categories = categoryService.findAllCategories();

		model.addAttribute("categories", categories);

		return "categories/index";
	}

}
