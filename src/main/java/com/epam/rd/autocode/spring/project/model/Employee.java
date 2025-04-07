package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "EMPLOYEES")
public class Employee extends User {
    @Column(name = "phone")
    private String phone;

    @Column(name = "birth_Date")
    private LocalDate birthDate;

    public Employee() {
    }

    public Employee(Long id, String email, String password, String name, String phone, LocalDate birthDate) {
        super(id, email, password, name);
        this.phone = phone;
        this.birthDate = birthDate;
    }
}
