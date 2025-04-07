package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ClientRepository clientRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        clientRepository.deleteAll();
    }

    @Test
    void testClientLoginSuccess() throws Exception {
        Client client = new Client();
        client.setEmail("test@example.com");
        client.setPassword(passwordEncoder.encode("secret123"));
        client.setName("Test User");
        client.setRole(Role.ROLE_CLIENT);
        clientRepository.save(client);

        mockMvc.perform(post("/login")
                        .param("email", "test@example.com")
                        .param("password", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void testClientLoginFail() throws Exception {
        Client client = new Client();
        client.setEmail("fail@example.com");
        client.setPassword(passwordEncoder.encode("secret123"));
        client.setRole(Role.ROLE_CLIENT);
        clientRepository.save(client);

        mockMvc.perform(post("/login")
                        .param("email", "fail@example.com")
                        .param("password", "wrong-pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeHasErrors("loginForm"));
    }
}
