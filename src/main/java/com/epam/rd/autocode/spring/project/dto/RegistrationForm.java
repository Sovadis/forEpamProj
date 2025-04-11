package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegistrationForm {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 4, message = "{validation.password.size}")
    @Pattern(regexp = "^[A-Za-z0-9]+$",
            message = "{validation.password.chars}")
    private String password;

    @NotBlank(message = "{validation.password.required}")
    @Pattern(regexp = "^[A-Za-z0-9]+$",
            message = "{validation.password.chars}")
    private String confirmPassword;

    @NotBlank(message = "{validation.name.required}")
    private String name;
}
