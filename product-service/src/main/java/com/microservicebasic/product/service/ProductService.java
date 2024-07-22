package com.microservicebasic.product.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microservicebasic.product.model.Product;
import com.microservicebasic.product.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {


	private final ProductRepository productRepository;
	
	
	public List<Product> findAll(){
		return productRepository.findAll();
	}
	
	
}
