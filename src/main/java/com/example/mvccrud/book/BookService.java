package com.example.mvccrud.book;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book createBook(String title, int price) {
        Book book = new Book(null, title, price);
        return bookRepository.save(book);
    }

    public Book findBook(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(BookNotFoundException::new);
    }

    public List<Book> findBooks() {
        return bookRepository.findAll();
    }

    public void updateBook(Long id, String title, int price) {
        Book book = findBook(id);
        book.changeTitle(title);
        book.changePrice(price);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException();
        }
        bookRepository.deleteById(id);
    }

}
