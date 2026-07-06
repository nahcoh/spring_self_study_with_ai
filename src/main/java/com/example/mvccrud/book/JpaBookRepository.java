package com.example.mvccrud.book;


import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaBookRepository extends JpaRepository<Book, Long> {


    @Query("""
        select b from Book b
        where (:title is null or :title = '' or b.title like concat('%', :title, '%'))
        and (:minPrice is null or b.price >= :minPrice)
        and (:maxPrice is null or b.price <= :maxPrice)
        """)
    List<Book> search(
        @Param("title") String title,
        @Param("minPrice") Integer minPrice,
        @Param("maxPrice") Integer maxPrice);

}
