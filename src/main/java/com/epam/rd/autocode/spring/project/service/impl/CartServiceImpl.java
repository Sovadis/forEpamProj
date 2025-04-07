package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Cart;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    private final BookRepository bookRepository;

    public CartServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void addBookToCart(String bookName, Cart cart) {
        log.info("Adding book {} to cart", bookName);
        Optional<Book> bookOpt = bookRepository.findByName(bookName);
        if (bookOpt.isEmpty()) {
            log.warn("Book {} not found", bookName);
            throw new NotFoundException("Book is not found");
        }
        Book book = bookOpt.get();
        BookItem existingItem = findCartItemByBookName(cart, bookName);
        if (existingItem != null) {
            int currentQty = cart.getItems().get(existingItem);
            int newQty = currentQty + 1;
            cart.getItems().put(existingItem, newQty);
            existingItem.setQuantity(newQty);
            log.debug("Incremented quantity for book {} in cart", bookName);
        } else {
            BookItem newItem = new BookItem();
            newItem.setBook(book);
            newItem.setQuantity(1);

            cart.getItems().put(newItem, 1);
            log.debug("Added new book {} to cart", bookName);
        }
    }

    @Override
    public BookItem findCartItemByBookName(Cart cart, String bookName) {
        log.info("Looking for cart item by book name: {}", bookName);
        for (BookItem item : cart.getItems().keySet()) {
            if (item.getBook().getName().equals(bookName)) {
                log.info("Returning cart item by book name: {}", bookName);
                return item;
            }
        }
        log.warn("Book item in cart by name '{}' was not found (returning null)", bookName);
        return null;
    }

    @Override
    public void removeBookFromCart(String bookName, Cart cart) {
        log.info("Removing book {} from cart", bookName);
        BookItem itemToRemove = findCartItemByBookName(cart, bookName);
        if (itemToRemove != null) {
            cart.getItems().remove(itemToRemove);
            log.debug("Removed book {} from cart", bookName);
        }
    }
}
