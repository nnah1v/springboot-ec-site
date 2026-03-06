package com.natsuka.ec.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.natsuka.ec.dto.SessionHistory;
import com.natsuka.ec.entity.Product;
import com.natsuka.ec.service.ProductService;

@Controller
public class HistoryController {

	private final ProductService productService;

	public HistoryController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("/history")
	public String history(
			@SessionAttribute(name = "SESSION_HISTORY", required = false) SessionHistory sessionHistory,
			@PageableDefault(size = 20) Pageable pageable, 
			Model model) {

		Page<Product> productPage = productService.findHistoryProducts(
				sessionHistory == null ? null : sessionHistory.getProductIdSet(),
				pageable);

		model.addAttribute("productPage", productPage);
		model.addAttribute("isHistoryEmpty", productPage.isEmpty()); 

		return "history/index";
	}
}