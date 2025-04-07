package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class EmployeeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EmployeeRepository employeeRepository;


    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testAddEmployee() throws Exception {
        mockMvc.perform(post("/employees/add")
                        .param("email", "emp@test.com")
                        .param("name", "Emp Name")
                        .param("password", "empPass")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        Employee emp = employeeRepository.findByEmail("emp@test.com").orElse(null);
        assertThat(emp).isNotNull();
        assertThat(emp.getName()).isEqualTo("Emp Name");
    }
}
