package com.microservicebasic.product.controller;

import com.microservicebasic.product.annotation.UserAuthenticate;
import com.microservicebasic.product.model.Product;
import com.microservicebasic.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BasicController {

	private final ProductService productService;
	
	@UserAuthenticate
	@GetMapping("/all")
	public List<Product> getAll() {
		
		return productService.findAll();
	}
	
	
	
}
