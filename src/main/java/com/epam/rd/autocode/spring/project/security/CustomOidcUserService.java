package com.epam.rd.autocode.spring.project.security;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class CustomOidcUserService extends OidcUserService {
    private final ClientService clientService;

    public CustomOidcUserService(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        log.info("Google login for email: {}", email);

        ClientDTO client = clientService.getClientByEmail(email);
        if (client == null) {
            ClientDTO newClient = new ClientDTO();
            newClient.setEmail(email);
            newClient.setName(oidcUser.getAttribute("name"));
            newClient.setRole(null);
            newClient.setPassword(null);
            try {
                clientService.addClient(newClient);
                log.info("Created new client account for Google user: {}", email);
            } catch (AlreadyExistException ex) {
                log.warn("Client with email {} already exists (Google login)", email);
            }
            client = clientService.getClientByEmail(email);
        }

        if (client != null && client.getRole() != null) {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            String roleName = client.getRole().toString();
            mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
            mappedAuthorities.addAll(oidcUser.getAuthorities());
            oidcUser = new DefaultOidcUser(mappedAuthorities, userRequest.getIdToken(), oidcUser.getUserInfo());
            log.info("Assigned role '{}' to Google user {} in SecurityContext", roleName, email);
        }

        return oidcUser;
    }
}
