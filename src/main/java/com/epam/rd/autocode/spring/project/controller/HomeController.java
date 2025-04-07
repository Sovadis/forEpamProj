package com.epam.rd.autocode.spring.project.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
public class HomeController {

    @GetMapping("/")
    public String home(RedirectAttributes redirectAttributes) {
        try {
            log.info("Entering HomeController.home()");
            return "home";
        } catch (Exception e) {
            log.error("Error in HomeController.home(): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "home.error");
            return "redirect:/";
        }
    }
}
