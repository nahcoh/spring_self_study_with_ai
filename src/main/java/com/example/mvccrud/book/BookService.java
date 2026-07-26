package com.example.mvccrud.book;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Book createBook(String title, int price) {
        Book book = new Book(title, price);
        return bookRepository.save(book);
    }

    public Book findBook(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(BookNotFoundException::new);
    }

    @Cacheable(value = "book", key = "#id")
    public BookResponse findBookResponse(Long id) {
        System.out.println("DB 조회 발생: book id = " + id);

        Book book = findBook(id);
        return new BookResponse(book);
    }

    public List<Book> findBooks() {
        return bookRepository.findAll();
    }

    public Page<Book> findBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @CacheEvict(value = "book", key = "#id")
    @Transactional
    public Book updateBook(Long id, String title, int price) {
        Book book = findBook(id);
        book.changeTitle(title);
        book.changePrice(price);
        return book;
    }

    @CacheEvict(value = "book", key = "#id")
    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException();
        }
        bookRepository.deleteById(id);
    }

    @CacheEvict(value = "book", key = "#id")
    @Transactional
    public Book patchBook(Long id, String title, Integer price) {
        Book book = findBook(id);

        if (title != null) {
            book.changeTitle(title);
        }
        if (price != null) {
            book.changePrice(price);
        }
        return book;
    }

    public List<Book> searchBooks(String title, Integer minPrice, Integer maxPrice) {
        return bookRepository.search(title, minPrice, maxPrice);
    }

    public Page<Book> searchBooks(String title, Integer minPrice, Integer maxPrice,
        Pageable pageable) {
        return bookRepository.search(title, minPrice, maxPrice, pageable);
    }


}
