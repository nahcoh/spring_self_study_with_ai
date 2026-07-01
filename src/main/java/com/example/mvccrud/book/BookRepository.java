package com.example.mvccrud.book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    Book save(Book book);

    Optional<Book> findById(Long id);

    List<Book> findAll();

    List<Book> search(String title, Integer minPrice, Integer maxPrice);

    void deleteById(Long id);

    boolean existsById(Long id);

}
