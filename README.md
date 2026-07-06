# Spring MVC CRUD Practice

## 1. 프로젝트 목표

이 프로젝트는 JPA 없이 메모리 저장소를 사용해서 Spring MVC 기반 REST API 구조를 연습하는 프로젝트다.

목표는 다음과 같다.

* Controller, Service, Repository 계층 구조 이해
* REST API 요청/응답 흐름 이해
* Request DTO, Response DTO 분리
* Validation 적용
* Global Exception Handling 적용
* HTTP Status Code 정리
* PUT과 PATCH 차이 이해
* Service Test, Controller Test 작성
* 같은 구조를 여러 도메인에 반복 적용하기

---

## 2. 기술 스택

* Java 17
* Spring Boot
* Spring Web
* Validation
* Lombok
* JUnit 5
* AssertJ
* MockMvc

---

## 3. 패키지 구조

```text
com.example.mvccrud
 ├── book
 │   ├── Book
 │   ├── BookController
 │   ├── BookService
 │   ├── BookRepository
 │   ├── MemoryBookRepository
 │   ├── BookCreateRequest
 │   ├── BookUpdateRequest
 │   ├── BookPatchRequest
 │   ├── BookSearchRequest
 │   ├── BookResponse
 │   └── BookNotFoundException
 │
 ├── member
 │   ├── Member
 │   ├── MemberController
 │   ├── MemberService
 │   ├── MemberRepository
 │   ├── MemoryMemberRepository
 │   ├── MemberCreateRequest
 │   ├── MemberUpdateRequest
 │   ├── MemberPatchRequest
 │   ├── MemberSearchRequest
 │   ├── MemberResponse
 │   ├── MemberNotFoundException
 │   └── DuplicateEmailException
 │
 └── global
     ├── ApiResponse
     ├── ErrorResponse
     └── GlobalExceptionHandler
```

---

# Book API

## 4. Book 도메인

`Book`은 책 정보를 표현하는 도메인 객체다.

필드:

```text
id
title
price
```

주요 기능:

```text
changeTitle()
changePrice()
```

검증 조건:

* 제목은 필수
* 가격은 1원 이상

---

## 5. Book API 목록

### 책 등록

```http
POST /books
```

요청 예시:

```json
{
  "title": "데미안",
  "price": 15000
}
```

응답:

```http
201 Created
```

---

### 책 단건 조회

```http
GET /books/{id}
```

응답 예시:

```json
{
  "data": {
    "id": 1,
    "title": "데미안",
    "price": 15000
  }
}
```

---

### 책 전체 조회

```http
GET /books
```

응답 예시:

```json
{
  "data": [
    {
      "id": 1,
      "title": "데미안",
      "price": 15000
    }
  ]
}
```

---

### 책 전체 수정

```http
PUT /books/{id}
```

요청 예시:

```json
{
  "title": "수정된 데미안",
  "price": 20000
}
```

PUT은 수정 가능한 값을 모두 보내는 전체 수정에 가깝다.

---

### 책 부분 수정

```http
PATCH /books/{id}
```

요청 예시:

```json
{
  "title": "제목만 수정"
}
```

또는:

```json
{
  "price": 30000
}
```

PATCH는 일부 필드만 수정할 때 사용한다.

---

### 책 삭제

```http
DELETE /books/{id}
```

응답:

```http
204 No Content
```

---

### 책 검색

```http
GET /books/search?title=자바
GET /books/search?minPrice=10000&maxPrice=30000
GET /books/search?title=자바&minPrice=10000&maxPrice=30000
```

검색 조건은 없으면 무시하고, 있으면 해당 조건에 맞는 책만 반환한다.

---

# Member API

## 6. Member 도메인

`Member`는 회원 정보를 표현하는 도메인 객체다.

필드:

```text
id
name
email
age
```

주요 기능:

```text
changeName()
changeEmail()
changeAge()
```

검증 조건:

