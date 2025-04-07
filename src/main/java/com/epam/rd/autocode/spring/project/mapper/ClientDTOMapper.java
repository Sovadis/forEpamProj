package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ClientDTOMapper {
    private ModelMapper mapper;

    public ClientDTOMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public ClientDTO convertClientToClientDTO(Client client) {
        log.debug("Mapping Client to ClientDTO for email: {}", client.getEmail());
        return mapper.map(client, ClientDTO.class);
    }

    public Client convertClientDTOToClient(ClientDTO clientDTO) {
        log.debug("Mapping ClientDTO to Client for email: {}", clientDTO.getEmail());
        return mapper.map(clientDTO, Client.class);
    }
}
