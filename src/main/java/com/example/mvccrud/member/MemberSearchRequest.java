package com.example.mvccrud.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSearchRequest {

    private String name;
    private String email;

}
