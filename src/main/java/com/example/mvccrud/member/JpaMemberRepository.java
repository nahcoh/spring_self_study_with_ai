package com.example.mvccrud.member;

import com.example.mvccrud.book.JpaBookRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaMemberRepository extends JpaRepository<Member, Long>, MemberRepository {

    @Override
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
select m from Member m
where (:name is null or :name = '' or m.name like concat('%', :name, '%') )
and (:email is null or :email = '' or m.email like concat('%', :email, '%'))
""")
    List<Member> search(
        @Param("name") String name,
        @Param("email") String email
    );

    @Query("""
        select m from Member m
        where (:name is null or :name = '' or m.name like concat('%', :name, '%'))
        and (:email is null or :email = '' or m.email like concat('%', :email, '%'))
        """)
    Page<Member> search(
        @Param("name") String name,
        @Param("email") String email,
        Pageable pageable
    );
}
