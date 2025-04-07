package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.BookService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final BookService bookService;
    private final ClientService clientService;

    @Autowired
    public OrderController(OrderService orderService, BookService bookService, ClientService clientService) {
        this.orderService = orderService;
        this.bookService = bookService;
        this.clientService = clientService;
    }

    @GetMapping
    public String getOrders(
            @RequestParam(name = "searchField", required = false) String searchField,
            @RequestParam(name = "searchValue", required = false) String searchValue,
            @RequestParam(name = "sortField", defaultValue = "price") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Model model,
            Authentication authentication) {
        log.info("Fetching orders for user: {}, searchField={}, searchValue={}",
                authentication.getName(), searchField, searchValue);
        boolean isClient = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
        Page<OrderDTO> pageResult;
        if (isClient) {
            if (searchValue != null && !searchValue.isBlank()) {
                pageResult = orderService.searchOrders("employeeEmail", searchValue, page, size, sortField, sortDir);
                List<OrderDTO> filteredOrders = pageResult.getContent().stream()
                        .filter(order -> order.getClientEmail().equalsIgnoreCase(authentication.getName()))
                        .collect(Collectors.toList());
                pageResult = new PageImpl<>(filteredOrders, pageResult.getPageable(), filteredOrders.size());
            } else {
                pageResult = orderService.getOrdersByClient(authentication.getName(), page, size, sortField, sortDir);
            }
            searchField = "employeeEmail";
        } else {
            pageResult = orderService.searchOrders(searchField, searchValue, page, size, sortField, sortDir);
        }
        model.addAttribute("page", pageResult);
        model.addAttribute("orders", pageResult.getContent());
        model.addAttribute("searchField", searchField);
        model.addAttribute("searchValue", searchValue);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("size", size);
        model.addAttribute("currentPage", page);
        return "orders";
    }

    @GetMapping("/new")
    public String showAddOrderForm(Model model) {
        log.info("Displaying new order form");
        OrderDTO newOrder = new OrderDTO();
        newOrder.setBookItems(List.of(new BookItemDTO(), new BookItemDTO(), new BookItemDTO()));
        model.addAttribute("order", newOrder);
        model.addAttribute("books", bookService.getAllBooks());
        model.addAttribute("clients", clientService.getAllClients());
        return "order_form";
    }

    @PostMapping("/add")
    public String addOrder(@ModelAttribute("order") OrderDTO orderDTO,
                           @SessionAttribute(name = "cart", required = false) com.epam.rd.autocode.spring.project.model.Cart cart,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        log.info("Adding order for user: {}", authentication.getName());
        if ((orderDTO.getBookItems() == null || orderDTO.getBookItems().isEmpty()
                || orderDTO.getBookItems().get(0).getBookName() == null)
                && cart != null && !cart.getItems().isEmpty()) {
            List<BookItemDTO> items = new ArrayList<>();
            cart.getItems().forEach((bookItem, quantity) -> {
                items.add(new BookItemDTO(bookItem.getBook().getName(), quantity));
            });
            orderDTO.setBookItems(items);
            orderDTO.setOrderDate(LocalDateTime.now());
            orderDTO.setClientEmail(authentication.getName());
            BigDecimal total = BigDecimal.ZERO;
            orderDTO.setPrice(total);

        }
        try {
            OrderDTO createdOrder = orderService.addOrder(orderDTO);
            if (cart != null) {
                cart.clearCart();
            }
            return "redirect:/orders";
        } catch (InsufficientFundsException e) {
            log.warn("Insufficient funds for user {}: {}", authentication.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "client.not.enough.money");
            if (cart != null) {
                redirectAttributes.addFlashAttribute("items", cart.getItems());
            }
            return "redirect:/cart";
        }
    }

    @GetMapping("/edit")
    public String showEditOrderForm(@RequestParam("clientEmail") String clientEmail,
                                    @RequestParam("orderDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime orderDate,
                                    Model model,
                                    Authentication authentication) {
        log.info("Attempting to edit order for client: {} at date: {}", clientEmail, orderDate);

        try {
            OrderDTO order = orderService.getOrder(clientEmail, orderDate);
            if (order == null) {
                return "redirect:/orders";
            }
            boolean isClient = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
            if (isClient && order.getEmployeeEmail() != null) {
                log.warn("Client {} attempted to edit a confirmed order. Access denied.", authentication.getName());
                return "redirect:/orders";
            }
            model.addAttribute("order", order);
            model.addAttribute("books", bookService.getAllBooks());
            return "order_form";
        } catch (NotFoundException e) {
            log.warn("Order was not found for client: {} at date: {}", authentication.getName(), orderDate);
            model.addAttribute("errorMessage", "client.was.not.found");
            return "redirect:/orders";
        }
    }

    @PostMapping("/edit")
    public String updateOrder(@RequestParam("clientEmail") String originalClientEmail,
                              @RequestParam("orderDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime originalOrderDate,
                              @ModelAttribute("order") OrderDTO orderDTO,
                              Authentication authentication,
                              Model model) {
        log.info("Updating order for client: {} at date: {}", originalClientEmail, originalOrderDate);
        boolean isClient = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
        try {
            OrderDTO existingOrder = orderService.getOrder(originalClientEmail, originalOrderDate);
            if (isClient && existingOrder.getEmployeeEmail() != null) {
                log.warn("Client {} attempted to update a confirmed order. No changes applied.", authentication.getName());
                return "redirect:/orders";
            }
        } catch (NotFoundException e) {
            log.warn("Order was not found for client: {} at date: {}", authentication.getName(), originalOrderDate);
        }
        try {
            orderService.updateOrder(originalClientEmail, originalOrderDate, orderDTO);
            return "redirect:/orders";
        } catch (NotFoundException e) {
            log.warn("Order not found during update: {}", e.getMessage());
            model.addAttribute("errorMessage", "order.not.found");
            model.addAttribute("order", orderDTO);
            model.addAttribute("books", bookService.getAllBooks());
            return "order_form";
        } catch (InsufficientFundsException e) {
            log.warn("InsufficientFundsException: {}", e.getMessage());
            model.addAttribute("errorMessage", "client.not.enough.money");
            model.addAttribute("order", orderDTO);
            model.addAttribute("books", bookService.getAllBooks());
            return "order_form";
        } catch (IllegalArgumentException e) {
            log.warn("IllegalArgumentException: {}", e.getMessage());
            model.addAttribute("errorMessage", "order.invalid");
            model.addAttribute("order", orderDTO);
            model.addAttribute("books", bookService.getAllBooks());
            return "order_form";
        }
    }

    @GetMapping("/delete")
    public String deleteOrder(@RequestParam("clientEmail") String clientEmail,
                              @RequestParam("orderDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime orderDate,
                              Authentication authentication,
                              Model model) {
        log.info("Attempting to delete order for client: {} at date: {}", clientEmail, orderDate);
        try {
            OrderDTO order = orderService.getOrder(clientEmail, orderDate);
            boolean isClient = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));
            if (isClient && order.getEmployeeEmail() != null) {
                log.warn("Client {} attempted to delete a confirmed order. Access denied.", authentication.getName());
                model.addAttribute("errorMessage", "order.cannot.delete.confirmed");
                return "redirect:/orders";
            }
            orderService.deleteOrder(clientEmail, orderDate);
        } catch (NotFoundException e) {
            log.warn("Order not found during delete: {}", e.getMessage());
            model.addAttribute("errorMessage", "order.not.found");
        }
        return "redirect:/orders";
    }

    @PostMapping("/confirm")
    public String confirmOrder(@RequestParam("clientEmail") String clientEmail,
                               @RequestParam("orderDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime orderDate,
                               Authentication authentication,
                               Model model) {
        log.info("Confirming order for client: {} by employee: {}", clientEmail, authentication.getName());
        String workerEmail = authentication.getName();
        try {
            orderService.confirmOrder(clientEmail, orderDate, workerEmail);
            return "redirect:/orders";
        } catch (Exception e) {
            log.warn("Error confirming order: {}", e.getMessage());
            model.addAttribute("errorMessage", "order.confirm.error");
            return "redirect:/orders";
        }
    }
}
