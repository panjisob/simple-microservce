package com.microservicebasic.product.controller;

import java.util.List;

import com.microservicebasic.product.annotation.UserAuthenticate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservicebasic.product.model.Product;
import com.microservicebasic.product.service.ProductService;

@RestController
public class BasicController {

	@Autowired
	private ProductService productService;
	
	@UserAuthenticate
	@GetMapping("/all")
	public List<Product> getAll() {
		
		return productService.findAll();
	}
	
	
	
}
