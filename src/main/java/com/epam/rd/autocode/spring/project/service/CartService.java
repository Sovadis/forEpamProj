package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Cart;

public interface CartService {
    void addBookToCart(String bookName, Cart cart);

    BookItem findCartItemByBookName(Cart cart, String bookName);

    void removeBookFromCart(String bookName, Cart cart);
}
