package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    @Email
    @NotBlank
    private String clientEmail;
    @Email
    private String employeeEmail;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime orderDate;
    @DecimalMin("0.0")
    private BigDecimal price;
    @NotEmpty
    private List<BookItemDTO> bookItems;

    private Boolean isConfirmed = false;

    public OrderDTO() {
    }

    public OrderDTO(String clientEmail, String employeeEmail,
                    LocalDateTime orderDate, BigDecimal price, List<BookItemDTO> bookItems) {
        this.clientEmail = clientEmail;
        this.employeeEmail = employeeEmail;
        this.orderDate = orderDate;
        this.price = price;
        this.bookItems = bookItems;
    }
}
