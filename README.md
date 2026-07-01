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
 └── global
     ├── ApiResponse
     ├── ErrorResponse
     └── GlobalExceptionHandler
```

---

## 4. API 목록

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

## 5. 계층 구조

### Controller

HTTP 요청을 받는다.

역할:

* URL 매핑
* Request DTO 받기
* Validation 적용
* Service 호출
* Response DTO 반환

---

### Service

비즈니스 흐름을 처리한다.

역할:

* 책 등록
* 책 조회
* 책 수정
* 책 삭제
* 책 검색
* 없는 책에 대한 예외 처리

---

### Repository

저장소 역할을 담당한다.

현재는 DB를 사용하지 않고 `Map<Long, Book>` 기반 메모리 저장소를 사용한다.

---

## 6. DTO를 사용하는 이유

API 요청과 응답에서 도메인 객체를 직접 사용하지 않기 위해 DTO를 사용한다.

사용한 DTO:

* `BookCreateRequest`
* `BookUpdateRequest`
* `BookPatchRequest`
* `BookSearchRequest`
* `BookResponse`

DTO를 사용하면 API 스펙과 내부 도메인 모델을 분리할 수 있다.

---

## 7. Validation

요청 값 검증에는 Bean Validation을 사용했다.

예시:

```java
@NotBlank(message = "책 제목은 필수입니다.")
private String title;

@Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
private int price;
```

잘못된 요청이 들어오면 `400 Bad Request`를 반환한다.

---

## 8. 예외 처리

공통 예외 처리를 위해 `GlobalExceptionHandler`를 사용했다.

처리한 예외:

* `BookNotFoundException`
* `MethodArgumentNotValidException`
* `IllegalArgumentException`

없는 책을 조회하면 `404 Not Found`를 반환한다.

예시:

```json
{
  "status": 404,
  "message": "책을 찾을 수 없습니다."
}
```

검증 실패 시에는 `400 Bad Request`를 반환한다.

예시:

```json
{
  "status": 400,
  "message": "검증에 실패했습니다.",
  "errors": [
    "title: 책 제목은 필수입니다.",
    "price: 가격은 1원 이상이어야 합니다."
  ]
}
```

---

## 9. 테스트

### Service Test

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

### Controller Test

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

## 10. 현재까지 배운 핵심

* Spring MVC REST API 기본 흐름
* Controller / Service / Repository 역할 분리
* DTO를 통한 요청/응답 분리
* Validation 적용
* Global Exception Handling
* HTTP Status Code 사용
* PUT과 PATCH의 차이
* Stream filter를 활용한 검색
* Service 단위 테스트
* MockMvc 기반 Controller 테스트

---

## 11. 다음 목표

다음 단계에서는 같은 구조를 다른 도메인으로 반복한다.

예정 도메인:

```text
Member CRUD
```

목표:

* Book CRUD에서 배운 구조를 반복해서 체화하기
* Controller / Service / Repository / DTO / Test 흐름을 스스로 작성하기
* 이후 JPA로 저장소를 교체할 준비하기
