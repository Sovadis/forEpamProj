package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collection;

@Controller
@RequestMapping("/account")
@Slf4j
public class AccountController {

    private final ClientService clientService;
    private final OrderService orderService;
    private final EmployeeService employeeService;

    public AccountController(ClientService clientService,
                             OrderService orderService,
                             EmployeeService employeeService) {
        this.clientService = clientService;
        this.orderService = orderService;
        this.employeeService = employeeService;
    }


    @GetMapping
    public String showAccount(Model model, Authentication authentication) {
        String email = authentication.getName();
        log.info("Displaying account for user: {}", email);
        try {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
                ClientDTO client = clientService.getClientByEmail(email);
                model.addAttribute("client", client);
                model.addAttribute("orders", orderService.getOrdersByClient(email));
                return "account";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE") ||
                    a.getAuthority().equals("ROLE_ADMIN"))) {
                EmployeeDTO employee = employeeService.getEmployeeByEmail(email);
                model.addAttribute("employee", employee);
                model.addAttribute("orders", orderService.getOrdersByEmployee(email));
                return "employeeAccount";
            }
        } catch (NotFoundException e) {
            log.error("Error retrieving account data for {}: {}", email, e.getMessage());
            model.addAttribute("errorMessage", "account.data.error");
        } catch (Exception e) {
            log.error("Unexpected error in showAccount for {}: {}", email, e.getMessage());
            model.addAttribute("errorMessage", "account.unknown.error");
        }
        return "redirect:/";
    }

    @GetMapping("/edit")
    public String showEditProfile(Model model, Authentication authentication) {
        String email = authentication.getName();
        log.info("Displaying edit profile for user: {}", email);
        try {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
                ClientDTO client = clientService.getClientByEmail(email);
                model.addAttribute("client", client);
                return "account_edit";
            } else if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE") ||
                    a.getAuthority().equals("ROLE_ADMIN"))) {
                EmployeeDTO employee = employeeService.getEmployeeByEmail(email);
                model.addAttribute("employee", employee);
                return "empAccount_edit";
            }
        } catch (NotFoundException e) {
            log.error("Profile not found for {}: {}", email, e.getMessage());
            model.addAttribute("errorMessage", "profile.not.found");
        } catch (Exception e) {
            log.error("Unexpected error in showEditProfile for {}: {}", email, e.getMessage());
            model.addAttribute("errorMessage", "profile.unknown.error");
        }
        return "redirect:/account";
    }

    @PostMapping(value = "/edit", params = "client")
    public String updateClientProfile(@ModelAttribute("client") ClientDTO clientDTO,
                                      Authentication authentication,
                                      Model model) {
        String email = authentication.getName();
        log.info("Updating client profile for user: {}", email);
        if (clientDTO.getPassword() != null && !clientDTO.getPassword().isBlank() &&
                clientDTO.getPassword().length() < 4) {
            log.warn("Client password too short for {}: {}", email, clientDTO.getPassword());
            model.addAttribute("errorMessage", "password.too.short");
            return "account_edit";
        }
        if (clientDTO.getName() == null || clientDTO.getName().isBlank()) {
            log.warn("Client name is blank {}: {}", email, clientDTO.getName());
            model.addAttribute("errorMessage", "name.absent");
            return "account_edit";
        }
        try {
            clientService.updateClientByEmail(email, clientDTO);
            return "redirect:/account";
        } catch (NotFoundException e) {
            log.error("Client update error for {}: {}", email, e.getMessage());
            model.addAttribute("errorMessage", "client.was.not.found");
            return "account_edit";
        } catch (IllegalArgumentException e) {
            log.error("Invalid client data for {}: {}", email, e.getMessage());
            model.addAttribute("errorMessage", "client.update.invalid");
            return "account_edit";
        } catch (Exception e) {
            log.error("Unexpected error during client update for {}: {}", email, e.getMessage());
            model.addAttribute("errorMessage", "profile.update.error");
            return "account_edit";
        }
    }

    @PostMapping(value = "/edit", params = "employee")
    public String updateEmployeeProfile(@ModelAttribute("employee") EmployeeDTO employeeDTO,
                                        Authentication authentication,
                                        Model model) {
        String email = authentication.getName();
        log.info("Updating employee profile for user: {}", email);
        try {
            employeeService.updateEmployeeByEmail(email, employeeDTO);
            return "redirect:/account";
        } catch (NotFoundException e) {
            log.error("Employee update error for {}: {}", email, e.getMessage());
            model.addAttribute("errorMessage", "employee.not.found");
            return "empAccount_edit";
        } catch (IllegalArgumentException e) {
            log.error("Invalid employee data for {}: {}", email, e.getMessage());
            model.addAttribute("errorMessage", "employee.update.invalid");
            return "empAccount_edit";
        } catch (Exception e) {
            log.error("Unexpected error during employee update for {}: {}", email, e.getMessage());
            model.addAttribute("errorMessage", "profile.update.error");
            return "empAccount_edit";
        }
    }

    @PostMapping("/topup")
    @PreAuthorize("hasRole('CLIENT')")
    public String topUpBalance(@RequestParam("amount") BigDecimal amount,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        log.info("Top-up request from client {}: amount={}", email, amount);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid top-up amount {} by client {}", amount, email);
            redirectAttributes.addFlashAttribute("errorMessage", "topup.invalid.amount");
            return "redirect:/account";
        }
        if (amount.compareTo(BigDecimal.valueOf(10000)) == 1) {
            log.warn("Invalid top-up amount {} by client {}", amount, email);
            redirectAttributes.addFlashAttribute("errorMessage", "topup.invalid.amount.to.much");
            return "redirect:/account";
        }
        try {
            clientService.topUpBalance(email, amount);
            log.info("Balance successfully topped up for client {} by {}", email, amount);
        } catch (NotFoundException e) {
            log.error("Client not found for top-up {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "client.not.found");
        } catch (Exception e) {
            log.error("Unexpected error during top-up for {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "topup.error");
        }
        return "redirect:/account";
    }

    @PostMapping("/delete")
    public String deleteAccount(Authentication authentication, RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        log.info("Deleting account for client: {}", email);
        try {
            clientService.deleteClientAccountByEmail(email);
            return "redirect:/logout";
        } catch (IllegalStateException e) {
            log.error("Error deleting account for {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/account";
        } catch (Exception e) {
            log.error("Unexpected error deleting account for {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "account.delete.error");
            return "redirect:/account";
        }
    }
}
