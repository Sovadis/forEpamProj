package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ClientServiceTest {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
    }

    @Test
    void getAllClients_ShouldReturnAllClients() {
        Client client1 = new Client();
        client1.setEmail("alice@example.com");
        client1.setName("Alice");
        client1.setPassword("pass1");
        client1.setBalance(BigDecimal.valueOf(100));
        client1.setRole(Role.ROLE_CLIENT);
        clientRepository.save(client1);

        Client client2 = new Client();
        client2.setEmail("bob@example.com");
        client2.setName("Bob");
        client2.setPassword("pass2");
        client2.setBalance(BigDecimal.valueOf(200));
        client2.setRole(Role.ROLE_CLIENT);
        clientRepository.save(client2);

        List<ClientDTO> allClients = clientService.getAllClients();

        assertEquals(2, allClients.size(), "Should return exactly 2 clients");
        List<String> emails = allClients.stream().map(ClientDTO::getEmail).toList();
        assertTrue(emails.contains("alice@example.com"));
        assertTrue(emails.contains("bob@example.com"));
    }

    @Test
    void getClientByEmail_WhenExists_ShouldReturnClientDTO() {
        Client client = new Client();
        client.setEmail("charlie@example.com");
        client.setName("Charlie");
        client.setPassword("secret");
        client.setRole(Role.ROLE_CLIENT);
        clientRepository.save(client);

        ClientDTO result = clientService.getClientByEmail("charlie@example.com");

        assertNotNull(result, "Result should not be null for existing client");
        assertEquals("charlie@example.com", result.getEmail());
        assertEquals("Charlie", result.getName());
    }

    @Test
    void getClientByEmail_WhenNotExists_ShouldReturnNull() {
        ClientDTO result = clientService.getClientByEmail("nonexistent@example.com");
        assertNull(result, "Should return null when client not found");
    }

    @Test
    void updateClientByEmail_WhenClientExists_ShouldUpdateFields() {
        Client client = new Client();
        client.setEmail("dave@example.com");
        client.setName("Dave");
        client.setPassword("oldpass");
        client.setBalance(BigDecimal.valueOf(50));
        client.setRole(Role.ROLE_CLIENT);
        clientRepository.save(client);

        ClientDTO updateDto = new ClientDTO();
        updateDto.setEmail("dave@example.com");
        updateDto.setName("David");
        updateDto.setPassword("newpass");
        updateDto.setBalance(BigDecimal.valueOf(150));

        ClientDTO updated = clientService.updateClientByEmail("dave@example.com", updateDto);

        assertEquals("dave@example.com", updated.getEmail());
        assertEquals("David", updated.getName());
        if (updated.getPassword() != null) {
            assertNotEquals("newpass", updated.getPassword(), "Password should be encoded if present in DTO");
        }

        Client saved = clientRepository.findByEmail("dave@example.com").orElseThrow();
        assertEquals("David", saved.getName(), "Name should be updated in database");
        assertTrue(saved.getPassword().startsWith("$") || !saved.getPassword().equals("newpass"),
                "Password should be encoded in database");
        assertEquals(0, BigDecimal.valueOf(150).compareTo(saved.getBalance()), "Balance should be updated");
    }

    @Test
    void updateClientByEmail_WhenPasswordBlank_ShouldNotChangePassword() {
        Client client = new Client();
        client.setEmail("eve@example.com");
        client.setName("Eve");
        client.setPassword("encodedOldPassword");
        client.setRole(Role.ROLE_CLIENT);
        clientRepository.save(client);

        ClientDTO updateDto = new ClientDTO();
        updateDto.setEmail("eve@example.com");
        updateDto.setName("Eve Newname");
        updateDto.setPassword("   ");
        updateDto.setBalance(null);

        ClientDTO updated = clientService.updateClientByEmail("eve@example.com", updateDto);

        assertEquals("Eve Newname", updated.getName());
        Client updatedEntity = clientRepository.findByEmail("eve@example.com").orElseThrow();
        assertEquals("Eve Newname", updatedEntity.getName());
        assertEquals("encodedOldPassword", updatedEntity.getPassword(), "Password should remain unchanged");
    }

    @Test
    void updateClientByEmail_WhenNotFound_ShouldThrowNotFoundException() {
        ClientDTO updateDto = new ClientDTO();
        updateDto.setEmail("ghost@example.com");
        updateDto.setName("Ghost");

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                clientService.updateClientByEmail("ghost@example.com", updateDto));
        assertEquals("Client was not found", ex.getMessage());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteClientByEmail_WithAdminRole_ShouldDeleteClient() {
        Client client = new Client();
        client.setEmail("harry@example.com");
        client.setName("Harry");
        client.setPassword("pwd");
        client.setRole(Role.ROLE_CLIENT);
        clientRepository.save(client);
        assertTrue(clientRepository.findByEmail("harry@example.com").isPresent());

        clientService.deleteClientByEmail("harry@example.com");

        assertFalse(clientRepository.findByEmail("harry@example.com").isPresent(), "Client should be deleted");
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void deleteClientByEmail_WithEmployeeRole_ShouldDeleteClient() {
        Client client = new Client();
        client.setEmail("iris@example.com");
        client.setName("Iris");
        client.setPassword("pwd");
        client.setRole(Role.ROLE_CLIENT);
        clientRepository.save(client);

        clientService.deleteClientByEmail("iris@example.com");

        assertFalse(clientRepository.findByEmail("iris@example.com").isPresent());
    }

    @Test
    void deleteClientByEmail_WithoutProperRole_ShouldThrowAccessDenied() {
        Client client = new Client();
        client.setEmail("john@example.com");
        client.setName("John");
        client.setPassword("pwd");
        client.setRole(Role.ROLE_CLIENT);
        clientRepository.save(client);

        assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
                clientService.deleteClientByEmail("john@example.com"));
    }

    @Test
    void deleteClientByEmail_WhenClientNotFound_ShouldDoNothing() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", "pass", "ROLE_ADMIN")
        );
        clientService.deleteClientByEmail("noone@example.com");
        assertEquals(0, clientRepository.count());
        SecurityContextHolder.clearContext();
    }

    @Test
    void addClient_WhenNewEmail_ShouldSaveClient() {
        ClientDTO newClient = new ClientDTO();
        newClient.setEmail("kate@example.com");
        newClient.setName("Kate");
        newClient.setPassword("mypassword");
        newClient.setRole(null);

        ClientDTO savedDto = clientService.addClient(newClient);

        assertEquals("kate@example.com", savedDto.getEmail());
        assertEquals("Kate", savedDto.getName());
        assertEquals(Role.ROLE_CLIENT, savedDto.getRole());
        Client savedEntity = clientRepository.findByEmail("kate@example.com").orElseThrow();
        assertNotNull(savedEntity.getId(), "New client ID should be generated");
        assertEquals("Kate", savedEntity.getName());
        assertTrue(savedEntity.getPassword() != null && !savedEntity.getPassword().equals("mypassword"),
                "Stored password should be encoded");
        assertEquals(Role.ROLE_CLIENT, savedEntity.getRole());
    }

    @Test
    void addClient_WhenEmailAlreadyExists_ShouldThrowAlreadyExistException() {
        Client existing = new Client();
        existing.setEmail("lionel@example.com");
        existing.setName("Lionel");
        existing.setPassword("pwd");
        existing.setRole(Role.ROLE_CLIENT);
        clientRepository.save(existing);

        ClientDTO duplicate = new ClientDTO();
        duplicate.setEmail("lionel@example.com");
        duplicate.setName("Lionel2");
        duplicate.setPassword("otherpwd");

        AlreadyExistException ex = assertThrows(AlreadyExistException.class, () -> clientService.addClient(duplicate));
        assertTrue(ex.getMessage().contains("already exists"), "Exception message should indicate duplicate");
        assertEquals(1, clientRepository.count());
    }

    @Test
    void searchClients_WhenNameMatches_ShouldReturnMatchingClients() {
        Client c1 = new Client();
        c1.setEmail("mike@example.com");
        c1.setName("Michael Stone");
        c1.setPassword("pwd");
        c1.setRole(Role.ROLE_CLIENT);
        clientRepository.save(c1);
        Client c2 = new Client();
        c2.setEmail("michelle@example.com");
        c2.setName("Michelle Green");
        c2.setPassword("pwd");
        c2.setRole(Role.ROLE_CLIENT);
        clientRepository.save(c2);
        Client c3 = new Client();
        c3.setEmail("aaron@example.com");
        c3.setName("Aaron Smith");
        c3.setPassword("pwd");
        c3.setRole(Role.ROLE_CLIENT);
        clientRepository.save(c3);

        Page<ClientDTO> resultPage = clientService.searchClients("name", "mic", 1, 10, "name", "asc");

        List<ClientDTO> results = resultPage.getContent();
        assertEquals(2, results.size());
        List<String> names = results.stream().map(ClientDTO::getName).map(String::toLowerCase).toList();
        assertTrue(names.contains("michael stone"));
        assertTrue(names.contains("michelle green"));
    }

    @Test
    void searchClients_WhenEmailMatches_ShouldReturnMatchingClients() {
        Client c1 = new Client();
        c1.setEmail("sunny@example.com");
        c1.setName("Sunny");
        c1.setPassword("pwd");
        c1.setRole(Role.ROLE_CLIENT);
        clientRepository.save(c1);
        Client c2 = new Client();
        c2.setEmail("cloudy@sample.com");
        c2.setName("Cloudy");
        c2.setPassword("pwd");
        c2.setRole(Role.ROLE_CLIENT);
        clientRepository.save(c2);

        Page<ClientDTO> page = clientService.searchClients("email", "example.com", 1, 5, "email", "desc");

        List<ClientDTO> results = page.getContent();
        assertEquals(1, results.size());
        assertEquals("sunny@example.com", results.get(0).getEmail());
    }

    @Test
    void searchClients_WhenSearchValueBlank_ShouldReturnAllPaged() {
        for (int i = 1; i <= 3; i++) {
            Client c = new Client();
            c.setEmail("user" + i + "@test.com");
            c.setName("User" + i);
            c.setPassword("pwd");
            c.setRole(Role.ROLE_CLIENT);
            clientRepository.save(c);
        }
        Page<ClientDTO> page = clientService.searchClients("anyField", "", 1, 2, "email", "asc");
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
    }

    @Test
    void topUpBalance_WhenClientExists_ShouldIncreaseBalance() {
        Client client = new Client();
        client.setEmail("peter@example.com");
        client.setName("Peter");
        client.setPassword("pwd");
        client.setRole(Role.ROLE_CLIENT);
        client.setBalance(BigDecimal.valueOf(100));
        clientRepository.save(client);

        clientService.topUpBalance("peter@example.com", BigDecimal.valueOf(50));

        Client updated = clientRepository.findByEmail("peter@example.com").orElseThrow();
        assertEquals(0, BigDecimal.valueOf(150).compareTo(updated.getBalance()), "Balance should be 150 after top-up");
    }

    @Test
    void topUpBalance_WhenClientNotFound_ShouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                clientService.topUpBalance("unknown@example.com", BigDecimal.TEN));
        assertEquals("Client was not found", ex.getMessage());
    }
}
