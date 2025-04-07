package com.epam.rd.autocode.spring.project.model;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    private Map<BookItem, Integer> items = new HashMap<>();

    public Map<BookItem, Integer> getItems() {
        return items;
    }

    public void setItems(Map<BookItem, Integer> items) {
        this.items = items;
    }

    public void addBook(BookItem bookItem) {
        items.put(bookItem, items.getOrDefault(bookItem, 0) + 1);
    }

    public void removeBook(BookItem bookItem) {
        items.remove(bookItem);
    }

    public void clearCart() {
        items.clear();
    }
}
