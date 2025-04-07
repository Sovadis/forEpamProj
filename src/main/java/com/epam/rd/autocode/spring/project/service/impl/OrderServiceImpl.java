package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.OrderDTOMapper;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDTOMapper orderDTOMapper;
    private final BookRepository bookRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderDTOMapper orderDTOMapper,
                            BookRepository bookRepository, ClientRepository clientRepository,
                            EmployeeRepository employeeRepository) {
        this.orderRepository = orderRepository;
        this.orderDTOMapper = orderDTOMapper;
        this.bookRepository = bookRepository;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        log.info("Retrieving all orders");
        return orderRepository.findAll().stream()
                .map(orderDTOMapper::convertOrderToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByClient(String clientEmail) {
        log.info("Retrieving orders for client: {}", clientEmail);
        return orderRepository.findOrderByClient_Email(clientEmail).stream()
                .map(orderDTOMapper::convertOrderToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByEmployee(String employeeEmail) {
        log.info("Retrieving orders for employee: {}", employeeEmail);
        return orderRepository.findOrderByEmployee_Email(employeeEmail).stream()
                .map(orderDTOMapper::convertOrderToOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrder(String clientEmail, LocalDateTime orderDate) {
        log.info("Retrieving order for client: {} at date: {}", clientEmail, orderDate);
        Optional<Order> optionalOrder = orderRepository.findByClient_EmailAndOrderDate(clientEmail, orderDate);
        if (optionalOrder.isEmpty()) {
            log.warn("Order not found for client: {} at date: {}", clientEmail, orderDate);
            throw new NotFoundException("Order was not found");
        }

        return orderDTOMapper.convertOrderToOrderDTO(optionalOrder.get());
    }

    @Override
    @Transactional
    public OrderDTO addOrder(OrderDTO orderDTO) {
        log.info("Adding order for client: {}", orderDTO.getClientEmail());
        Client client = null;
        if (orderDTO.getClientEmail() != null && !orderDTO.getClientEmail().isBlank()) {
            Optional<Client> clientOpt = clientRepository.findByEmail(orderDTO.getClientEmail());
            if (clientOpt.isEmpty()) {
                log.warn("Client with email {} not found", orderDTO.getClientEmail());
                throw new NotFoundException("Client was not found");
            }
            client = clientOpt.get();
        }

        Employee employee = null;
        if (orderDTO.getEmployeeEmail() != null && !orderDTO.getEmployeeEmail().isBlank()) {
            Optional<Employee> empOpt = employeeRepository.findByEmail(orderDTO.getEmployeeEmail());
            if (empOpt.isEmpty()) {
                log.warn("Employee with email {} not found", orderDTO.getEmployeeEmail());
                throw new NotFoundException("Employee was not found");
            }
            employee = empOpt.get();
        }
        Order order = new Order();
        order.setClient(client);
        order.setEmployee(employee);
        order.setOrderDate(orderDTO.getOrderDate() != null ? orderDTO.getOrderDate() : LocalDateTime.now());

        List<BookItem> items = new ArrayList<>();
        if (orderDTO.getBookItems() != null) {
            for (BookItemDTO itemDTO : orderDTO.getBookItems()) {
                if (itemDTO.getBookName() == null || itemDTO.getBookName().isBlank()) {
                    continue;
                }
                Optional<Book> bookOpt = bookRepository.findByName(itemDTO.getBookName());
                if (bookOpt.isEmpty()) {
                    log.warn("Book {} not found", itemDTO.getBookName());
                    throw new NotFoundException("Book was not found");
                }
                Book book = bookOpt.get();
                int quantity = (itemDTO.getQuantity() != null ? itemDTO.getQuantity() : 0);
                if (quantity < 1) {
                    log.warn("Invalid quantity {} for book {}", quantity, itemDTO.getBookName());
                    throw new IllegalArgumentException("Invalid quantity for book");
                }
                BookItem bookItem = new BookItem();
                bookItem.setBook(book);
                bookItem.setQuantity(quantity);
                bookItem.setOrder(order);
                items.add(bookItem);
            }
        }
        if (items.isEmpty()) {
            log.warn("No valid book items in order");
            throw new IllegalArgumentException("No valid book items in order");
        }
        order.setBookItems(items);
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (BookItem bi : items) {
            totalPrice = totalPrice.add(bi.getBook().getPrice().multiply(BigDecimal.valueOf(bi.getQuantity())));
        }
        order.setPrice(totalPrice);
        if (client != null) {
            if (client.getBalance() == null) {
                client.setBalance(BigDecimal.ZERO);
            }
            if (client.getBalance().compareTo(totalPrice) < 0) {
                log.warn("Insufficient funds for client {}: balance={}, required={}", client.getEmail(), client.getBalance(), totalPrice);
                throw new InsufficientFundsException("Insufficient funds");
            }
            BigDecimal newBalance = client.getBalance().subtract(totalPrice);
            client.setBalance(newBalance);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order added successfully for client: {}", order.getClient().getEmail());
        return orderDTOMapper.convertOrderToOrderDTO(savedOrder);
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(String originalClientEmail, LocalDateTime originalOrderDate, OrderDTO orderDTO) {
        log.info("Updating order for client: {} at date: {}", originalClientEmail, originalOrderDate);
        Optional<Order> optOrder = orderRepository.findByClient_EmailAndOrderDate(originalClientEmail, originalOrderDate);
        if (optOrder.isEmpty()) {
            log.warn("Order not found for client: {} at date: {}", originalClientEmail, originalOrderDate);
            throw new NotFoundException("Order was not found at this date");
        }
        Order order = optOrder.get();
        Client originalClient = order.getClient();
        BigDecimal oldPrice = order.getPrice();

        if (orderDTO.getClientEmail() != null && !orderDTO.getClientEmail().isBlank()) {
            Optional<Client> clientOpt = clientRepository.findByEmail(orderDTO.getClientEmail());
            if (clientOpt.isEmpty()) {
                log.warn("Client with email {} not found", orderDTO.getClientEmail());
                throw new NotFoundException("Client with this email was not found");
            }
            order.setClient(clientOpt.get());
        }
        if (orderDTO.getEmployeeEmail() != null) {
            if (orderDTO.getEmployeeEmail().isBlank()) {
                order.setEmployee(null);
            } else {
                Optional<Employee> empOpt = employeeRepository.findByEmail(orderDTO.getEmployeeEmail());
                if (empOpt.isEmpty()) {
                    log.warn("Employee with email {} not found", orderDTO.getEmployeeEmail());
                    throw new NotFoundException("Employee with this email was not found");
                }
                order.setEmployee(empOpt.get());
            }
        }
        if (orderDTO.getOrderDate() != null) {
            order.setOrderDate(orderDTO.getOrderDate().withNano(0));
        }

        List<BookItem> newItems = new ArrayList<>();
        if (orderDTO.getBookItems() != null) {
            for (BookItemDTO itemDTO : orderDTO.getBookItems()) {
                if (itemDTO.getBookName() == null || itemDTO.getBookName().isBlank()) {
                    continue;
                }
                Optional<Book> bookOpt = bookRepository.findByName(itemDTO.getBookName());
                if (bookOpt.isEmpty()) {
                    log.warn("Book {} not found", itemDTO.getBookName());
                    throw new NotFoundException("Book was not found");
                }
                Book book = bookOpt.get();
                int quantity = (itemDTO.getQuantity() != null ? itemDTO.getQuantity() : 0);
                if (quantity < 1) {
                    log.warn("Invalid quantity {} for book {}", quantity, itemDTO.getBookName());
                    throw new IllegalArgumentException("Invalid quantity for book");
                }
                BookItem bookItem = new BookItem();
                bookItem.setBook(book);
                bookItem.setQuantity(quantity);
                bookItem.setOrder(order);
                newItems.add(bookItem);
            }
        }
        if (newItems.isEmpty()) {
            log.warn("No valid book items provided for update");
            throw new NotFoundException("No valid book items provided for update");
        }

        order.getBookItems().clear();
        order.getBookItems().addAll(newItems);
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (BookItem bi : newItems) {
            totalPrice = totalPrice.add(
                    bi.getBook().getPrice().multiply(BigDecimal.valueOf(bi.getQuantity()))
            );
        }
        order.setPrice(totalPrice);

        Client client = order.getClient();
        if (client != null) {
            if (client.getBalance() == null) {
                client.setBalance(BigDecimal.ZERO);
            }
            if (!client.equals(originalClient)) {
                if (originalClient != null) {
                    if (originalClient.getBalance() == null) {
                        originalClient.setBalance(BigDecimal.ZERO);
                    }
                    originalClient.setBalance(originalClient.getBalance().add(oldPrice));
                }
                if (client.getBalance().compareTo(totalPrice) < 0) {
                    log.warn("Insufficient funds for new client {}: balance={}, required={}",
                            client.getEmail(), client.getBalance(), totalPrice);
                    throw new InsufficientFundsException("Insufficient funds");
                }
                client.setBalance(client.getBalance().subtract(totalPrice));
            } else {
                if (totalPrice.compareTo(oldPrice) > 0) {
                    BigDecimal additionalAmount = totalPrice.subtract(oldPrice);
                    if (client.getBalance().compareTo(additionalAmount) < 0) {
                        log.warn("Insufficient funds for client {}: balance={}, additional required={}",
                                client.getEmail(), client.getBalance(), additionalAmount);
                        throw new InsufficientFundsException("Insufficient funds");
                    }
                    client.setBalance(client.getBalance().subtract(additionalAmount));
                } else if (totalPrice.compareTo(oldPrice) < 0) {
                    BigDecimal refundAmount = oldPrice.subtract(totalPrice);
                    client.setBalance(client.getBalance().add(refundAmount));
                }
            }
        }

        Order saved = orderRepository.save(order);
        log.info("Order updated successfully for client: {}", order.getClient().getEmail());
        return orderDTOMapper.convertOrderToOrderDTO(saved);
    }

    @Override
    @Transactional
    public void deleteOrder(String clientEmail, LocalDateTime orderDate) {
        log.info("Deleting order for client: {} at date: {}", clientEmail, orderDate);
        Optional<Order> orderOpt = orderRepository.findByClient_EmailAndOrderDate(clientEmail, orderDate);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (order.getEmployee() == null) {
                Client client = order.getClient();
                if (client != null) {
                    if (client.getBalance() == null) {
                        client.setBalance(BigDecimal.ZERO);
                    }
                    client.setBalance(client.getBalance().add(order.getPrice()));
                    log.info("Refunded {} to client {} due to order deletion", order.getPrice(), client.getEmail());
                }
            }
            orderRepository.delete(order);
            log.info("Order deleted successfully");
        } else {
            log.warn("Order for client: {} at date: {} not found", clientEmail, orderDate);
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @Transactional
    public OrderDTO confirmOrder(String clientEmail, LocalDateTime orderDate, String workerEmail) {
        log.info("Confirming order for client: {} at date: {} by worker: {}", clientEmail, orderDate, workerEmail);
        Order order = orderRepository.findByClient_EmailAndOrderDate(clientEmail, orderDate)
                .orElseThrow(() -> {
                    log.error("Order not found for client: {} at date: {}", clientEmail, orderDate);
                    return new IllegalArgumentException("Order is not found");
                });

        Employee employee = employeeRepository.findByEmail(workerEmail)
                .orElseThrow(() -> {
                    log.error("Employee not found with email: {}", workerEmail);
                    return new IllegalArgumentException("Employee is not found");
                });

        order.setEmployee(employee);
        Order updated = orderRepository.save(order);
        OrderDTO orderDTO = orderDTOMapper.convertOrderToOrderDTO(updated);

        orderDTO.setIsConfirmed(true);
        orderDTO.setEmployeeEmail(employee.getEmail());
        log.info("Order confirmed for client: {}", clientEmail);
        return orderDTO;
    }

    @Override
    public Page<OrderDTO> searchOrders(String searchField,
                                       String searchValue,
                                       int page,
                                       int size,
                                       String sortField,
                                       String sortDir) {
        log.info("Searching orders: field={}, value={}", searchField, searchValue);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.toUpperCase()), sortField);
        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);

        Page<Order> orderPage;

        if (searchValue == null || searchValue.isBlank()) {
            orderPage = orderRepository.findAll(pageRequest);
        } else if ("clientEmail".equalsIgnoreCase(searchField)) {
            orderPage = orderRepository.findByClient_EmailContainingIgnoreCase(searchValue, pageRequest);
        } else if ("employeeEmail".equalsIgnoreCase(searchField)) {
            orderPage = orderRepository.findByEmployee_EmailContainingIgnoreCase(searchValue, pageRequest);
        } else {
            log.warn("Unknown searchField={}, returning all orders", searchField);
            orderPage = orderRepository.findAll(pageRequest);
        }

        return orderPage.map(orderDTOMapper::convertOrderToOrderDTO);
    }

    @Override
    public Page<OrderDTO> getOrdersByClient(String clientEmail,
                                            int page,
                                            int size,
                                            String sortField,
                                            String sortDir) {
        log.info("Fetching paginated orders for clientEmail={}, page={}, size={}", clientEmail, page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.toUpperCase()), sortField);
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Order> orderPage = orderRepository.findByClient_Email(clientEmail, pageable);
        return orderPage.map(orderDTOMapper::convertOrderToOrderDTO);
    }

}
