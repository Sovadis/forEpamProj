package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;

    public CustomUserDetailsService(EmployeeRepository employeeRepository, ClientRepository clientRepository) {
        this.employeeRepository = employeeRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username (email): {}", username);
        Optional<Employee> empOpt = employeeRepository.findByEmail(username);
        if (empOpt.isPresent()) {
            Employee employee = empOpt.get();
            String role = employee.getRole().name();
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
            log.debug("Employee found: {} with role {}", employee.getEmail(), role);
            return new org.springframework.security.core.userdetails.User(
                    employee.getEmail(),
                    employee.getPassword(),
                    true,
                    true,
                    true,
                    !employee.isBlocked(),
                    authorities
            );
        }

        Optional<Client> clientOpt = clientRepository.findByEmail(username);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();

            String pwd = client.getPassword();
            if (pwd == null || pwd.trim().isEmpty()) {
                pwd = "GOOGLE_PASSWORD_BOM";
            }

            String roleString = (client.getRole() == null) ? "ROLE_CLIENT" : client.getRole().name();
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(roleString));
            log.debug("Client found: {}", client.getEmail());
            return new org.springframework.security.core.userdetails.User(
                    client.getEmail(),
                    pwd,
                    true,
                    true,
                    true,
                    !client.isBlocked(),
                    authorities
            );
        }
        log.error("User not found with email: {}", username);
        throw new UsernameNotFoundException("User not found with email: " + username);
    }
}
