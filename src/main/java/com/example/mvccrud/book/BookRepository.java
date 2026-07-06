package com.example.mvccrud.book;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepository {

    Book save(Book book);

    Optional<Book> findById(Long id);

    List<Book> findAll();

    Page<Book> findAll(Pageable pageable);

    List<Book> search(String title, Integer minPrice, Integer maxPrice);

    void deleteById(Long id);

    boolean existsById(Long id);

}
