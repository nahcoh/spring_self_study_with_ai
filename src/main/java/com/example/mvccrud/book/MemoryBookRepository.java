package com.example.mvccrud.book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.swing.text.html.Option;
import org.springframework.stereotype.Repository;

//@Repository
public class MemoryBookRepository implements BookRepository {

    private final Map<Long, Book> store = new HashMap<>();
    private long sequence = 0L;


    @Override
    public Book save(Book book) {
        Long id = ++sequence;
        Book savedBook = new Book(id, book.getTitle(), book.getPrice());
        store.put(id, savedBook);
        return savedBook;
    }

    @Override
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public List<Book> search(String title, Integer minPrice, Integer maxPrice) {
        return store.values().stream()
            .filter(book -> title == null || title.isBlank() || book.getTitle().contains(title))
            .filter(book -> minPrice == null || book.getPrice() >= minPrice)
            .filter(book -> maxPrice == null || book.getPrice() <= maxPrice)
            .toList();
    }

}
