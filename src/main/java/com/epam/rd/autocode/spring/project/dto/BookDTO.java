package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookDTO {
    @NotBlank(message = "{validation.required}")
    private String name;
    private String genre;
    private AgeGroup ageGroup;
    @DecimalMin(value = "0.0", message = "{validation.price.min}")
    private BigDecimal price;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;
    private String author;
    @Min(value = 1, message = "{validation.pages.min}")
    private Integer pages;
    private String characteristics;
    private String description;
    private Language language;

    public BookDTO() {
    }

    public BookDTO(
            String name, String genre, AgeGroup ageGroup,
            BigDecimal price, LocalDate publicationDate,
            String author, Integer pages, String characteristics,
            String description, Language language
    ) {
        this.name = name;
        this.genre = genre;
        this.ageGroup = ageGroup;
        this.price = price;
        this.publicationDate = publicationDate;
        this.author = author;
        this.pages = pages;
        this.characteristics = characteristics;
        this.description = description;
        this.language = language;
    }
}
