package com.example.mvccrud.member;

import com.example.mvccrud.global.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String email;
    private int age;
    private String password;

    public Member(String name, String email, String password, int age) {
        validateEmail(email);
        validateName(name);
        validatePassword(password);
        validateAge(age);

        this.name = name;
        this.email = email;
        this.password = password;
        this.age = age;
    }

    Member(Long id, String name, String email, String password, int age) {
        validateName(name);
        validateEmail(email);
        validatePassword(password);
        validateAge(age);

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.age = age;
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

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
