package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class BookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    BookRepository bookRepository;

    @BeforeEach
    void cleanUp() {
        bookRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testAddBook() throws Exception {
        mockMvc.perform(post("/books/add")
                        .param("name", "Моя новая книга")
                        .param("author", "Иванов")
                        .param("price", "199.99")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        Book book = bookRepository.findByName("Моя новая книга").orElse(null);
        assertThat(book).isNotNull();
        assertThat(book.getAuthor()).isEqualTo("Иванов");
        assertThat(book.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(199.99));
    }
}
