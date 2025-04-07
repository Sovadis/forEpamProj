package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        clientRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    @Transactional
    @WithMockUser(username = "cust@example.com", roles = {"CLIENT"})
    void testAddOrder() throws Exception {
        Client client = new Client();
        client.setEmail("cust@example.com");
        client.setPassword(passwordEncoder.encode("12345"));
        client.setName("Заказчик");
        client.setRole(Role.ROLE_CLIENT);
        client.setBalance(BigDecimal.valueOf(1000));
        clientRepository.save(client);

        Book book = new Book();
        book.setName("Book for Order");
        book.setPrice(BigDecimal.valueOf(200));
        bookRepository.save(book);

        long beforeCount = orderRepository.count();

        mockMvc.perform(post("/orders/add")
                        .param("clientEmail", "cust@example.com")
                        .param("bookItems[0].bookName", "Book for Order")
                        .param("bookItems[0].quantity", "2")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));

        long afterCount = orderRepository.count();
        assertThat(afterCount).isEqualTo(beforeCount + 1);
        Order order = orderRepository.findAll().get(0);
        assertThat(order.getBookItems()).hasSize(1);
        assertThat(order.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(400));
    }
}
