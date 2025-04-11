package com.epam.rd.autocode.spring.project.exception;

public class InvalidEmailAddressException extends EmailSendingException {
    public InvalidEmailAddressException(String message) {
        super(message);
    }

    public InvalidEmailAddressException(String message, Throwable cause) {
        super(message, cause);
    }
}
