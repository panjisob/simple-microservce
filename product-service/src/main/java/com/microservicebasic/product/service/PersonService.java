package com.microservicebasic.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microservicebasic.product.model.Persons;
import com.microservicebasic.product.repository.PersonRepository;

@Service
public class PersonService {

	
	@Autowired
	private PersonRepository personRepository;
	
	
	public List<Persons> findAll(){
		return personRepository.findAll();
	}
	
	
}
