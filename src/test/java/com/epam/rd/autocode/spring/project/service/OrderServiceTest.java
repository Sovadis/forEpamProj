package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.InsufficientFundsException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.*;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private Client testClient;
    private Employee testEmployee;
    private Book book1;
    private Book book2;

    @BeforeEach
    void setupData() {
        orderRepository.deleteAll();
        clientRepository.deleteAll();
        employeeRepository.deleteAll();
        bookRepository.deleteAll();
        testClient = new Client();
        testClient.setEmail("testclient@example.com");
        testClient.setName("Test Client");
        testClient.setPassword("pwd");
        testClient.setBalance(BigDecimal.valueOf(1000));
        clientRepository.save(testClient);

        testEmployee = new Employee();
        testEmployee.setEmail("worker@example.com");
        testEmployee.setName("Test Employee");
        testEmployee.setPassword("pwd");
        employeeRepository.save(testEmployee);

        book1 = new Book();
        book1.setName("Book One");
        book1.setPrice(BigDecimal.valueOf(100));
        bookRepository.save(book1);
        book2 = new Book();
        book2.setName("Book Two");
        book2.setPrice(BigDecimal.valueOf(50));
        bookRepository.save(book2);
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        Order o1 = new Order();
        o1.setClient(testClient);
        o1.setOrderDate(LocalDateTime.now().minusDays(1));
        o1.setPrice(BigDecimal.valueOf(150));
        orderRepository.save(o1);
        Order o2 = new Order();
        o2.setClient(testClient);
        o2.setOrderDate(LocalDateTime.now());
        o2.setPrice(BigDecimal.valueOf(50));
        orderRepository.save(o2);

        List<OrderDTO> orders = orderService.getAllOrders();

        assertEquals(2, orders.size());
        List<LocalDateTime> dates = orders.stream().map(OrderDTO::getOrderDate).toList();
        assertTrue(dates.contains(o1.getOrderDate()));
        assertTrue(dates.contains(o2.getOrderDate()));
    }

    @Test
    void getOrdersByClient_ShouldReturnOrdersForThatClient() {
        Client otherClient = new Client();
        otherClient.setEmail("other@example.com");
        otherClient.setName("Other");
        otherClient.setPassword("pwd");
        otherClient.setBalance(BigDecimal.valueOf(500));
        clientRepository.save(otherClient);

        Order orderA = new Order();
        orderA.setClient(testClient);
        orderA.setOrderDate(LocalDateTime.now().minusHours(2));
        orderA.setPrice(BigDecimal.valueOf(100));
        orderRepository.save(orderA);

        Order orderB = new Order();
        orderB.setClient(otherClient);
        orderB.setOrderDate(LocalDateTime.now().minusHours(1));
        orderB.setPrice(BigDecimal.valueOf(200));
        orderRepository.save(orderB);

        List<OrderDTO> clientOrders = orderService.getOrdersByClient(testClient.getEmail());

        assertEquals(1, clientOrders.size());
        assertEquals(orderA.getOrderDate(), clientOrders.get(0).getOrderDate());
        assertEquals(testClient.getEmail(), clientOrders.get(0).getClientEmail());
    }

    @Test
    void getOrdersByEmployee_ShouldReturnOrdersForThatEmployee() {
        Employee otherEmp = new Employee();
        otherEmp.setEmail("otheremp@example.com");
        otherEmp.setName("Other Emp");
        otherEmp.setPassword("pwd");
        employeeRepository.save(otherEmp);

        Order order1 = new Order();
        order1.setClient(testClient);
        order1.setEmployee(testEmployee);
        order1.setOrderDate(LocalDateTime.now().minusDays(1));
        order1.setPrice(BigDecimal.valueOf(100));
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setClient(testClient);
        order2.setEmployee(otherEmp);
        order2.setOrderDate(LocalDateTime.now().minusDays(2));
        order2.setPrice(BigDecimal.valueOf(200));
        orderRepository.save(order2);

        List<OrderDTO> empOrders = orderService.getOrdersByEmployee(testEmployee.getEmail());

        assertEquals(1, empOrders.size());
        assertEquals(testEmployee.getEmail(), empOrders.get(0).getEmployeeEmail());
        assertEquals(order1.getOrderDate(), empOrders.get(0).getOrderDate());
    }

    @Test
    void getOrder_WhenExists_ShouldReturnOrderDTO() {
        Order order = new Order();
        order.setClient(testClient);
        order.setOrderDate(LocalDateTime.now().withNano(0));
        order.setPrice(BigDecimal.valueOf(300));
        orderRepository.save(order);

        OrderDTO dto = orderService.getOrder(testClient.getEmail(), order.getOrderDate());

        assertNotNull(dto);
        assertEquals(testClient.getEmail(), dto.getClientEmail());
        assertEquals(order.getOrderDate(), dto.getOrderDate());
        assertEquals(0, BigDecimal.valueOf(300).compareTo(dto.getPrice()));
    }

    @Test
    void getOrder_WhenNotFound_ShouldThrowNotFoundException() {
        LocalDateTime someDate = LocalDateTime.now();
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                orderService.getOrder("nonexistent@example.com", someDate));
        assertEquals("Order was not found", ex.getMessage());
    }

    @Test
    void addOrder_WithValidData_ShouldCreateOrderAndDeductBalance() {
        OrderDTO newOrder = new OrderDTO();
        newOrder.setClientEmail(testClient.getEmail());
        newOrder.setEmployeeEmail(testEmployee.getEmail());
        newOrder.setOrderDate(LocalDateTime.now().withNano(0));
        List<BookItemDTO> items = new ArrayList<>();
        BookItemDTO item1 = new BookItemDTO();
        item1.setBookName(book1.getName());
        item1.setQuantity(2);
        items.add(item1);
        BookItemDTO item2 = new BookItemDTO();
        item2.setBookName(book2.getName());
        item2.setQuantity(5);
        items.add(item2);
        newOrder.setBookItems(items);

        BigDecimal initialBalance = testClient.getBalance();

        OrderDTO savedOrder = orderService.addOrder(newOrder);

        assertEquals(testClient.getEmail(), savedOrder.getClientEmail());
        assertEquals(testEmployee.getEmail(), savedOrder.getEmployeeEmail());
        assertTrue(savedOrder.getBookItems().stream().anyMatch(b -> b.getBookName().equals("Book One")));
        assertTrue(savedOrder.getBookItems().stream().anyMatch(b -> b.getBookName().equals("Book Two")));
        BigDecimal expectedTotal = book1.getPrice().multiply(BigDecimal.valueOf(2))
                .add(book2.getPrice().multiply(BigDecimal.valueOf(5)));
        assertEquals(0, expectedTotal.compareTo(savedOrder.getPrice()), "Total price should match sum of items");

        assertTrue(orderRepository.findByClient_EmailAndOrderDate(
                testClient.getEmail(), savedOrder.getOrderDate()).isPresent());
        Client updatedClient = clientRepository.findByEmail(testClient.getEmail()).orElseThrow();
        BigDecimal expectedBalance = initialBalance.subtract(expectedTotal);
        assertEquals(0, expectedBalance.compareTo(updatedClient.getBalance()), "Client balance should be reduced by order price");
    }

    @Test
    void addOrder_WhenClientNotFound_ShouldThrowNotFoundException() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail("nobody@example.com");
        orderDTO.setOrderDate(LocalDateTime.now());
        BookItemDTO item = new BookItemDTO();
        item.setBookName(book1.getName());
        item.setQuantity(1);
        orderDTO.setBookItems(List.of(item));

        NotFoundException ex = assertThrows(NotFoundException.class, () -> orderService.addOrder(orderDTO));
        assertEquals("Client was not found", ex.getMessage());
        assertEquals(0, orderRepository.count());
    }

    @Test
    void addOrder_WhenEmployeeNotFound_ShouldThrowNotFoundException() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail(testClient.getEmail());
        orderDTO.setEmployeeEmail("wrongemp@example.com");
        orderDTO.setOrderDate(LocalDateTime.now());
        BookItemDTO item = new BookItemDTO();
        item.setBookName(book1.getName());
        item.setQuantity(1);
        orderDTO.setBookItems(List.of(item));

        NotFoundException ex = assertThrows(NotFoundException.class, () -> orderService.addOrder(orderDTO));
        assertEquals("Employee was not found", ex.getMessage());
        assertEquals(0, orderRepository.count());
    }

    @Test
    void addOrder_WhenBookNotFound_ShouldThrowNotFoundException() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail(testClient.getEmail());
        orderDTO.setOrderDate(LocalDateTime.now());
        BookItemDTO item = new BookItemDTO();
        item.setBookName("Nonexistent Book");
        item.setQuantity(1);
        orderDTO.setBookItems(List.of(item));

        NotFoundException ex = assertThrows(NotFoundException.class, () -> orderService.addOrder(orderDTO));
        assertEquals("Book was not found", ex.getMessage());
        assertEquals(0, orderRepository.count());
    }

    @Test
    void addOrder_WhenInvalidQuantity_ShouldThrowIllegalArgumentException() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail(testClient.getEmail());
        orderDTO.setOrderDate(LocalDateTime.now());
        BookItemDTO item = new BookItemDTO();
        item.setBookName(book1.getName());
        item.setQuantity(0);
        orderDTO.setBookItems(List.of(item));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.addOrder(orderDTO));
        assertEquals("Invalid quantity for book", ex.getMessage());
        assertEquals(0, orderRepository.count());
    }

    @Test
    void addOrder_WhenNoItems_ShouldThrowIllegalArgumentException() {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail(testClient.getEmail());
        orderDTO.setOrderDate(LocalDateTime.now());
        orderDTO.setBookItems(new ArrayList<>());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> orderService.addOrder(orderDTO));
        assertEquals("No valid book items in order", ex.getMessage());
        assertEquals(0, orderRepository.count());
    }

    @Test
    void addOrder_WhenInsufficientFunds_ShouldThrowInsufficientFundsExceptionAndRollback() {
        testClient.setBalance(BigDecimal.valueOf(100));
        clientRepository.save(testClient);
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setClientEmail(testClient.getEmail());
        orderDTO.setOrderDate(LocalDateTime.now());
        BookItemDTO item = new BookItemDTO();
        item.setBookName(book1.getName());
        item.setQuantity(2);
        orderDTO.setBookItems(List.of(item));

        BigDecimal balanceBefore = testClient.getBalance();

        InsufficientFundsException ex = assertThrows(InsufficientFundsException.class, () -> orderService.addOrder(orderDTO));
        assertEquals("Insufficient funds", ex.getMessage());
        Client clientAfter = clientRepository.findByEmail(testClient.getEmail()).orElseThrow();
        assertEquals(0, balanceBefore.compareTo(clientAfter.getBalance()), "Client balance should not change on failed order");
        assertEquals(0, orderRepository.count());
    }

    @Test
    void updateOrder_WithValidChanges_ShouldUpdateOrder() {
        Order original = new Order();
        original.setClient(testClient);
        original.setEmployee(null);
        original.setOrderDate(LocalDateTime.now().minusDays(1).withNano(0));

        BookItem origItem = new BookItem();
        origItem.setBook(book1);
        origItem.setQuantity(1);
        origItem.setOrder(original);

        original.setBookItems(new ArrayList<>());
        original.getBookItems().add(origItem);
        original.setPrice(book1.getPrice());
        orderRepository.save(original);

        OrderDTO updateDto = new OrderDTO();
        updateDto.setClientEmail(testClient.getEmail());
        updateDto.setEmployeeEmail(testEmployee.getEmail());
        LocalDateTime newDate = LocalDateTime.now().withNano(0);
        updateDto.setOrderDate(newDate);

        BookItemDTO newItemDto = new BookItemDTO();
        newItemDto.setBookName(book2.getName());
        newItemDto.setQuantity(2);
        updateDto.setBookItems(List.of(newItemDto));

        OrderDTO updatedDto = orderService.updateOrder(testClient.getEmail(), original.getOrderDate(), updateDto);

        assertEquals(testClient.getEmail(), updatedDto.getClientEmail());
        assertEquals(testEmployee.getEmail(), updatedDto.getEmployeeEmail());
        assertEquals(newDate, updatedDto.getOrderDate().withNano(0));

        assertEquals(1, updatedDto.getBookItems().size());
        assertEquals("Book Two", updatedDto.getBookItems().get(0).getBookName());
        assertEquals(2, updatedDto.getBookItems().get(0).getQuantity());

        BigDecimal expectedPrice = book2.getPrice().multiply(BigDecimal.valueOf(2));
        assertEquals(0, expectedPrice.compareTo(updatedDto.getPrice()));

        Order savedOrder = orderRepository.findByClient_EmailAndOrderDate(testClient.getEmail(), newDate)
                .orElseThrow();
        assertEquals(testEmployee.getEmail(), savedOrder.getEmployee().getEmail());
        assertEquals(1, savedOrder.getBookItems().size());
        BookItem savedItem = savedOrder.getBookItems().get(0);
        assertEquals("Book Two", savedItem.getBook().getName());
        assertEquals(2, savedItem.getQuantity());
        assertEquals(0, expectedPrice.compareTo(savedOrder.getPrice()));
    }

    @Test
    void updateOrder_WhenOrderNotFound_ShouldThrowNotFoundException() {
        OrderDTO updateDto = new OrderDTO();
        updateDto.setClientEmail(testClient.getEmail());
        updateDto.setOrderDate(LocalDateTime.now());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                orderService.updateOrder("wrong@example.com", LocalDateTime.now().minusDays(1), updateDto));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void updateOrder_WhenNewClientNotFound_ShouldThrowNotFoundExceptionAndNoChange() {
        Order order = new Order();
        order.setClient(testClient);
        order.setOrderDate(LocalDateTime.now().minusHours(5).withNano(0));
        order.setPrice(BigDecimal.TEN);
        BookItem item = new BookItem();
        item.setBook(book1);
        item.setQuantity(1);
        item.setOrder(order);
        order.setBookItems(List.of(item));
        orderRepository.save(order);

        OrderDTO updateDto = new OrderDTO();
        updateDto.setClientEmail("nobody@nowhere.com");
        BookItemDTO itemDto = new BookItemDTO();
        itemDto.setBookName(book1.getName());
        itemDto.setQuantity(1);
        updateDto.setBookItems(List.of(itemDto));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                orderService.updateOrder(testClient.getEmail(), order.getOrderDate(), updateDto));
        Order unchanged = orderRepository.findByClient_EmailAndOrderDate(testClient.getEmail(), order.getOrderDate()).orElseThrow();
        assertEquals(testClient.getEmail(), unchanged.getClient().getEmail(), "Client should remain unchanged");
        assertEquals(1, unchanged.getBookItems().size(), "Original items should remain");
    }


    @Test
    void updateOrder_WhenNoValidItemsProvided_ShouldThrowIllegalArgumentExceptionAndNoChange() {
        Order order = new Order();
        order.setClient(testClient);
        order.setOrderDate(LocalDateTime.now().minusHours(10).withNano(0));
        BookItem item = new BookItem();
        item.setBook(book1);
        item.setQuantity(2);
        item.setOrder(order);
        order.setBookItems(List.of(item));
        order.setPrice(book1.getPrice().multiply(BigDecimal.valueOf(2)));
        orderRepository.save(order);

        OrderDTO updateDto = new OrderDTO();
        BookItemDTO blankItem = new BookItemDTO();
        blankItem.setBookName("  ");
        updateDto.setBookItems(List.of(blankItem));

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                orderService.updateOrder(testClient.getEmail(), order.getOrderDate(), updateDto));
        assertEquals("No valid book items provided for update", ex.getMessage());
        Order unchanged = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(1, unchanged.getBookItems().size());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void deleteOrder_WithAuthorizedRole_ShouldDeleteOrder() {
        Order order = new Order();
        order.setClient(testClient);
        order.setOrderDate(LocalDateTime.now().minusMinutes(30).withNano(0));
        order.setPrice(BigDecimal.TEN);
        orderRepository.save(order);
        assertTrue(orderRepository.findByClient_EmailAndOrderDate(testClient.getEmail(), order.getOrderDate()).isPresent());

        orderService.deleteOrder(testClient.getEmail(), order.getOrderDate());

        assertFalse(orderRepository.findByClient_EmailAndOrderDate(testClient.getEmail(), order.getOrderDate()).isPresent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void confirmOrder_WithAuthorizedRole_ShouldAssignEmployeeAndMarkConfirmed() {
        Order order = new Order();
        order.setClient(testClient);
        order.setEmployee(null);
        order.setOrderDate(LocalDateTime.now().minusMinutes(10).withNano(0));
        order.setPrice(BigDecimal.valueOf(100));
        orderRepository.save(order);

        OrderDTO confirmedDto = orderService.confirmOrder(testClient.getEmail(), order.getOrderDate(), testEmployee.getEmail());

        assertTrue(confirmedDto.getIsConfirmed(), "OrderDTO should be marked as confirmed");
        assertEquals(testEmployee.getEmail(), confirmedDto.getEmployeeEmail(), "OrderDTO should have employee email set");

        Order updatedOrder = orderRepository.findByClient_EmailAndOrderDate(testClient.getEmail(), order.getOrderDate()).orElseThrow();
        assertNotNull(updatedOrder.getEmployee(), "Employee should be assigned to order");
        assertEquals(testEmployee.getEmail(), updatedOrder.getEmployee().getEmail());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void confirmOrder_WithUnauthorizedRole_ShouldThrowAccessDenied() {
        Order order = new Order();
        order.setClient(testClient);
        order.setOrderDate(LocalDateTime.now());
        orderRepository.save(order);

        assertThrows(AccessDeniedException.class, () ->
                orderService.confirmOrder(testClient.getEmail(), order.getOrderDate(), testEmployee.getEmail()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void confirmOrder_WhenOrderNotFound_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.confirmOrder("no@client.com", LocalDateTime.now(), testEmployee.getEmail()));
        assertEquals("Order is not found", ex.getMessage());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void confirmOrder_WhenEmployeeNotFound_ShouldThrowIllegalArgumentException() {
        Order order = new Order();
        testClient.setEmail("123@gmail.com");
        order.setClient(testClient);
        order.setOrderDate(LocalDateTime.now().minusSeconds(30).withNano(0));
        orderRepository.saveAndFlush(order);

        Order savedOrder = orderRepository.findById(order.getId()).orElseThrow();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                orderService.confirmOrder(testClient.getEmail(), savedOrder.getOrderDate(), "nobody@company.com"));
        assertEquals("Employee is not found", ex.getMessage());
    }


    @Test
    void searchOrders_ByClientEmail_ShouldReturnMatchingOrders() {
        Client otherClient = new Client();
        otherClient.setEmail("someone@else.com");
        otherClient.setName("X");
        otherClient.setPassword("pwd");
        otherClient.setBalance(BigDecimal.TEN);
        clientRepository.save(otherClient);

        Order o1 = new Order();
        o1.setClient(testClient);
        o1.setOrderDate(LocalDateTime.now().minusDays(1));
        o1.setPrice(BigDecimal.TEN);
        orderRepository.save(o1);

        Order o2 = new Order();
        o2.setClient(otherClient);
        o2.setOrderDate(LocalDateTime.now().minusDays(2));
        o2.setPrice(BigDecimal.ONE);
        orderRepository.save(o2);

        Order o3 = new Order();
        o3.setClient(testClient);
        o3.setOrderDate(LocalDateTime.now().minusDays(3));
        o3.setPrice(BigDecimal.ONE);
        orderRepository.save(o3);

        Page<OrderDTO> page = orderService.searchOrders("clientEmail", "testclient", 1, 10, "orderDate", "asc");

        List<OrderDTO> results = page.getContent();
        assertTrue(results.stream().allMatch(o -> o.getClientEmail().equals(testClient.getEmail())));
        assertEquals(2, results.size());
    }

    @Test
    void searchOrders_ByEmployeeEmail_ShouldReturnMatchingOrders() {
        Order o1 = new Order();
        o1.setClient(testClient);
        o1.setEmployee(testEmployee);
        o1.setOrderDate(LocalDateTime.now().minusHours(4));
        o1.setPrice(BigDecimal.TEN);
        orderRepository.save(o1);

        Order o2 = new Order();
        o2.setClient(testClient);
        o2.setEmployee(null);
        o2.setOrderDate(LocalDateTime.now().minusHours(3));
        o2.setPrice(BigDecimal.ONE);
        orderRepository.save(o2);

        Page<OrderDTO> page = orderService.searchOrders("employeeEmail", "worker", 1, 5, "orderDate", "desc");

        List<OrderDTO> results = page.getContent();
        assertEquals(1, results.size());
        assertEquals(testEmployee.getEmail(), results.get(0).getEmployeeEmail());
    }

    @Test
    void searchOrders_WhenValueBlank_ShouldReturnAllPaged() {
        for (int i = 0; i < 3; i++) {
            Order o = new Order();
            o.setClient(testClient);
            o.setOrderDate(LocalDateTime.now().minusMinutes(i));
            o.setPrice(BigDecimal.valueOf(10 * (i + 1)));
            orderRepository.save(o);
        }
        Page<OrderDTO> page = orderService.searchOrders("anything", "", 1, 2, "orderDate", "desc");
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }

    @Test
    void getOrdersByClientPaged_ShouldReturnClientOrdersPage() {
        for (int i = 1; i <= 3; i++) {
            Order o = new Order();
            o.setClient(testClient);
            o.setOrderDate(LocalDateTime.now().plusSeconds(i));
            o.setPrice(BigDecimal.TEN);
            orderRepository.save(o);
        }
        Page<OrderDTO> page1 = orderService.getOrdersByClient(testClient.getEmail(), 1, 2, "orderDate", "asc");

        assertEquals(2, page1.getContent().size());
        assertEquals(3, page1.getTotalElements());

        Page<OrderDTO> page2 = orderService.getOrdersByClient(testClient.getEmail(), 2, 2, "orderDate", "asc");

        assertEquals(1, page2.getContent().size());
    }
}
