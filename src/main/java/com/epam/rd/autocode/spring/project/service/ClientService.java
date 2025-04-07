package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface ClientService {

    List<ClientDTO> getAllClients();

    ClientDTO getClientByEmail(String email);

    ClientDTO updateClientByEmail(String email, ClientDTO client);

    void deleteClientByEmail(String email);

    @Transactional
    void deleteClientAccountByEmail(String email);

    ClientDTO addClient(ClientDTO client);

    Page<ClientDTO> searchClients(String searchField, String searchValue, int page, int size, String sortField, String sortDir);

    @Transactional
    void topUpBalance(String email, BigDecimal amount);

    @Transactional
    void blockClient(String email);

    @Transactional
    void unblockClient(String email);
}
