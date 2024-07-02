package com.microservicebasic.product.controller;

import java.util.List;

import com.microservicebasic.product.annotation.UserAuthenticate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservicebasic.product.model.Persons;
import com.microservicebasic.product.service.PersonService;

@RestController
public class BasicController {

	@Autowired
	private PersonService personService;
	
	@UserAuthenticate
	@GetMapping("/all")
	public List<Persons> getAll() {
		
		return personService.findAll();
	}
	
	
	
}
