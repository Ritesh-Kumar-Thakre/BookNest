package com.cg.order.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cg.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

	List<Order> findByUserId(Integer userId);

	List<Order> findByUserIdOrderByOrderIdDesc(Integer userId);

	Optional<Order> findFirstByOrderByOrderIdDesc();

	List<Order> findByOrderStatus(String status);

	List<Order> findByOrderDateBetween(LocalDate start, LocalDate end);

}