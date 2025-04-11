package com.epam.rd.autocode.spring.project.exception;

public class EmailAuthenticationException extends EmailSendingException {
    public EmailAuthenticationException(String message) {
        super(message);
    }

    public EmailAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
