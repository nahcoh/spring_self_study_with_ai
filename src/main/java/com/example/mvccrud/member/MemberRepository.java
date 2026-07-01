package com.example.mvccrud.member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Member save(Member member);

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);

    List<Member> findAll();

    List<Member> search(String name, String email);

    boolean existsById(Long id);

    boolean existsByEmail(String email);

    void deleteById(Long id);


}