* 이름은 필수
* 이메일은 필수
* 나이는 1 이상
* 이메일은 중복될 수 없음

---

## 7. Member API 목록

### 회원 등록

```http
POST /members
```

요청 예시:

```json
{
  "name": "김철수",
  "email": "kim@test.com",
  "age": 30
}
```

응답:

```http
201 Created
```

---

### 회원 단건 조회

```http
GET /members/{id}
```

응답 예시:

```json
{
  "data": {
    "id": 1,
    "name": "김철수",
    "email": "kim@test.com",
    "age": 30
  }
}
```

---

### 회원 전체 조회

```http
GET /members
```

응답 예시:

```json
{
  "data": [
    {
      "id": 1,
      "name": "김철수",
      "email": "kim@test.com",
      "age": 30
    }
  ]
}
```

---

### 회원 전체 수정

```http
PUT /members/{id}
```

요청 예시:

```json
{
  "name": "수정된 이름",
  "email": "new@test.com",
  "age": 35
}
```

---

### 회원 부분 수정

```http
PATCH /members/{id}
```

요청 예시:

```json
{
  "name": "이름만 수정"
}
```

또는:

```json
{
  "email": "new@test.com"
}
```

또는:

```json
{
  "age": 40
}
```

---

### 회원 삭제

```http
DELETE /members/{id}
```

응답:

```http
204 No Content
```

---

### 회원 검색

```http
GET /members/search?name=김
GET /members/search?email=test.com
GET /members/search?name=김&email=test.com
```

검색 조건은 없으면 무시하고, 있으면 해당 조건에 맞는 회원만 반환한다.

---

## 8. 계층 구조

### Controller

HTTP 요청을 받는다.

역할:

* URL 매핑
* Request DTO 받기
* Validation 적용
* Service 호출
* Response DTO 반환
* HTTP Status Code 반환

---

### Service

비즈니스 흐름을 처리한다.

역할:

* 등록
* 조회
* 전체 조회
* 수정
* 부분 수정
* 삭제
* 검색
* 예외 처리

---

### Repository

저장소 역할을 담당한다.

현재는 DB를 사용하지 않고 `Map` 기반 메모리 저장소를 사용한다.

예:

```text
Map<Long, Book>
Map<Long, Member>
```

---

## 9. DTO를 사용하는 이유

API 요청과 응답에서 도메인 객체를 직접 사용하지 않기 위해 DTO를 사용한다.

사용한 Book DTO:

* `BookCreateRequest`
* `BookUpdateRequest`
* `BookPatchRequest`
* `BookSearchRequest`
* `BookResponse`

사용한 Member DTO:

* `MemberCreateRequest`
* `MemberUpdateRequest`
* `MemberPatchRequest`
* `MemberSearchRequest`
* `MemberResponse`

DTO를 사용하면 API 스펙과 내부 도메인 모델을 분리할 수 있다.

---

## 10. Validation

요청 값 검증에는 Bean Validation을 사용했다.

예시:

```java
@NotBlank(message = "이름은 필수입니다.")
private String name;

@NotBlank(message = "이메일은 필수입니다.")
@Email(message = "이메일 형식이 올바르지 않습니다.")
private String email;

@Min(value = 1, message = "나이는 1 이상이어야 합니다.")
private int age;
```

잘못된 요청이 들어오면 `400 Bad Request`를 반환한다.

---

## 11. 예외 처리

공통 예외 처리를 위해 `GlobalExceptionHandler`를 사용했다.

처리한 예외:

* `BookNotFoundException`
* `MemberNotFoundException`
* `DuplicateEmailException`
* `MethodArgumentNotValidException`
* `IllegalArgumentException`

---

### 없는 리소스 조회

없는 책 또는 회원을 조회하면 `404 Not Found`를 반환한다.

예시:

```json
{
  "status": 404,
  "message": "책을 찾을 수 없습니다."
}
```

```json
{
  "status": 404,
  "message": "회원을 찾을 수 없습니다."
}
```

---

