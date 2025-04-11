package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResetPasswordForm {
    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 4, message = "{validation.password.size}")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "{validation.password.chars}")
    private String password;

    @NotBlank(message = "{validation.password.required}")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "{validation.password.chars}")
    private String confirmPassword;

    @AssertTrue(message = "{validation.password.confirmation}")
    public boolean isPasswordsMatch() {
        return password != null && password.equals(confirmPassword);
    }
}
