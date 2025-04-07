package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        bookRepository.deleteAll();
        clientRepository.deleteAll();

        Client client = new Client();
        client.setEmail("client1@example.com");
        client.setPassword(passwordEncoder.encode("pass"));
        client.setName("Test Client");
        client.setRole(Role.ROLE_CLIENT);
        clientRepository.save(client);
    }

    @Test
    @Transactional
    @WithMockUser(username = "client1@example.com", roles = {"CLIENT"})
    void testAddToCartAndView() throws Exception {
        Book book = new Book();
        book.setName("Book A");
        book.setAuthor("Author A");
        book.setPrice(BigDecimal.valueOf(100));
        bookRepository.save(book);

        MvcResult addResult = mockMvc.perform(get("/cart/add")
                        .param("name", "Book A"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) addResult.getRequest().getSession();

        mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Book A")));
    }
}

