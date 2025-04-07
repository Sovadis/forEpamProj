package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.EmployeeDTOMapper;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeDTOMapper employeeDTOMapper;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeDTOMapper employeeDTOMapper, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.employeeDTOMapper = employeeDTOMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<EmployeeDTO> getAllEmployees() {
        log.info("Retrieving all employees");
        return employeeRepository.findAll().stream()
                .map(employeeDTOMapper::convertEmployeeToEmployeeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        log.info("Retrieving employee with email: {}", email);
        return employeeRepository.findByEmail(email)
                .map(employeeDTOMapper::convertEmployeeToEmployeeDTO)
                .orElseThrow(() -> {
                    log.warn("Employee with email {} not found", email);
                    return new NotFoundException("Employee was not found");
                });
    }


    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public EmployeeDTO updateEmployeeByEmail(String email, EmployeeDTO employeeDTO) {
        log.info("Updating employee with email: {}", email);
        Optional<Employee> opt = employeeRepository.findByEmail(email);
        if (opt.isEmpty()) {
            log.warn("Employee with email {} not found", email);
            throw new NotFoundException("Employee was not found");
        }

        Employee existing = opt.get();

        existing.setEmail(employeeDTO.getEmail());
        existing.setPhone(employeeDTO.getPhone());
        existing.setName(employeeDTO.getName());

        if (employeeDTO.getPassword() != null && !employeeDTO.getPassword().isBlank()) {
            if (employeeDTO.getPassword().length() < 4) {
                throw new IllegalArgumentException("Password must be at least 4 characters long");
            }
            existing.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));
        }

        existing.setBirthDate(employeeDTO.getBirthDate());
        Employee saved = employeeRepository.save(existing);
        log.info("Employee {} updated successfully", saved.getEmail());
        return employeeDTOMapper.convertEmployeeToEmployeeDTO(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployeeByEmail(String email) {
        log.info("Deleting employee with email: {}", email);
        employeeRepository.findByEmail(email).ifPresent(employeeRepository::delete);
        log.info("Employee {} deleted", email);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public EmployeeDTO addEmployee(EmployeeDTO employeeDTO) {
        log.info("Adding new employee with email: {}", employeeDTO.getEmail());
        if (employeeRepository.findByEmail(employeeDTO.getEmail()).isPresent()) {
            log.warn("Employee with email {} already exists", employeeDTO.getEmail());
            throw new AlreadyExistException("Employee with email: " + employeeDTO.getEmail() + " already exists");
        }
        if (employeeDTO.getPassword() == null || employeeDTO.getPassword().isBlank() || employeeDTO.getPassword().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long");
        }
        employeeDTO.setPassword(passwordEncoder.encode(employeeDTO.getPassword()));

        Employee savedEmployee = employeeRepository.save(employeeDTOMapper.convertEmployeeDTOToEmployee(employeeDTO));
        log.info("Employee {} added successfully", savedEmployee.getEmail());
        return employeeDTOMapper.convertEmployeeToEmployeeDTO(savedEmployee);
    }

    @Override
    public Page<EmployeeDTO> searchEmployees(String searchField, String searchValue, int page, int size, String sortField, String sortDir) {
        log.info("Searching employees - field: {}, value: {}", searchField, searchValue);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.toUpperCase()), sortField);
        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);
        Page<Employee> employeePage;
        if (searchValue == null || searchValue.isBlank()) {
            employeePage = employeeRepository.findAll(pageRequest);
        } else if ("name".equalsIgnoreCase(searchField)) {
            employeePage = employeeRepository.findByNameContainingIgnoreCase(searchValue, pageRequest);
        } else if ("email".equalsIgnoreCase(searchField)) {
            employeePage = employeeRepository.findByEmailContainingIgnoreCase(searchValue, pageRequest);
        } else {
            employeePage = employeeRepository.findAll(pageRequest);
        }
        return employeePage.map(employeeDTOMapper::convertEmployeeToEmployeeDTO);
    }

    @Override
    @Transactional
    public void blockEmployee(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee not found with email: " + email));
        employee.setBlocked(true);
        employeeRepository.save(employee);
        log.info("Employee {} blocked successfully", email);
    }

    @Override
    @Transactional
    public void unblockEmployee(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Employee not found with email: " + email));
        employee.setBlocked(false);
        employeeRepository.save(employee);
        log.info("Employee {} unblocked successfully", email);
    }

}

