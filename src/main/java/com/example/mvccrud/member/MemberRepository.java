package com.example.mvccrud.member;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);

    List<Member> findAll();

    Page<Member> findAll(Pageable pageable);

    List<Member> search(String name, String email);

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    void deleteById(Long id);


}
