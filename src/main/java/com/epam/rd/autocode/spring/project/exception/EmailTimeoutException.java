package com.epam.rd.autocode.spring.project.exception;

public class EmailTimeoutException extends EmailSendingException {
    public EmailTimeoutException(String message) {
        super(message);
    }

    public EmailTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
