package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class AdminServiceTest {
    private final AdminService adminService;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AdminServiceTest(AdminService adminService,
                            ClientRepository clientRepository,
                            EmployeeRepository employeeRepository) {
        this.adminService = adminService;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
    }

    @Test
    void testUpdateUserRole_shouldUpdateClient() {
        Client client = new Client();
        client.setEmail("client@example.com");
        client.setRole(Role.ROLE_CLIENT);
        client = clientRepository.save(client);
        adminService.updateUserRole(client.getId(), "client", "ROLE_ADMIN");

        Client updatedClient = clientRepository.findById(client.getId()).orElseThrow();
        assertEquals(Role.ROLE_ADMIN, updatedClient.getRole());
    }

    @Test
    void testUpdateUserRole_shouldUpdateEmployee() {
        Employee employee = new Employee();
        employee.setEmail("employee@example.com");
        employee.setRole(Role.ROLE_EMPLOYEE);
        employee = employeeRepository.save(employee);
        adminService.updateUserRole(employee.getId(), "employee", "ROLE_ADMIN");

        Employee updatedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertEquals(Role.ROLE_ADMIN, updatedEmployee.getRole());
    }

    @Test
    void testUpdateUserRole_shouldThrowNotFoundEx() {
        Long failingId = Long.MAX_VALUE;
        assertThrows(NotFoundException.class, () -> {
            adminService.updateUserRole(failingId, "client", "ROLE_ADMIN");
        });

        assertThrows(NotFoundException.class, () -> {
            adminService.updateUserRole(failingId, "employee", "ROLE_ADMIN");
        });
    }
}
