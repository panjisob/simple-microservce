package com.microservicebasic.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.microservicebasic.product.model.Persons;

@Repository
public interface PersonRepository extends JpaRepository<Persons, Integer> {

}
