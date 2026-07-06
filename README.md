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
* 단순 CRUD와 행위 중심 API 차이 이해
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
 ├── order
 │   ├── Order
 │   ├── OrderStatus
 │   ├── OrderController
 │   ├── OrderService
 │   ├── OrderRepository
 │   ├── MemoryOrderRepository
 │   ├── OrderCreateRequest
 │   ├── OrderSearchRequest
 │   ├── OrderResponse
 │   └── OrderNotFoundException
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

# Order API

## 8. Order 도메인

`Order`는 회원이 책을 주문한 정보를 표현하는 도메인 객체다.

필드:

```text
id
memberId
bookId
quantity
orderPrice
status
```

`OrderStatus`:

```text
ORDERED
CANCELED
```

주요 기능:

```text
cancel()
getTotalPrice()
```

검증 조건:

* 회원 ID는 필수
* 책 ID는 필수
* 주문 수량은 1 이상
* 주문 가격은 1원 이상
* 이미 취소된 주문은 다시 취소할 수 없음

---

## 9. Order API 목록

### 주문 생성

```http
POST /orders
```

요청 예시:

```json
{
  "memberId": 1,
  "bookId": 1,
  "quantity": 2
}
```

응답 예시:

```http
201 Created
```

```json
{
  "data": {
    "id": 1,
    "memberId": 1,
    "bookId": 1,
    "quantity": 2,
    "orderPrice": 15000,
    "totalPrice": 30000,
    "status": "ORDERED"
  }
}
```

주문 생성 시 흐름:

```text
1. 회원 존재 여부 확인
2. 책 존재 여부 확인
3. 책 가격을 주문 가격으로 저장
4. 주문 상태를 ORDERED로 생성
5. 주문 저장
```

---

### 주문 단건 조회

```http
GET /orders/{id}
```

응답 예시:

```json
{
  "data": {
    "id": 1,
    "memberId": 1,
    "bookId": 1,
    "quantity": 2,
    "orderPrice": 15000,
    "totalPrice": 30000,
    "status": "ORDERED"
  }
}
```

---

### 주문 전체 조회

```http
GET /orders
```

---

### 주문 취소

```http
PATCH /orders/{id}/cancel
```

응답 예시:

```json
{
  "data": {
    "id": 1,
    "memberId": 1,
    "bookId": 1,
    "quantity": 2,
    "orderPrice": 15000,
    "totalPrice": 30000,
    "status": "CANCELED"
  }
}
```

주문 취소는 단순 상태 변경이 아니라, `cancel()`이라는 의미 있는 행위 메서드로 처리한다.

---

### 주문 검색

```http
GET /orders/search?memberId=1
GET /orders/search?status=ORDERED
GET /orders/search?memberId=1&status=CANCELED
```

검색 조건은 없으면 무시하고, 있으면 해당 조건에 맞는 주문만 반환한다.

---

# 공통 구조

## 10. 계층 구조

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
* 주문 생성
* 주문 취소
* 예외 처리

---

### Repository

저장소 역할을 담당한다.

현재는 DB를 사용하지 않고 `Map` 기반 메모리 저장소를 사용한다.

예:

```text
Map<Long, Book>
Map<Long, Member>
Map<Long, Order>
```

---

## 11. DTO를 사용하는 이유

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

사용한 Order DTO:

* `OrderCreateRequest`
* `OrderSearchRequest`
* `OrderResponse`

DTO를 사용하면 API 스펙과 내부 도메인 모델을 분리할 수 있다.

---

## 12. Validation

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

@NotNull(message = "회원 ID는 필수입니다.")
private Long memberId;
```

잘못된 요청이 들어오면 `400 Bad Request`를 반환한다.

---

## 13. 예외 처리

공통 예외 처리를 위해 `GlobalExceptionHandler`를 사용했다.

처리한 예외:

* `BookNotFoundException`
* `MemberNotFoundException`
* `OrderNotFoundException`
* `DuplicateEmailException`
* `MethodArgumentNotValidException`
* `IllegalArgumentException`
* `IllegalStateException`

---

### 없는 리소스 조회

없는 책, 회원, 주문을 조회하면 `404 Not Found`를 반환한다.

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

```json
{
  "status": 404,
  "message": "주문을 찾을 수 없습니다."
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

### 잘못된 상태 변경

이미 취소된 주문을 다시 취소하려 하면 `400 Bad Request`를 반환한다.

예시:

```json
{
  "status": 400,
  "message": "이미 취소된 주문입니다."
}
```

---

## 14. 테스트

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

### Order Service Test

`OrderServiceTest`에서 다음 기능을 검증했다.

* 주문 생성
* 없는 회원으로 주문 생성 실패
* 없는 책으로 주문 생성 실패
* 주문 단건 조회
* 없는 주문 조회 실패
* 주문 전체 조회
* 주문 취소
* 이미 취소된 주문 다시 취소 실패
* 회원 ID로 주문 검색
* 상태로 주문 검색
* 회원 ID + 상태로 주문 검색

---

### Order Controller Test

`OrderControllerTest`에서 MockMvc를 사용해 API 요청/응답을 검증했다.

검증한 내용:

* `POST /orders` → 201 Created
* 주문 생성 검증 실패 → 400 Bad Request
* 없는 회원으로 주문 생성 → 404 Not Found
* 없는 책으로 주문 생성 → 404 Not Found
* `GET /orders/{id}` → 200 OK
* 없는 주문 조회 → 404 Not Found
* `GET /orders` → 200 OK
* `PATCH /orders/{id}/cancel` → 200 OK
* 이미 취소된 주문 다시 취소 → 400 Bad Request
* 주문 검색 API → 200 OK

---

## 15. 현재까지 배운 핵심

* Spring MVC REST API 기본 흐름
* Controller / Service / Repository 역할 분리
* DTO를 통한 요청/응답 분리
* Validation 적용
* Global Exception Handling
* HTTP Status Code 사용
* PUT과 PATCH의 차이
* Stream filter를 활용한 검색
* 이메일 중복 검사 로직
* 단순 CRUD와 행위 API의 차이
* 주문 생성 시 다른 도메인 존재 확인
* 주문 취소 상태 변경
* Service 단위 테스트
* MockMvc 기반 Controller 테스트
* 같은 구조를 여러 도메인에 반복 적용하기

---

## 16. 다음 목표

다음 단계에서는 현재 메모리 저장소 기반 구조를 JPA 기반으로 전환한다.

우선 전환 대상:

```text
Book
```

목표:

* H2 Database 추가
* Spring Data JPA 추가
* `Book`을 JPA Entity로 변경
* `MemoryBookRepository`를 JPA 기반 Repository로 교체
* 기존 Controller / Service 구조를 최대한 유지
* 기존 테스트를 JPA 환경에 맞게 조정
* 계층 분리의 장점 체감하기

예상 흐름:

```text
MemoryBookRepository
↓
JpaBookRepository
```

핵심 목표는 저장소 구현이 바뀌어도 Controller와 Service 흐름이 크게 흔들리지 않게 만드는 것이다.
