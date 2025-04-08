package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.AlreadyExistException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.mapper.BookDTOMapper;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookDTOMapper bookDTOMapper;

    public BookServiceImpl(BookRepository bookRepository, BookDTOMapper bookDTOMapper) {
        this.bookRepository = bookRepository;
        this.bookDTOMapper = bookDTOMapper;
    }

    @Override
    public List<BookDTO> getAllBooks() {
        log.info("Retrieving all books");
        return bookRepository.findAll().stream()
                .map(bookDTOMapper::convertBookToBookDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookDTO getBookByName(String name) {
        log.info("Retrieving book by name: {}", name);
        if (name == null) throw new NotFoundException("Book's name == null");
        return bookRepository.findByName(name)
                .map(bookDTOMapper::convertBookToBookDTO)
                .orElseThrow(() -> {
                    log.warn("Book with name: {} was not found", name);
                    return new NotFoundException("Book was not found");
                });
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public BookDTO updateBookByName(String name, BookDTO bookDTO) {
        log.info("Updating book: {} to new name: {}", name, bookDTO.getName());
        Optional<Book> optional = bookRepository.findByName(name);
        if (optional.isEmpty()) {
            log.warn("Book {} not found", name);
            throw new NotFoundException("Book was not found");
        }

        if (bookDTO.getPublicationDate() != null && bookDTO.getPublicationDate().isAfter(LocalDate.now())) {
            log.warn("Publication date {} is later than today", bookDTO.getPublicationDate());
            throw new IllegalArgumentException("book.publicationDate.invalid");
        }


        Book existing = optional.get();

        existing.setName(bookDTO.getName());
        existing.setGenre(bookDTO.getGenre());
        existing.setAuthor(bookDTO.getAuthor());
        existing.setDescription(bookDTO.getDescription());
        existing.setCharacteristics(bookDTO.getCharacteristics());
        existing.setLanguage(bookDTO.getLanguage());
        existing.setPrice(bookDTO.getPrice());
        existing.setPages(bookDTO.getPages());
        existing.setAgeGroup(bookDTO.getAgeGroup());
        existing.setPublicationDate(bookDTO.getPublicationDate());

        Book saved = bookRepository.save(existing);
        log.info("Book {} updated successfully", saved.getName());
        return bookDTOMapper.convertBookToBookDTO(saved);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public void deleteBookByName(String name) {
        log.info("Deleting book with name: {}", name);
        bookRepository.findByName(name).ifPresent(bookRepository::delete);
        log.info("Book {} deleted", name);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public BookDTO addBook(BookDTO bookDTO) {
        log.info("Adding new book: {}", bookDTO.getName());
        if (bookRepository.findByName(bookDTO.getName()).isPresent()) {
            log.warn("Book {} already exists", bookDTO.getName());
            throw new AlreadyExistException("Book already exists");
        }

        if (bookDTO.getPublicationDate() != null && bookDTO.getPublicationDate().isAfter(LocalDate.now())) {
            log.warn("Publication date {} is later than today", bookDTO.getPublicationDate());
            throw new IllegalArgumentException("book.publicationDate.invalid");
        }

        Book savedBook = bookRepository.save(bookDTOMapper.convertBookDTOToBook(bookDTO));
        log.info("Book {} added successfully", savedBook.getName());
        return bookDTOMapper.convertBookToBookDTO(savedBook);
    }

    @Override
    public Page<BookDTO> searchBooks(String searchField, String searchValue, int page, int size, String sortField, String sortDir) {
        log.info("Searching books - field: {}, value: {}", searchField, searchValue);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir.toUpperCase()), sortField);
        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);
        Page<Book> bookPage;
        if (searchValue == null || searchValue.isBlank()) {
            bookPage = bookRepository.findAll(pageRequest);
        } else if ("name".equalsIgnoreCase(searchField)) {
            bookPage = bookRepository.findByNameContainingIgnoreCase(searchValue, pageRequest);
        } else if ("author".equalsIgnoreCase(searchField)) {
            bookPage = bookRepository.findByAuthorContainingIgnoreCase(searchValue, pageRequest);
        } else {
            bookPage = bookRepository.findAll(pageRequest);
        }
        return bookPage.map(bookDTOMapper::convertBookToBookDTO);
    }
}
