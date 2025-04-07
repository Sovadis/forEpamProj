package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);

    Page<Client> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Client> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    List<Client> findByEmailContainingIgnoreCase(String email);

    List<Client> findByRole(Role role);
}
