package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/clients")
@Slf4j
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientServiceImpl clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public String getAllClients(
            @RequestParam(name = "searchField", required = false) String searchField,
            @RequestParam(name = "searchValue", required = false) String searchValue,
            @RequestParam(name = "sortField", defaultValue = "name") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            log.info("Fetching clients with searchField={}, searchValue={}, sortField={}, sortDir={}, page={}, size={}",
                    searchField, searchValue, sortField, sortDir, page, size);
            Page<ClientDTO> clientPage = clientService.searchClients(searchField, searchValue, page, size, sortField, sortDir);
            model.addAttribute("page", clientPage);
            model.addAttribute("clients", clientPage.getContent());
            model.addAttribute("searchField", searchField);
            model.addAttribute("searchValue", searchValue);
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("size", size);
            model.addAttribute("currentPage", page);
            return "clients";
        } catch (Exception e) {
            log.error("Error fetching clients: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "clients.fetch.error");
            return "redirect:/clients";
        }
    }

    @GetMapping("/{email}")
    public String getClientByEmail(@PathVariable String email, Model model, RedirectAttributes redirectAttributes) {
        try {
            log.info("Fetching client with email: {}", email);
            ClientDTO client = clientService.getClientByEmail(email);
            if (client == null) {
                log.warn("Client with email {} not found", email);
                redirectAttributes.addFlashAttribute("errorMessage", "client.not.found");
                return "redirect:/clients";
            }
            model.addAttribute("clients", List.of(client));
            return "clients";
        } catch (Exception e) {
            log.error("Error fetching client with email {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "client.fetch.error");
            return "redirect:/clients";
        }
    }

    @GetMapping("/new")
    public String showAddClientForm(Model model, RedirectAttributes redirectAttributes) {
        try {
            log.info("Displaying add client form");
            model.addAttribute("client", new ClientDTO());
            return "client_form";
        } catch (Exception e) {
            log.error("Error displaying add client form: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "client.form.error");
            return "redirect:/clients";
        }
    }

    @PostMapping("/add")
    public String addClient(@ModelAttribute("client") ClientDTO clientDTO,
                            RedirectAttributes redirectAttributes) {
        try {
            log.info("Adding new client with email: {}", clientDTO.getEmail());
            clientService.addClient(clientDTO);
            return "redirect:/clients";
        } catch (Exception e) {
            log.error("Error adding client: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "client.add.error");
            return "redirect:/clients/new";
        }
    }

    @GetMapping("/edit/{email}")
    public String showEditClientForm(@PathVariable String email,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        try {
            log.info("Editing client with email: {}", email);
            ClientDTO client = clientService.getClientByEmail(email);
            if (client == null) {
                log.warn("Client {} not found", email);
                redirectAttributes.addFlashAttribute("errorMessage", "client.not.found");
                return "redirect:/clients";
            }
            model.addAttribute("client", client);
            return "client_form";
        } catch (Exception e) {
            log.error("Error displaying edit client form for {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "client.edit.form.error");
            return "redirect:/clients";
        }
    }

    @PostMapping("/edit")
    public String updateClient(@RequestParam("originalEmail") String originalEmail,
                               @ModelAttribute("client") ClientDTO clientDTO,
                               RedirectAttributes redirectAttributes) {
        try {
            log.info("Updating client from {} to {}", originalEmail, clientDTO.getEmail());
            clientService.updateClientByEmail(originalEmail, clientDTO);
            return "redirect:/clients";
        } catch (Exception e) {
            log.error("Error updating client {}: {}", originalEmail, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "client.update.error");
            return "redirect:/clients/edit/" + originalEmail;
        }
    }

    @GetMapping("/delete/{email}")
    public String deleteClient(@PathVariable String email,
                               RedirectAttributes redirectAttributes) {
        try {
            log.info("Deleting client with email: {}", email);
            clientService.deleteClientByEmail(email);
            return "redirect:/clients";
        } catch (Exception e) {
            log.error("Error deleting client {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "client.delete.error");
            return "redirect:/clients";
        }
    }
}
