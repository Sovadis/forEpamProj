package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ClientDTO {
    private Long id;
    @Email(message = "{validation.email}")
    @NotBlank(message = "{validation.required}")
    private String email;

    @NotBlank(message = "{validation.required}")
    @Size(min = 4, max = 100, message = "{validation.password.size}")
    private String password;

    @NotBlank(message = "{validation.required}")
    private String name;

    @DecimalMin(value = "0.0", message = "{validation.balance.min}")
    private BigDecimal balance;

    private Role role;

    private boolean blocked;


    public ClientDTO() {
    }

    public ClientDTO(String email, String password, String name, BigDecimal balance) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.balance = balance;
    }
}
