package com.epam.rd.autocode.spring.project.mapper;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookDTOMapper {

    private ModelMapper mapper;

    public BookDTOMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public BookDTO convertBookToBookDTO(Book book) {
        log.debug("Mapping Book to BookDTO for book: {}", book.getName());
        return mapper.map(book, BookDTO.class);
    }

    public Book convertBookDTOToBook(BookDTO bookDTO) {
        log.debug("Mapping BookDTO to Book for book: {}", bookDTO.getName());
        return mapper.map(bookDTO, Book.class);
    }
}
