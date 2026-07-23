package com.example.mvccrud.global;

public class InvalidSortException extends RuntimeException{

    public InvalidSortException(String field) {
        super("정렬할 수 없는 필드입니다: " + field);
    }

}
