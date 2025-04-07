package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Cart;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CartServiceTest {

    @Autowired
    private CartService cartService;
    @Autowired
    private BookRepository bookRepository;

    private Cart cart;

    @BeforeEach
    void init() {
        bookRepository.deleteAll();
        cart = new Cart();
        cart.setItems(new HashMap<>());
    }

    @Test
    void addBookToCart_WhenBookExistsAndNotInCart_ShouldAddNewItem() {
        Book book = new Book();
        book.setName("CartBook1");
        book.setPrice(BigDecimal.TEN);
        bookRepository.save(book);
        assertTrue(cart.getItems().isEmpty());

        cartService.addBookToCart("CartBook1", cart);

        assertEquals(1, cart.getItems().size());
        BookItem item = cart.getItems().keySet().iterator().next();
        assertEquals("CartBook1", item.getBook().getName());
        assertEquals(1, item.getQuantity());
        assertEquals(1, cart.getItems().get(item).intValue(), "Map value should be 1");
    }

    @Test
    void addBookToCart_WhenBookAlreadyInCart_ShouldIncrementQuantity() {
        Book book = new Book();
        book.setName("CartBook2");
        book.setPrice(BigDecimal.ONE);
        bookRepository.save(book);
        BookItem existingItem = new BookItem();
        existingItem.setBook(book);
        existingItem.setQuantity(1);
        cart.getItems().put(existingItem, 1);

        cartService.addBookToCart("CartBook2", cart);

        assertEquals(1, cart.getItems().size());
        BookItem item = cart.getItems().keySet().iterator().next();
        assertEquals("CartBook2", item.getBook().getName());
        assertEquals(2, item.getQuantity());
        assertEquals(2, cart.getItems().get(item).intValue());
    }

    @Test
    void addBookToCart_WhenBookNotExists_ShouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                cartService.addBookToCart("NoSuchBook", cart));
        assertEquals("Book is not found", ex.getMessage());
        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void findCartItemByBookName_WhenItemExists_ShouldReturnItem() {
        Book book = new Book();
        book.setName("FindMe");
        book.setPrice(BigDecimal.TEN);
        BookItem item = new BookItem();
        item.setBook(book);
        item.setQuantity(1);
        cart.getItems().put(item, 1);

        BookItem found = cartService.findCartItemByBookName(cart, "FindMe");

        assertNotNull(found);
        assertEquals("FindMe", found.getBook().getName());
    }

    @Test
    void findCartItemByBookName_WhenItemNotInCart_ShouldReturnNull() {
        Book book = new Book();
        book.setName("OtherBook");
        cart.getItems().put(new BookItem(null, null, book, 1), 1);

        BookItem result = cartService.findCartItemByBookName(cart, "MissingBook");

        assertNull(result, "Should return null if book not in cart");
    }

    @Test
    void removeBookFromCart_WhenItemExists_ShouldRemoveIt() {
        Book book = new Book();
        book.setName("RemovableBook");
        BookItem item = new BookItem();
        item.setBook(book);
        item.setQuantity(3);
        cart.getItems().put(item, 3);
        assertEquals(1, cart.getItems().size());

        cartService.removeBookFromCart("RemovableBook", cart);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void removeBookFromCart_WhenItemNotInCart_ShouldDoNothing() {
        Book book = new Book();
        book.setName("SomethingElse");
        cart.getItems().put(new BookItem(null, null, book, 1), 1);

        cartService.removeBookFromCart("NotInCart", cart);

        assertEquals(1, cart.getItems().size());
    }
}
