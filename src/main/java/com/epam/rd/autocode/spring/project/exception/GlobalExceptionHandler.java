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

    @ExceptionHandler(InvalidEmailAddressException.class)
    public String handleInvalidEmail(InvalidEmailAddressException e, Model model) {
        log.warn("InvalidEmailAddressException caught: {}", e.getMessage());
        model.addAttribute("errorMessage", "Невірний формат email адреси.");
        return "error/invalid_email";
    }

    @ExceptionHandler(EmailAuthenticationException.class)
    public String handleEmailAuthError(EmailAuthenticationException e, Model model) {
        log.error("EmailAuthenticationException caught: {}", e.getMessage());
        model.addAttribute("errorMessage", "Помилка аутентифікації поштового сервера. Будь ласка, зв'яжіться з адміністратором.");
        return "error/email_auth_error";
    }

    @ExceptionHandler(EmailTimeoutException.class)
    public String handleEmailTimeout(EmailTimeoutException e, Model model) {
        log.error("EmailTimeoutException caught: {}", e.getMessage());
        model.addAttribute("errorMessage", "Час очікування поштового сервера вичерпано. Спробуйте пізніше.");
        return "error/email_timeout";
    }

    @ExceptionHandler(EmailSendingException.class)
    public String handleEmailSending(EmailSendingException e, Model model) {
        log.error("EmailSendingException caught: {}", e.getMessage());
        model.addAttribute("errorMessage", "Не вдалося відправити лист. Будь ласка, спробуйте пізніше.");
        return "error/email_sending";
    }

    @ExceptionHandler(Exception.class)
    public String handleOtherEx(Exception e, Model model) {
        log.error("Unexpected exception caught", e);
        model.addAttribute("errorMessage", "Внутрішня помилка. Повторіть спробу пізніше.");
        return "error/general";
    }
}
