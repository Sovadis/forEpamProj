package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class EmployeeServiceTest {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void cleanUp() {
        employeeRepository.deleteAll();
    }

    @Test
    void getAllEmployees_ShouldReturnAllEmployeeDTOs() {
        Employee e1 = new Employee();
        e1.setEmail("emp1@company.com");
        e1.setName("Emp One");
        e1.setPassword("pass");
        e1.setPhone("123456");
        e1.setBirthDate(LocalDate.of(1990, 1, 1));
        employeeRepository.save(e1);
        Employee e2 = new Employee();
        e2.setEmail("emp2@company.com");
        e2.setName("Emp Two");
        e2.setPassword("pass");
        e2.setPhone("654321");
        e2.setBirthDate(LocalDate.of(1995, 5, 5));
        employeeRepository.save(e2);

        List<EmployeeDTO> allEmployees = employeeService.getAllEmployees();

        assertEquals(2, allEmployees.size());
        List<String> emails = allEmployees.stream().map(EmployeeDTO::getEmail).toList();
        assertTrue(emails.contains("emp1@company.com"));
        assertTrue(emails.contains("emp2@company.com"));
    }

    @Test
    void getEmployeeByEmail_WhenExists_ShouldReturnDTO() {
        Employee emp = new Employee();
        emp.setEmail("john.doe@company.com");
        emp.setName("John Doe");
        emp.setPassword("pwd");
        emp.setPhone("111222333");
        employeeRepository.save(emp);

        EmployeeDTO dto = employeeService.getEmployeeByEmail("john.doe@company.com");

        assertNotNull(dto);
        assertEquals("john.doe@company.com", dto.getEmail());
        assertEquals("John Doe", dto.getName());
        assertEquals("111222333", dto.getPhone());
    }

    @Test
    void getEmployeeByEmail_WhenNotFound_ShouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                employeeService.getEmployeeByEmail("no.such@company.com"));
        assertEquals("Employee was not found", ex.getMessage());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void updateEmployeeByEmail_WithAllowedRole_ShouldUpdateEmployee() {
        Employee emp = new Employee();
        emp.setEmail("jane@company.com");
        emp.setName("Jane");
        emp.setPhone("000");
        emp.setPassword("oldpwd");
        emp.setBirthDate(LocalDate.of(1980, 1, 1));
        employeeRepository.save(emp);

        EmployeeDTO updateDto = new EmployeeDTO();
        updateDto.setEmail("jane.new@company.com");
        updateDto.setName("Jane New");
        updateDto.setPassword("newpwd");
        updateDto.setPhone("999");
        updateDto.setBirthDate(LocalDate.of(1980, 1, 1));

        EmployeeDTO resultDto = employeeService.updateEmployeeByEmail("jane@company.com", updateDto);

        assertEquals("jane.new@company.com", resultDto.getEmail());
        assertEquals("Jane New", resultDto.getName());
        assertEquals("999", resultDto.getPhone());
        if (resultDto.getPassword() != null) {
            assertNotEquals("newpwd", resultDto.getPassword());
        }
        Optional<Employee> opt = employeeRepository.findByEmail("jane.new@company.com");
        assertTrue(opt.isPresent(), "Employee email should be updated in repository");
        Employee updatedEmp = opt.get();
        assertEquals("Jane New", updatedEmp.getName());
        assertEquals("999", updatedEmp.getPhone());
        assertTrue(updatedEmp.getPassword().startsWith("$") || !updatedEmp.getPassword().equals("newpwd"),
                "Password should be encoded in database");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEmployeeByEmail_WhenNotFound_ShouldThrowNotFoundException() {
        EmployeeDTO updateDto = new EmployeeDTO();
        updateDto.setEmail("doesntmatter@company.com");
        updateDto.setName("No One");

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                employeeService.updateEmployeeByEmail("ghost@company.com", updateDto));
        assertEquals("Employee was not found", ex.getMessage());
    }

    @Test
    void updateEmployeeByEmail_WithoutAuth_ShouldThrowAccessDenied() {
        Employee emp = new Employee();
        emp.setEmail("mark@company.com");
        emp.setName("Mark");
        emp.setPassword("pwd");
        employeeRepository.save(emp);
        assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
                employeeService.updateEmployeeByEmail("mark@company.com", new EmployeeDTO()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEmployeeByEmail_WithAdminRole_ShouldRemoveEmployee() {
        Employee emp = new Employee();
        emp.setEmail("to.delete@company.com");
        emp.setName("ToDelete");
        emp.setPassword("pwd");
        employeeRepository.save(emp);
        assertTrue(employeeRepository.findByEmail("to.delete@company.com").isPresent());

        employeeService.deleteEmployeeByEmail("to.delete@company.com");

        assertFalse(employeeRepository.findByEmail("to.delete@company.com").isPresent(), "Employee should be deleted");
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deleteEmployeeByEmail_WithNonAdminRole_ShouldThrowAccessDenied() {
        Employee emp = new Employee();
        emp.setEmail("joe@company.com");
        emp.setName("Joe");
        emp.setPassword("pwd");
        employeeRepository.save(emp);

        assertThrows(AccessDeniedException.class, () ->
                employeeService.deleteEmployeeByEmail("joe@company.com"));
        assertTrue(employeeRepository.findByEmail("joe@company.com").isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEmployeeByEmail_WhenNotExist_ShouldDoNothing() {
        employeeService.deleteEmployeeByEmail("absent@company.com");
        assertEquals(0, employeeRepository.count());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void addEmployee_WithAllowedRole_ShouldSaveEmployee() {
        EmployeeDTO newEmp = new EmployeeDTO();
        newEmp.setEmail("newhire@company.com");
        newEmp.setName("New Hire");
        newEmp.setPassword("plaintext");
        newEmp.setPhone("101010");
        newEmp.setBirthDate(LocalDate.of(2000, 1, 1));

        EmployeeDTO savedDto = employeeService.addEmployee(newEmp);

        assertEquals("newhire@company.com", savedDto.getEmail());
        assertEquals("New Hire", savedDto.getName());
        if (savedDto.getPassword() != null) {
            assertNotEquals("plaintext", savedDto.getPassword());
        }
        Employee savedEntity = employeeRepository.findByEmail("newhire@company.com").orElseThrow();
        assertEquals("New Hire", savedEntity.getName());
        assertTrue(savedEntity.getPassword() != null && !savedEntity.getPassword().equals("plaintext"),
                "Stored password should be encoded");
        assertEquals("101010", savedEntity.getPhone());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addEmployee_WhenEmailExists_ShouldThrowAlreadyExistException() {
        Employee existing = new Employee();
        existing.setEmail("exists@company.com");
        existing.setName("Existing");
        existing.setPassword("pwd");
        employeeRepository.save(existing);

        EmployeeDTO dup = new EmployeeDTO();
        dup.setEmail("exists@company.com");
        dup.setName("Dupe");
        dup.setPassword("pwd2");

        AlreadyExistException ex = assertThrows(AlreadyExistException.class, () ->
                employeeService.addEmployee(dup));
        assertTrue(ex.getMessage().contains("already exists"));
        assertEquals(1, employeeRepository.count());
    }

    @Test
    void addEmployee_WithoutAuth_ShouldThrowAccessDenied() {
        EmployeeDTO newEmp = new EmployeeDTO();
        newEmp.setEmail("unauth@company.com");
        newEmp.setName("Unauth");
        newEmp.setPassword("pwd");
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> employeeService.addEmployee(newEmp));
        assertFalse(employeeRepository.findByEmail("unauth@company.com").isPresent());
    }

    @Test
    void searchEmployees_ByName_ShouldReturnMatchingPage() {
        Employee e1 = new Employee();
        e1.setEmail("aa@co.com");
        e1.setName("Alice Wonderland");
        e1.setPassword("p");
        employeeRepository.save(e1);
        Employee e2 = new Employee();
        e2.setEmail("bb@co.com");
        e2.setName("Bob Marley");
        e2.setPassword("p");
        employeeRepository.save(e2);
        Employee e3 = new Employee();
        e3.setEmail("cc@co.com");
        e3.setName("Alicia Keys");
        e3.setPassword("p");
        employeeRepository.save(e3);

        Page<EmployeeDTO> page = employeeService.searchEmployees("name", "Ali", 1, 10, "name", "asc");

        List<EmployeeDTO> results = page.getContent();
        List<String> names = results.stream().map(EmployeeDTO::getName).toList();
        assertTrue(names.contains("Alice Wonderland"));
        assertTrue(names.contains("Alicia Keys"));
        assertFalse(names.contains("Bob Marley"));
    }

    @Test
    void searchEmployees_ByEmail_ShouldReturnMatchingPage() {
        Employee e1 = new Employee();
        e1.setEmail("first@org.com");
        e1.setName("First");
        e1.setPassword("p");
        employeeRepository.save(e1);
        Employee e2 = new Employee();
        e2.setEmail("second@test.org");
        e2.setName("Second");
        e2.setPassword("p");
        employeeRepository.save(e2);

        Page<EmployeeDTO> page = employeeService.searchEmployees("email", "org", 1, 5, "email", "desc");

        List<String> emails = page.getContent().stream().map(EmployeeDTO::getEmail).toList();
        assertTrue(emails.contains("first@org.com"));
        assertTrue(emails.contains("second@test.org"));
    }

    @Test
    void searchEmployees_WhenValueBlank_ShouldReturnAll() {
        for (int i = 1; i <= 3; i++) {
            Employee e = new Employee();
            e.setEmail("person" + i + "@example.com");
            e.setName("Person" + i);
            e.setPassword("p");
            employeeRepository.save(e);
        }
        Page<EmployeeDTO> page = employeeService.searchEmployees("irrelevant", "", 1, 2, "email", "asc");
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }
}
