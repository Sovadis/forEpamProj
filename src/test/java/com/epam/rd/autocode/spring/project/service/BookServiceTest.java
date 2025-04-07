package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BookServiceTest {

    @Autowired
    private BookService bookService;
    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void cleanDB() {
        bookRepository.deleteAll();
    }

    @Test
    void getAllBooks_ShouldReturnAllBookDTOs() {
        Book b1 = new Book();
        b1.setName("Alpha");
        b1.setPrice(BigDecimal.valueOf(10));
        bookRepository.save(b1);
        Book b2 = new Book();
        b2.setName("Beta");
        b2.setPrice(BigDecimal.valueOf(20));
        bookRepository.save(b2);

        List<BookDTO> books = bookService.getAllBooks();

        assertEquals(2, books.size());
        List<String> names = books.stream().map(BookDTO::getName).toList();
        assertTrue(names.contains("Alpha"));
        assertTrue(names.contains("Beta"));
    }

    @Test
    void getBookByName_WhenExists_ShouldReturnDTO() {
        Book book = new Book();
        book.setName("Gamma");
        book.setPrice(BigDecimal.valueOf(30));
        bookRepository.save(book);

        BookDTO dto = bookService.getBookByName("Gamma");

        assertNotNull(dto);
        assertEquals("Gamma", dto.getName());
        assertEquals(0, BigDecimal.valueOf(30).compareTo(dto.getPrice()));
    }

    @Test
    void getBookByName_WhenNotFound_ShouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                bookService.getBookByName("NonexistentBook"));
        assertEquals("Book was not found", ex.getMessage());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void updateBookByName_WithAllowedRole_ShouldUpdateBook() {
        Book book = new Book();
        book.setName("Delta");
        book.setPrice(BigDecimal.valueOf(50));
        bookRepository.save(book);

        BookDTO updateDto = new BookDTO();
        updateDto.setName("Delta");
        updateDto.setPrice(BigDecimal.valueOf(75));

        BookDTO updatedDto = bookService.updateBookByName("Delta", updateDto);

        assertEquals("Delta", updatedDto.getName());
        assertEquals(0, BigDecimal.valueOf(75).compareTo(updatedDto.getPrice()));
        Book updatedEntity = bookRepository.findByName("Delta").orElseThrow();
        assertEquals(0, BigDecimal.valueOf(75).compareTo(updatedEntity.getPrice()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBookByName_WhenNotFound_ShouldThrowNotFoundException() {
        BookDTO updateDto = new BookDTO();
        updateDto.setName("NoBook");
        updateDto.setPrice(BigDecimal.TEN);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                bookService.updateBookByName("NoBook", updateDto));
        assertEquals("Book was not found", ex.getMessage());
    }

    @Test
    void updateBookByName_WithoutAuth_ShouldThrowAccessDenied() {
        Book book = new Book();
        book.setName("Epsilon");
        book.setPrice(BigDecimal.ONE);
        bookRepository.save(book);
        assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
                bookService.updateBookByName("Epsilon", new BookDTO()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void deleteBookByName_WithAllowedRole_ShouldDeleteBook() {
        Book book = new Book();
        book.setName("Zeta");
        book.setPrice(BigDecimal.TEN);
        bookRepository.save(book);
        assertTrue(bookRepository.findByName("Zeta").isPresent());

        bookService.deleteBookByName("Zeta");

        assertFalse(bookRepository.findByName("Zeta").isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBookByName_WhenNotExist_ShouldDoNothing() {
        bookService.deleteBookByName("NoSuchBook");
        assertEquals(0, bookRepository.count());
    }

    @Test
    void deleteBookByName_WithoutAuth_ShouldThrowAccessDenied() {
        Book book = new Book();
        book.setName("Eta");
        book.setPrice(BigDecimal.ONE);
        bookRepository.save(book);
        assertThrows(AuthenticationCredentialsNotFoundException.class, () ->
                bookService.deleteBookByName("Eta"));
        assertTrue(bookRepository.findByName("Eta").isPresent());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLOYEE"})
    void addBook_WithAllowedRole_ShouldSaveBook() {
        BookDTO newBook = new BookDTO();
        newBook.setName("Theta");
        newBook.setPrice(BigDecimal.valueOf(99.99));

        BookDTO savedDto = bookService.addBook(newBook);

        assertEquals("Theta", savedDto.getName());
        assertEquals(0, BigDecimal.valueOf(99.99).compareTo(savedDto.getPrice()));
        Book savedEntity = bookRepository.findByName("Theta").orElseThrow();
        assertEquals("Theta", savedEntity.getName());
        assertEquals(0, BigDecimal.valueOf(99.99).compareTo(savedEntity.getPrice()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addBook_WhenNameExists_ShouldThrowAlreadyExistException() {
        Book book = new Book();
        book.setName("Lambda");
        book.setPrice(BigDecimal.TEN);
        bookRepository.save(book);

        BookDTO dup = new BookDTO();
        dup.setName("Lambda");
        dup.setPrice(BigDecimal.ONE);

        AlreadyExistException ex = assertThrows(AlreadyExistException.class, () ->
                bookService.addBook(dup));
        assertTrue(ex.getMessage().contains("already exists"));
        assertEquals(1, bookRepository.count());
    }

    @Test
    void addBook_WithoutAuth_ShouldThrowAccessDenied() {
        BookDTO book = new BookDTO();
        book.setName("Mu");
        book.setPrice(BigDecimal.TEN);
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> bookService.addBook(book));
        assertFalse(bookRepository.findByName("Mu").isPresent());
    }

    @Test
    void searchBooks_ByName_ShouldReturnMatchingBooks() {
        Book b1 = new Book();
        b1.setName("Java 101");
        b1.setPrice(BigDecimal.TEN);
        bookRepository.save(b1);
        Book b2 = new Book();
        b2.setName("Java for Experts");
        b2.setPrice(BigDecimal.ONE);
        bookRepository.save(b2);
        Book b3 = new Book();
        b3.setName("Spring Framework");
        b3.setPrice(BigDecimal.TEN);
        bookRepository.save(b3);

        Page<BookDTO> page = bookService.searchBooks("name", "Java", 1, 10, "name", "asc");

        List<BookDTO> results = page.getContent();
        assertEquals(2, results.size());
        List<String> names = results.stream().map(BookDTO::getName).toList();
        assertTrue(names.contains("Java 101"));
        assertTrue(names.contains("Java for Experts"));
        assertFalse(names.contains("Spring Framework"));
    }

    @Test
    void searchBooks_WhenValueBlank_ShouldReturnAll() {
        Book b1 = new Book();
        b1.setName("BookA");
        b1.setPrice(BigDecimal.ONE);
        bookRepository.save(b1);
        Book b2 = new Book();
        b2.setName("BookB");
        b2.setPrice(BigDecimal.TEN);
        bookRepository.save(b2);
        Page<BookDTO> page = bookService.searchBooks("anything", "", 1, 5, "name", "asc");
        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }
}
