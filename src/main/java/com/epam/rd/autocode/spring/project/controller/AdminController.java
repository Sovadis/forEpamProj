package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.service.AdminService;
import com.epam.rd.autocode.spring.project.service.ClientService;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final ClientService clientService;
    private final EmployeeService employeeService;

    public AdminController(AdminService adminService, ClientService clientService, EmployeeService employeeService) {
        this.adminService = adminService;
        this.clientService = clientService;
        this.employeeService = employeeService;
    }

    @GetMapping("/panel")
    public String adminPanel(Model model, RedirectAttributes redirectAttributes) {
        log.info("Accessing admin panel");
        try {
            List<ClientDTO> clients = adminService.getAllClients();
            List<EmployeeDTO> employees = adminService.getAllEmployees();
            model.addAttribute("clients", clients);
            model.addAttribute("employees", employees);
            return "admin_panel";
        } catch (Exception e) {
            log.error("Error accessing admin panel: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "admin.panel.error");
            return "redirect:/";
        }
    }

    @PostMapping("/updateRole")
    public String updateUserRole(@RequestParam("userId") Long userId,
                                 @RequestParam("userType") String userType,
                                 @RequestParam("newRole") String newRole,
                                 RedirectAttributes redirectAttributes) {
        log.info("Updating role for userId: {} type: {} to newRole: {}", userId, userType, newRole);
        try {
            adminService.updateUserRole(userId, userType, newRole);
        } catch (NotFoundException e) {
            log.error("User with id {} not found when updating role: {}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "user.not.found");
        } catch (IllegalArgumentException e) {
            log.error("Invalid role update for user id {}: {}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "role.update.invalid");
        } catch (Exception e) {
            log.error("Unexpected error while updating role for user id {}: {}", userId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "role.update.error");
        }
        return "redirect:/admin/panel";
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "searchField", required = false, defaultValue = "email") String searchField,
                         @RequestParam(name = "searchValue", required = false, defaultValue = "") String searchValue,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        log.info("Admin search requested: field={}, value={}", searchField, searchValue);
        try {
            List<ClientDTO> clients = adminService.searchClients(searchField, searchValue);
            List<EmployeeDTO> employees = adminService.searchEmployees(searchField, searchValue);
            model.addAttribute("clients", clients);
            model.addAttribute("employees", employees);
            model.addAttribute("searchField", searchField);
            model.addAttribute("searchValue", searchValue);
            return "admin_panel";
        } catch (Exception e) {
            log.error("Error during admin search: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "admin.search.error");
            return "redirect:/admin/panel";
        }
    }

    @PostMapping("/block")
    public String blockUser(@RequestParam("userType") String userType,
                            @RequestParam("email") String email,
                            @RequestParam("redirect") String redirect,
                            RedirectAttributes redirectAttributes) {
        try {
            if ("client".equalsIgnoreCase(userType)) {
                clientService.blockClient(email);
            } else if ("employee".equalsIgnoreCase(userType)) {
                employeeService.blockEmployee(email);
            }
            redirectAttributes.addFlashAttribute("message", "user.block.success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "user.block.error");
        }
        return "redirect:" + redirect;
    }

    @PostMapping("/unblock")
    public String unblockUser(@RequestParam("userType") String userType,
                              @RequestParam("email") String email,
                              @RequestParam("redirect") String redirect,
                              RedirectAttributes redirectAttributes) {
        try {
            if ("client".equalsIgnoreCase(userType)) {
                clientService.unblockClient(email);
            } else if ("employee".equalsIgnoreCase(userType)) {
                employeeService.unblockEmployee(email);
            }
            redirectAttributes.addFlashAttribute("message", "user.unblock.success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "user.unblock.error");
        }
        return "redirect:" + redirect;
    }


}

