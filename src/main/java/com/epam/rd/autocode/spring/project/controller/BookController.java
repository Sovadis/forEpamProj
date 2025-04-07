package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
@Slf4j
public class BookController {
    private final BookServiceImpl bookService;

    public BookController(BookServiceImpl bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String getBooks(
            @RequestParam(name = "searchField", required = false) String searchField,
            @RequestParam(name = "searchValue", required = false) String searchValue,
            @RequestParam(name = "sortField", defaultValue = "name") String sortField,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            log.info("Fetching books with searchField={}, searchValue={}, sortField={}, sortDir={}, page={}, size={}",
                    searchField, searchValue, sortField, sortDir, page, size);
            page = Math.max(page, 0);
            Page<BookDTO> booksPage = bookService.searchBooks(searchField, searchValue, page, size, sortField, sortDir);
            model.addAttribute("page", booksPage);
            model.addAttribute("books", booksPage.getContent());
            model.addAttribute("searchField", searchField);
            model.addAttribute("searchValue", searchValue);
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("size", size);
            model.addAttribute("currentPage", page);
            return "books";
        } catch (Exception e) {
            log.error("Error fetching books: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "books.fetch.error");
            return "redirect:/books";
        }
    }

    @GetMapping("/new")
    public String showAddBookForm(Model model, RedirectAttributes redirectAttributes) {
        try {
            log.info("Displaying add book form");
            model.addAttribute("book", new BookDTO());
            return "book_form";
        } catch (Exception e) {
            log.error("Error displaying add book form: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "book.form.error");
            return "redirect:/books";
        }
    }

    @PostMapping("/add")
    public String addBook(@ModelAttribute("book") BookDTO bookDTO,
                          RedirectAttributes redirectAttributes) {
        try {
            log.info("Adding new book: {}", bookDTO.getName());
            bookService.addBook(bookDTO);
            return "redirect:/books";
        } catch (IllegalArgumentException e) {
            log.error("Validation error adding book: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/books/new";
        } catch (Exception e) {
            log.error("Error adding book: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "book.add.error");
            return "redirect:/books/new";
        }
    }

    @GetMapping("/edit/{name}")
    public String showEditBookForm(@PathVariable String name,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            log.info("Editing book with name: {}", name);
            BookDTO book = bookService.getBookByName(name);
            if (book == null) {
                log.warn("Book {} not found", name);
                redirectAttributes.addFlashAttribute("errorMessage", "book.not.found");
                return "redirect:/books";
            }
            model.addAttribute("book", book);
            return "book_form";
        } catch (Exception e) {
            log.error("Error editing book with name {}: {}", name, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "book.edit.error");
            return "redirect:/books";
        }
    }

    @PostMapping("/edit")
    public String updateBook(@RequestParam("originalName") String originalName,
                             @ModelAttribute("book") BookDTO bookDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            log.info("Updating book from {} to {}", originalName, bookDTO.getName());
            bookService.updateBookByName(originalName, bookDTO);
            return "redirect:/books";
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating book: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/books/edit/" + originalName;
        } catch (Exception e) {
            log.error("Error updating book: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "book.update.error");
            return "redirect:/books/edit/" + originalName;
        }
    }

    @GetMapping("/delete/{name}")
    public String deleteBook(@PathVariable String name,
                             RedirectAttributes redirectAttributes) {
        try {
            log.info("Deleting book with name: {}", name);
            bookService.deleteBookByName(name);
            return "redirect:/books";
        } catch (Exception e) {
            log.error("Error deleting book {}: {}", name, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "book.delete.error");
            return "redirect:/books";
        }
    }
}

