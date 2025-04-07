package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.ClientDTOMapper;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.enums.Role;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ClientDTOMapper clientDTOMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;

    public ClientServiceImpl(ClientRepository clientRepository,
                             ClientDTOMapper clientDTOMapper,
                             PasswordEncoder passwordEncoder, OrderRepository orderRepository) {
        this.clientRepository = clientRepository;
        this.clientDTOMapper = clientDTOMapper;
        this.passwordEncoder = passwordEncoder;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<ClientDTO> getAllClients() {
        log.info("Retrieving all clients");
        return clientRepository.findAll().stream()
                .map(clientDTOMapper::convertClientToClientDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClientDTO getClientByEmail(String email) {
        log.info("Retrieving client with email: {}", email);
        return clientRepository.findByEmail(email)
                .map(clientDTOMapper::convertClientToClientDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public ClientDTO updateClientByEmail(String email, ClientDTO clientDTO) {
        log.info("Updating client data for email: {}", email);
        Optional<Client> opt = clientRepository.findByEmail(email);
        if (opt.isEmpty()) {
            log.warn("Client with email {} not found", email);
            throw new NotFoundException("Client was not found");
        }
        Client existing = opt.get();
        existing.setEmail(clientDTO.getEmail());

        if (clientDTO.getBalance() != null) {
            existing.setBalance(clientDTO.getBalance());
        }

        existing.setName(clientDTO.getName());
        if (clientDTO.getPassword() != null && !clientDTO.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        }
        Client saved = clientRepository.save(existing);
        log.info("Client {} updated successfully", saved.getEmail());
        return clientDTOMapper.convertClientToClientDTO(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("#email == authentication.name")
    public void deleteClientAccountByEmail(String email) {
        log.info("Deleting client account with email: {}", email);
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client was not found"));

        if (!orderRepository.findOrderByClient_Email(email).isEmpty()) {
            log.warn("Client {} cannot be deleted because there are existing orders", email);
            throw new IllegalStateException("client.delete.error.ordersExist");
        }
        clientRepository.delete(client);
        log.info("Client account {} deleted", email);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public void deleteClientByEmail(String email) {
        log.info("Deleting client with email: {}", email);
        clientRepository.findByEmail(email).ifPresent(clientRepository::delete);
        log.info("Client {} deleted", email);
    }

    @Override
    @Transactional
    public ClientDTO addClient(ClientDTO clientDTO) {
        log.info("Adding new client with email: {}", clientDTO.getEmail());
        if (clientRepository.findByEmail(clientDTO.getEmail()).isPresent()) {
            log.warn("Client with email {} already exists", clientDTO.getEmail());
            throw new AlreadyExistException("User with this email already exists");
        }
        if (clientDTO.getRole() == null) {
            clientDTO.setRole(Role.ROLE_CLIENT);
        }
        if (clientDTO.getPassword() != null && !clientDTO.getPassword().isBlank()) {
            clientDTO.setPassword(passwordEncoder.encode(clientDTO.getPassword()));
        }
        Client savedClient = clientRepository.save(clientDTOMapper.convertClientDTOToClient(clientDTO));
        log.info("Client {} added successfully", savedClient.getEmail());
        return clientDTOMapper.convertClientToClientDTO(savedClient);
    }

    @Override
    public Page<ClientDTO> searchClients(String searchField, String searchValue, int page, int size, String sortField, String sortDir) {
        log.info("Searching clients - field: {}, value: {}", searchField, searchValue);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.toUpperCase()), sortField);
        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);
        Page<Client> clientPage;
        if (searchValue == null || searchValue.isBlank()) {
            clientPage = clientRepository.findAll(pageRequest);
        } else if ("name".equalsIgnoreCase(searchField)) {
            clientPage = clientRepository.findByNameContainingIgnoreCase(searchValue, pageRequest);
        } else if ("email".equalsIgnoreCase(searchField)) {
            clientPage = clientRepository.findByEmailContainingIgnoreCase(searchValue, pageRequest);
        } else {
            clientPage = clientRepository.findAll(pageRequest);
        }
        return clientPage.map(clientDTOMapper::convertClientToClientDTO);
    }

    @Override
    public void topUpBalance(String email, BigDecimal amount) {
        log.info("Top up balance for client {} by {}", email, amount);
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client was not found"));
        BigDecimal currentBalance = client.getBalance() != null ? client.getBalance() : BigDecimal.ZERO;
        client.setBalance(currentBalance.add(amount));
        clientRepository.save(client);
        log.info("New balance for client {} is {}", email, client.getBalance());
    }

    @Override
    @Transactional
    public void blockClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        client.setBlocked(true);
        clientRepository.save(client);
        log.info("Client {} blocked successfully", email);
    }

    @Override
    @Transactional
    public void unblockClient(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Client not found with email: " + email));
        client.setBlocked(false);
        clientRepository.save(client);
        log.info("Client {} unblocked successfully", email);
    }

}

