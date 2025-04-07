package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrderByEmployee_Email(String employeeEmail);

    List<Order> findOrderByClient_Email(String clientEmail);

    Optional<Order> findByClient_EmailAndOrderDate(String clientEmail, LocalDateTime orderDate);

    Page<Order> findByClient_Email(String clientEmail, Pageable pageable);

    Page<Order> findByClient_EmailContainingIgnoreCase(String clientEmail, Pageable pageable);

    Page<Order> findByEmployee_Email(String employeeEmail, Pageable pageable);

    Page<Order> findByEmployee_EmailContainingIgnoreCase(String employeeEmail, Pageable pageable);
}
