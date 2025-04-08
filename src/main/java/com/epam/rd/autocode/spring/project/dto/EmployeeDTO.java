package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class EmployeeDTO {
    private Long id;
    @Email(message = "{validation.email}")
    @NotBlank(message = "{validation.required}")
    private String email;

    private String password;

    @NotBlank(message = "{validation.required}")
    private String name;

    private String phone;

    @NotNull(message = "{validation.required}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "{validation.birthDate.past}")
    private LocalDate birthDate;

    private Role role;

    private boolean blocked;

    public EmployeeDTO() {
    }

    public EmployeeDTO(String email, String password, String name, String phone, LocalDate birthDate) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
    }
}
