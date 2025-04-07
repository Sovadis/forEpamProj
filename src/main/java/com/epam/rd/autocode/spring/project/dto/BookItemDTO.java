package com.epam.rd.autocode.spring.project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookItemDTO {
    @NotBlank
    private String bookName;
    @Min(1)
    private Integer quantity;

    public BookItemDTO() {
    }

    public BookItemDTO(String bookName, Integer quantity) {
        this.bookName = bookName;
        this.quantity = quantity;
    }


}
