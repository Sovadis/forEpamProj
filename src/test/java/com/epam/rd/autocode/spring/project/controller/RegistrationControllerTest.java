package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class RegistrationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ClientRepository clientRepository;

    @BeforeEach
    void cleanUp() {
        clientRepository.deleteAll();
    }

    @Test
    void testRegisterNewClient() throws Exception {
        mockMvc.perform(post("/register")
                        .param("name", "Новичок")
                        .param("email", "new@example.com")
                        .param("password", "mypassword")
                        .param("confirmPassword", "mypassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        Client saved = clientRepository.findByEmail("new@example.com").orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getName()).isEqualTo("Новичок");
    }
}
