package com.cg.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cg.order.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Integer> {

	List<Address> findByCustomerId(Integer customerId);

	List<Address> findByCity(String city);

	void deleteByCustomerId(Integer customerId);

}