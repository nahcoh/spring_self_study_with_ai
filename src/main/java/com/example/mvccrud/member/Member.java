package com.example.mvccrud.member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Member {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String email;
    private int age;

    public Member(String name, String email, int age) {
        validateEmail(email);
        validateName(name);
        validateAge(age);

        this.name = name;
        this.email = email;
        this.age = age;
    }

    Member(Long id, String name, String email, int age) {
        validateName(name);
        validateEmail(email);
        validateAge(age);

        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }
    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수로 입력해야 합니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수로 입력해야 합니다.");
        }
    }

    private void validateAge(int age) {
        if (age <= 0) {
            throw new IllegalArgumentException("나이는 1 이상이어야 합니다.");
        }

    }
    public void changeName(String name) {
        validateName(name);
        this.name = name;
    }

    public void changeEmail(String email) {
        validateEmail(email);
        this.email = email;
    }

    public void changeAge(int age) {
        validateAge(age);
        this.age = age;
    }


}
