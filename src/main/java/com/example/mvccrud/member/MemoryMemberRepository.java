package com.example.mvccrud.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

//@Repository
public class MemoryMemberRepository implements MemberRepository{

    private final Map<Long, Member> store = new HashMap<>();
    private long sequence = 0L;


    @Override
    public Member save(Member member) {
        Long id = ++sequence;
        Member savedMember = new Member(
            id,
            member.getName(),
            member.getEmail(),
            member.getAge()
        );
        store.put(id, savedMember);
        return savedMember;
    }


    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return store.values().stream()
            .filter(member -> member.getEmail().equals(email))
            .findFirst();
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Member> search(String name, String email) {
        return store.values().stream()
            .filter(member -> name == null || name.isBlank() || member.getName().contains(name))
            .filter(member -> email == null || email.isBlank() || member.getEmail().contains(email))
            .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public void deleteById(Long id) {
        store.remove(id);
    }
}
