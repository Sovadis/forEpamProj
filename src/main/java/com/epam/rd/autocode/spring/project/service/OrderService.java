package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    List<OrderDTO> getOrdersByClient(String clientEmail);

    List<OrderDTO> getOrdersByEmployee(String employeeEmail);

    OrderDTO addOrder(OrderDTO order);

    List<OrderDTO> getAllOrders();

    OrderDTO getOrder(String clientEmail, LocalDateTime orderDate);

    OrderDTO updateOrder(String originalClientEmail, LocalDateTime originalOrderDate, OrderDTO order);

    void deleteOrder(String clientEmail, LocalDateTime orderDate);

    OrderDTO confirmOrder(String clientEmail, LocalDateTime orderDate, String workerEmail);

    Page<OrderDTO> searchOrders(String searchField, String searchValue, int page, int size, String sortField, String sortDir);

    Page<OrderDTO> getOrdersByClient(String clientEmail, int page, int size, String sortField, String sortDir);
}
