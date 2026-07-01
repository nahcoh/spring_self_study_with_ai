package com.example.mvccrud.member;

import lombok.Getter;

@Getter
public class Member {

    private final Long id;
    private String name;
    private String email;
    private int age;

    public Member(Long id, String name, String email, int age) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수로 입력해야 합니다.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수로 입력해야 합니다.");
        }
        if (age <= 0) {
            throw new IllegalArgumentException("나이는 1 이상이어야 합니다.");
        }

        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public void changeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수로 입력해야 합니다.");
        }
        this.name = name;
    }

    public void changeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수로 입력해야 합니다.");
        }
        this.email = email;
    }

    public void changeAge(int age) {
        if (age <= 0) {
            throw new IllegalArgumentException("나이는 1 이상이어야 합니다.");
        }
        this.age = age;
    }


}
