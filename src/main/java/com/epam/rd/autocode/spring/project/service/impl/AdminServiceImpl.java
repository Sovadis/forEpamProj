package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ClientDTOMapper;
import com.epam.rd.autocode.spring.project.mapper.EmployeeDTOMapper;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.AdminService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientService clientService;
    private final EmployeeService employeeService;
    private final ClientDTOMapper clientDTOMapper;
    private final EmployeeDTOMapper employeeDTOMapper;

    public AdminServiceImpl(ClientRepository clientRepository,
                            EmployeeRepository employeeRepository,
                            ClientService clientService,
                            EmployeeService employeeService, ClientDTOMapper clientDTOMapper, EmployeeDTOMapper employeeDTOMapper) {
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.clientService = clientService;
        this.employeeService = employeeService;
        this.clientDTOMapper = clientDTOMapper;
        this.employeeDTOMapper = employeeDTOMapper;
    }

    @Override
    public void updateUserRole(Long userId, String userType, String newRole) {
        log.info("Updating role for userId: {} type: {} to newRole: {}", userId, userType, newRole);
        if ("client".equalsIgnoreCase(userType) && clientRepository.findById(userId).isPresent()) {
            clientRepository.findById(userId).ifPresent(client -> {
                client.setRole(Role.valueOf(newRole));
                clientRepository.save(client);
                log.info("Client role updated successfully");
            });
            return;

        } else if ("employee".equalsIgnoreCase(userType) && employeeRepository.findById(userId).isPresent()) {
            employeeRepository.findById(userId).ifPresent(employee -> {
                employee.setRole(Role.valueOf(newRole));
                employeeRepository.save(employee);
                log.info("Employee role updated successfully");

            });
            return;
        }
        log.warn("User with Id: {} was not found while trying to change his role to {}", userId, newRole);
        throw new NotFoundException("User was not found");
    }

    @Override
    public List<ClientDTO> getAllClients() {
        log.info("Retrieving all clients for admin panel");
        return clientService.getAllClients();
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        log.info("Retrieving all employees for admin panel");
        return employeeService.getAllEmployees();
    }

    @Override
    public List<ClientDTO> searchClients(String searchField, String searchValue) {
        log.info("Searching clients by {}: {}", searchField, searchValue);
        if ("email".equalsIgnoreCase(searchField)) {
            return clientRepository.findByEmailContainingIgnoreCase(searchValue)
                    .stream()
                    .map(clientDTOMapper::convertClientToClientDTO)
                    .collect(Collectors.toList());
        } else if ("role".equalsIgnoreCase(searchField)) {
            try {
                String roleName = searchValue.toUpperCase();
                if (!roleName.startsWith("ROLE_")) {
                    roleName = "ROLE_" + roleName;
                }
                Role role = Role.valueOf(roleName);
                return clientRepository.findByRole(role)
                        .stream()
                        .map(clientDTOMapper::convertClientToClientDTO)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role provided for search: {}", searchValue);
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }


    @Override
    public List<EmployeeDTO> searchEmployees(String searchField, String searchValue) {
        log.info("Searching employees by {}: {}", searchField, searchValue);
        if ("email".equalsIgnoreCase(searchField)) {
            return employeeRepository.findByEmailContainingIgnoreCase(searchValue)
                    .stream()
                    .map(employeeDTOMapper::convertEmployeeToEmployeeDTO)
                    .collect(Collectors.toList());
        } else if ("role".equalsIgnoreCase(searchField)) {
            try {
                String roleName = searchValue.toUpperCase();
                if (!roleName.startsWith("ROLE_")) {
                    roleName = "ROLE_" + roleName;
                }
                Role role = Role.valueOf(roleName);
                return employeeRepository.findByRole(role)
                        .stream()
                        .map(employeeDTOMapper::convertEmployeeToEmployeeDTO)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role provided for search: {}", searchValue);
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
}

