package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.RegistrationForm;
import com.epam.rd.autocode.spring.project.service.ClientService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;

@Controller
@Slf4j
public class RegistrationController {

    private final ClientService clientService;

    public RegistrationController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        log.info("Displaying registration form");
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register_form";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationForm") RegistrationForm registrationForm,
            BindingResult bindingResult,
            Model model
    ) {
        log.info("Attempting to register user with email: {}", registrationForm.getEmail());

        if (!registrationForm.getPassword().equals(registrationForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "validation.password.confirmation", "Passwords do not match.");
        }

        if (clientService.getClientByEmail(registrationForm.getEmail()) != null) {
            bindingResult.rejectValue("email", "registration.email.exists", "A user with this email already exists.");
        }

        if (bindingResult.hasErrors()) {
            log.debug("Registration form has errors: {}", bindingResult.getAllErrors());
            return "register_form";
        }

        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setEmail(registrationForm.getEmail());
        clientDTO.setName(registrationForm.getName());
        clientDTO.setPassword(registrationForm.getPassword());
        clientDTO.setBalance(BigDecimal.ZERO);

        ClientDTO registered = clientService.addClient(clientDTO);
        if (registered == null) {
            log.error("Registration failed for email: {}", registrationForm.getEmail());
            bindingResult.reject("registration.error.general", "Registration failed. Please try again.");
            return "register_form";
        }
        log.info("User registered successfully with email: {}", registrationForm.getEmail());
        return "redirect:/login";
    }
}
