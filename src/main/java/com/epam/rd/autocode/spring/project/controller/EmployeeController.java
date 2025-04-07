package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.EmployeeDTO;
import com.epam.rd.autocode.spring.project.service.EmployeeService;
import com.epam.rd.autocode.spring.project.service.impl.EmployeeServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employees")
@Slf4j
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeServiceImpl employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public String getAllEmployees(
            @RequestParam(name = "searchField", required = false) String searchField,
            @RequestParam(name = "searchValue", required = false) String searchValue,
            @RequestParam(name = "sortField", defaultValue = "name") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Page<EmployeeDTO> employeePage = employeeService.searchEmployees(searchField, searchValue, page, size, sortField, sortDir);
            model.addAttribute("page", employeePage);
            model.addAttribute("employees", employeePage.getContent());
            model.addAttribute("searchField", searchField);
            model.addAttribute("searchValue", searchValue);
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("size", size);
            model.addAttribute("currentPage", page);
            return "employees";
        } catch (Exception e) {
            log.error("Error fetching employees: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "employees.fetch.error");
            return "redirect:/employees";
        }
    }

    @GetMapping("/{email}")
    public String getEmployeeByEmail(@PathVariable String email, Model model, RedirectAttributes redirectAttributes) {
        try {
            log.info("Fetching employee with email: {}", email);
            EmployeeDTO employee = employeeService.getEmployeeByEmail(email);
            if (employee == null) {
                log.warn("Employee with email {} not found", email);
                redirectAttributes.addFlashAttribute("errorMessage", "employee.not.found");
                return "redirect:/employees";
            }
            model.addAttribute("employees", List.of(employee));
            return "employees";
        } catch (Exception e) {
            log.error("Error fetching employee {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "employee.fetch.error");
            return "redirect:/employees";
        }
    }

    @GetMapping("/new")
    public String showAddEmployeeForm(Model model, RedirectAttributes redirectAttributes) {
        try {
            log.info("Displaying add employee form");
            model.addAttribute("employee", new EmployeeDTO());
            return "employee_form";
        } catch (Exception e) {
            log.error("Error displaying add employee form: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "employee.form.error");
            return "redirect:/employees";
        }
    }

    @PostMapping("/add")
    public String addEmployee(@ModelAttribute("employee") @Valid EmployeeDTO employeeDTO,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "employee_form";
        }
        try {
            log.info("Adding new employee with email: {}", employeeDTO.getEmail());
            employeeService.addEmployee(employeeDTO);
            return "redirect:/employees";
        } catch (IllegalArgumentException e) {
            log.error("Invalid employee data: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "password.too.short");
            return "redirect:/employees/new";
        } catch (Exception e) {
            log.error("Error adding employee: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "employee.add.error");
            return "redirect:/employees/new";
        }
    }

    @GetMapping("/edit/{email}")
    public String showEditEmployeeForm(@PathVariable String email,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            log.info("Editing employee with email: {}", email);
            EmployeeDTO employee = employeeService.getEmployeeByEmail(email);
            if (employee == null) {
                log.warn("Employee with email {} not found", email);
                redirectAttributes.addFlashAttribute("errorMessage", "employee.not.found");
                return "redirect:/employees";
            }
            model.addAttribute("employee", employee);
            return "employee_form";
        } catch (Exception e) {
            log.error("Error displaying edit form for employee {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "employee.edit.form.error");
            return "redirect:/employees";
        }
    }

    @PostMapping("/edit")
    public String updateEmployee(@RequestParam("originalEmail") String originalEmail,
                                 @ModelAttribute("employee") @Valid EmployeeDTO employeeDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "employee_form";
        }
        try {
            log.info("Updating employee from {} to {}", originalEmail, employeeDTO.getEmail());
            employeeService.updateEmployeeByEmail(originalEmail, employeeDTO);
            return "redirect:/employees";
        } catch (IllegalArgumentException e) {
            log.error("Invalid employee data: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "password.too.short");
            return "redirect:/employees/new";
        } catch (Exception e) {
            log.error("Error updating employee {}: {}", originalEmail, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "employee.update.error");
            return "redirect:/employees/edit/" + originalEmail;
        }
    }

    @GetMapping("/delete/{email}")
    public String deleteEmployee(@PathVariable String email,
                                 RedirectAttributes redirectAttributes) {
        try {
            log.info("Deleting employee with email: {}", email);
            employeeService.deleteEmployeeByEmail(email);
            return "redirect:/employees";
        } catch (Exception e) {
            log.error("Error deleting employee {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "employee.delete.error");
            return "redirect:/employees";
        }
    }
}