### 검증 실패

검증 실패 시에는 `400 Bad Request`를 반환한다.

예시:

```json
{
  "status": 400,
  "message": "검증에 실패했습니다.",
  "errors": [
    "name: 이름은 필수입니다.",
    "email: 이메일 형식이 올바르지 않습니다.",
    "age: 나이는 1 이상이어야 합니다."
  ]
}
```

---

### 이메일 중복

이미 사용 중인 이메일로 회원을 등록하거나 수정하려 하면 `400 Bad Request`를 반환한다.

예시:

```json
{
  "status": 400,
  "message": "이미 사용 중인 이메일입니다."
}
```

---

## 12. 테스트

### Book Service Test

`BookServiceTest`에서 다음 기능을 검증했다.

* 책 등록
* 책 단건 조회
* 없는 책 조회 실패
* 책 전체 조회
* PUT 전체 수정
* PATCH 제목만 수정
* PATCH 가격만 수정
* 책 삭제
* 없는 책 삭제 실패
* 제목 검색
* 가격 범위 검색
* 제목 + 가격 검색

---

### Book Controller Test

`BookControllerTest`에서 MockMvc를 사용해 API 요청/응답을 검증했다.

검증한 내용:

* `POST /books` → 201 Created
* `GET /books/{id}` → 200 OK
* `GET /books` → 200 OK
* `PUT /books/{id}` → 200 OK
* `PATCH /books/{id}` → 200 OK
* `DELETE /books/{id}` → 204 No Content
* 잘못된 요청 → 400 Bad Request
* 없는 책 조회 → 404 Not Found
* 검색 API → 200 OK

---

### Member Service Test

`MemberServiceTest`에서 다음 기능을 검증했다.

* 회원 등록
* 이메일 중복 등록 실패
* 회원 단건 조회
* 없는 회원 조회 실패
* 회원 전체 조회
* PUT 전체 수정
* PUT 이메일 중복 수정 실패
* PATCH 이름만 수정
* PATCH 이메일만 수정
* PATCH 나이만 수정
* PATCH 이메일 중복 수정 실패
* 회원 삭제
* 없는 회원 삭제 실패
* 이름 검색
* 이메일 검색
* 이름 + 이메일 검색

---

### Member Controller Test

`MemberControllerTest`에서 MockMvc를 사용해 API 요청/응답을 검증했다.

검증한 내용:

* `POST /members` → 201 Created
* 이메일 중복 등록 → 400 Bad Request
* `GET /members/{id}` → 200 OK
* 없는 회원 조회 → 404 Not Found
* `GET /members` → 200 OK
* `PUT /members/{id}` → 200 OK
* `PATCH /members/{id}` → 200 OK
* `DELETE /members/{id}` → 204 No Content
* 검색 API → 200 OK

---

## 13. 현재까지 배운 핵심

* Spring MVC REST API 기본 흐름
* Controller / Service / Repository 역할 분리
* DTO를 통한 요청/응답 분리
* Validation 적용
* Global Exception Handling
* HTTP Status Code 사용
* PUT과 PATCH의 차이
* Stream filter를 활용한 검색
* 이메일 중복 검사 로직
* Service 단위 테스트
* MockMvc 기반 Controller 테스트
* 같은 구조를 다른 도메인에 반복 적용하기

---

## 14. 다음 목표

다음 단계에서는 단순 CRUD를 넘어서 관계가 있는 도메인을 연습한다.

예정 도메인:

```text
Order
```

목표:

* Member와 Book을 참조하는 주문 도메인 만들기
* 단순 CRUD가 아니라 주문 생성, 주문 취소 같은 행위 API 만들기
* 상태값 관리 연습
* JPA 연관관계에 들어가기 전 메모리 기반으로 관계 구조 이해하기

예정 API:

```http
POST /orders
GET /orders/{id}
GET /orders
PATCH /orders/{id}/cancel
GET /orders/search?memberId=1&status=ORDERED
```
