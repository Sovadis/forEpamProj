package com.epam.rd.autocode.spring.project.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundEx(NotFoundException e, Model model) {
        log.warn("NotFoundException caught: {}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage());
        return "error/not_found";
    }

    @ExceptionHandler(AlreadyExistException.class)
    public String handleAlreadyExistEx(AlreadyExistException e, Model model) {
        log.warn("AlreadyExistException caught: {}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage());
        return "error/already_exists";
    }

    @ExceptionHandler(Exception.class)
    public String handleOtherEx(Exception e, Model model) {
        log.error("Unexpected exception caught", e);
        model.addAttribute("errorMessage", "Внутренняя ошибка. Повторите попытку позже.");
        return "error/general";
    }
}
