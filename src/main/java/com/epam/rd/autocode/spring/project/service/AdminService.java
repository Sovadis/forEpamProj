package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;

import java.util.List;

public interface AdminService {
    void updateUserRole(Long userId, String userType, String newRole);

    List<ClientDTO> getAllClients();

    List<EmployeeDTO> getAllEmployees();

    List<ClientDTO> searchClients(String searchField, String searchValue);

    List<EmployeeDTO> searchEmployees(String searchField, String searchValue);
}
