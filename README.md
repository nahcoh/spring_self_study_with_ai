# Spring MVC CRUD Practice

Spring MVC 기반의 CRUD REST API를 직접 구현하면서 백엔드 기본 구조를 연습한 프로젝트다.

처음에는 `MemoryRepository` 기반으로 시작했고, 이후 Spring Data JPA와 H2 Database를 적용하여 실제 DB 기반 구조로 전환했다.  
현재는 Book, Member, Order 도메인에 대해 CRUD, 검색, 페이징, 정렬, 예외 처리, Validation, 테스트까지 적용되어 있다.

---

## 1. 프로젝트 목표

이 프로젝트의 목표는 단순히 CRUD API를 만드는 것이 아니라, Spring 백엔드 애플리케이션의 기본 흐름을 직접 구현하며 체화하는 것이다.

주요 학습 목표는 다음과 같다.

- Controller, Service, Repository 계층 구조 이해
- REST API 요청/응답 흐름 이해
- Request DTO, Response DTO 분리
- 공통 응답 구조 적용
- Bean Validation 적용
- Global Exception Handling 적용
- HTTP Status Code 정리
- PUT과 PATCH 차이 이해
- MemoryRepository에서 JPA Repository로 전환
- JPA Entity 매핑
- JPA 변경 감지와 트랜잭션 이해
- `@ManyToOne` 연관관계 적용
- JPQL 검색 기능 구현
- Pageable 기반 페이징/정렬 구현
- Service Test, Controller Test, JPA Integration Test 작성

---

## 2. 기술 스택

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- H2 Database
- Hibernate
- Bean Validation
- Lombok
- JUnit 5
- AssertJ
- MockMvc
- Gradle

---

## 3. 주요 기능

### Book

- 책 등록
- 책 단건 조회
- 책 목록 조회
- 책 수정
- 책 부분 수정
- 책 삭제
- 책 검색
- 책 목록 페이징/정렬
- 책 검색 결과 페이징/정렬

### Member

- 회원 등록
- 회원 단건 조회
- 회원 목록 조회
- 회원 수정
- 회원 부분 수정
- 회원 삭제
- 이메일 중복 검사
- 회원 검색
- 회원 목록 페이징/정렬
- 회원 검색 결과 페이징/정렬

### Order

- 주문 생성
- 주문 단건 조회
- 주문 목록 조회
- 주문 취소
- 취소된 주문 재취소 방지
- 주문 검색
- 주문 목록 페이징/정렬
- 주문 검색 결과 페이징/정렬
- Member, Book과 `@ManyToOne` 연관관계 적용

---

## 4. 패키지 구조

```text
com.example.mvccrud
 ├── book
 │   ├── Book
 │   ├── BookController
 │   ├── BookService
 │   ├── BookRepository
 │   ├── JpaBookRepository
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
 │   ├── JpaMemberRepository
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
 │   ├── JpaOrderRepository
 │   ├── MemoryOrderRepository
 │   ├── OrderCreateRequest
 │   ├── OrderSearchRequest
 │   ├── OrderResponse
 │   └── OrderNotFoundException
 │
 ├── global
 │   ├── ApiResponse
 │   ├── PageResponse
 │   ├── ErrorResponse
 │   └── GlobalExceptionHandler
 │
 └── common
     └── DataInitializer
```

---

## 5. 실행 방법

### 기본 테스트 실행

```bash
./gradlew test
```

### dev 프로필로 서버 실행

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

dev 프로필에서는 H2 파일 DB를 사용한다.

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/mvc-crud
```

서버를 재시작해도 데이터가 유지된다.

---

## 6. H2 Console

dev 프로필로 서버를 실행한 뒤 아래 주소로 접속한다.

```text
http://localhost:8080/h2-console
```

접속 정보:

```text
Driver Class: org.h2.Driver
JDBC URL: jdbc:h2:file:./data/mvc-crud
User Name: sa
Password:
```

비밀번호는 비워둔다.

---

## 7. 더미 데이터

dev 프로필에서는 `DataInitializer`를 통해 더미 데이터를 자동으로 삽입한다.

예시 데이터:

- 회원 5명
- 책 10권
- 주문 10건
- 일부 주문은 `CANCELED` 상태

중복 삽입 방지를 위해 기존 회원 데이터가 있으면 더미 데이터를 다시 넣지 않는다.

```java
if (!memberService.findMembers().isEmpty()) {
    return;
}
```

---

# Book API

## 8. Book 도메인

`Book`은 책 정보를 표현하는 도메인이다.

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

- 제목은 필수
- 가격은 1원 이상

---

## 9. Book API 목록

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

### 책 목록 조회

```http
GET /books?page=0&size=5
```

정렬 예시:

```http
GET /books?page=0&size=5&sort=price,desc
GET /books?page=0&size=5&sort=title,asc
```

응답 예시:

```json
{
  "data": {
    "content": [
      {
        "id": 1,
        "title": "데미안",
        "price": 15000
      }
    ],
    "page": 0,
    "size": 5,
    "totalElements": 10,
    "totalPages": 2,
    "first": true,
    "last": false
  }
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
GET /books/search?title=자바&page=0&size=5
GET /books/search?minPrice=10000&maxPrice=30000&page=0&size=5
GET /books/search?title=자바&minPrice=10000&maxPrice=30000&page=0&size=5
```

정렬 예시:

```http
GET /books/search?minPrice=20000&maxPrice=40000&page=0&size=5&sort=price,desc
```

검색 조건은 없으면 무시하고, 있으면 해당 조건에 맞는 책만 반환한다.

---

# Member API

## 10. Member 도메인

`Member`는 회원 정보를 표현하는 도메인이다.

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

- 이름은 필수
- 이메일은 필수
- 이메일 형식 검증
- 나이는 1 이상
- 이메일은 중복될 수 없음

---

## 11. Member API 목록

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

### 회원 목록 조회

```http
GET /members?page=0&size=5
```

정렬 예시:

```http
GET /members?page=0&size=5&sort=age,desc
GET /members?page=0&size=5&sort=name,asc
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
GET /members/search?name=김&page=0&size=5
GET /members/search?email=test.com&page=0&size=5
GET /members/search?name=김&email=test.com&page=0&size=5
```

정렬 예시:

```http
GET /members/search?name=김&page=0&size=5&sort=age,desc
```

검색 조건은 없으면 무시하고, 있으면 해당 조건에 맞는 회원만 반환한다.

---

# Order API

## 12. Order 도메인

`Order`는 회원이 책을 주문한 정보를 표현하는 도메인이다.

현재 구조에서 주문 1개는 회원 1명이 책 1종류를 수량 N개 주문한 기록을 의미한다.

필드:

```text
id
member
book
quantity
orderPrice
status
```

응답에서는 편의를 위해 `memberId`, `bookId`를 반환한다.

`OrderStatus`:

```text
ORDERED
CANCELED
```

주요 기능:

```text
cancel()
getTotalPrice()
getMemberId()
getBookId()
```

검증 조건:

- 회원은 필수
- 책은 필수
- 주문 수량은 1 이상
- 주문 가격은 1원 이상
- 이미 취소된 주문은 다시 취소할 수 없음

---

## 13. Order 연관관계

`Order`는 `Member`, `Book`과 다대일 관계를 가진다.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "member_id")
private Member member;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "book_id")
private Book book;
```

관계 의미:

```text
Member 1명 → Order 여러 개
Book 1권 → Order 여러 개
Order 1개 → Member 1명
Order 1개 → Book 1권
```

현재 프로젝트에서는 주문 하나가 책 한 종류만 담는 단순 구조다.  
실무처럼 주문 하나에 여러 책을 담으려면 `OrderItem` 중간 엔티티를 추가하는 구조가 더 적합하다.

---

## 14. Order API 목록

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

응답:

```http
201 Created
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

주문 생성 흐름:

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

### 주문 목록 조회

```http
GET /orders?page=0&size=5
```

정렬 예시:

```http
GET /orders?page=0&size=5&sort=quantity,desc
GET /orders?page=0&size=5&sort=orderPrice,desc
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

주문 취소는 단순 필드 변경이 아니라 `cancel()`이라는 행위 메서드로 처리한다.

---

### 주문 검색

```http
GET /orders/search?memberId=1&page=0&size=5
GET /orders/search?status=ORDERED&page=0&size=5
GET /orders/search?memberId=1&status=CANCELED&page=0&size=5
```

검색 조건은 없으면 무시하고, 있으면 해당 조건에 맞는 주문만 반환한다.

---

# 공통 구조

## 15. 공통 응답 구조

성공 응답은 `ApiResponse<T>`로 감싼다.

```json
{
  "data": {
    "id": 1,
    "title": "데미안",
    "price": 15000
  }
}
```

목록과 검색 결과는 페이징 응답으로 반환한다.

```json
{
  "data": {
    "content": [],
    "page": 0,
    "size": 5,
    "totalElements": 10,
    "totalPages": 2,
    "first": true,
    "last": false
  }
}
```

---

## 16. PageResponse

페이징 응답은 `PageResponse<T>`로 통일했다.

```java
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
```

---

## 17. 계층 구조

### Controller

HTTP 요청을 받는다.

역할:

- URL 매핑
- Request DTO 받기
- Validation 적용
- Pageable 요청 받기
- Service 호출
- Response DTO 반환
- HTTP Status Code 반환

---

### Service

비즈니스 흐름을 처리한다.

역할:

- 등록
- 조회
- 목록 조회
- 수정
- 부분 수정
- 삭제
- 검색
- 페이징 조회
- 주문 생성
- 주문 취소
- 예외 처리
- 트랜잭션 관리

---

### Repository

저장소 역할을 담당한다.

현재는 Repository 인터페이스를 기준으로 Service가 의존하고, 실제 애플리케이션 실행 시에는 Spring Data JPA 구현체가 주입된다.

메모리 구현체는 테스트용으로 유지한다.

```text
BookRepository
 ├── JpaBookRepository
 └── MemoryBookRepository

MemberRepository
 ├── JpaMemberRepository
 └── MemoryMemberRepository

OrderRepository
 ├── JpaOrderRepository
 └── MemoryOrderRepository
```

---

## 18. DTO를 사용하는 이유

API 요청과 응답에서 Entity를 직접 사용하지 않기 위해 DTO를 사용한다.

사용한 Book DTO:

- `BookCreateRequest`
- `BookUpdateRequest`
- `BookPatchRequest`
- `BookSearchRequest`
- `BookResponse`

사용한 Member DTO:

- `MemberCreateRequest`
- `MemberUpdateRequest`
- `MemberPatchRequest`
- `MemberSearchRequest`
- `MemberResponse`

사용한 Order DTO:

- `OrderCreateRequest`
- `OrderSearchRequest`
- `OrderResponse`

DTO를 사용하면 API 스펙과 내부 Entity 모델을 분리할 수 있다.

---

## 19. Validation

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

## 20. 예외 처리

공통 예외 처리를 위해 `GlobalExceptionHandler`를 사용했다.

처리한 예외:

- `BookNotFoundException`
- `MemberNotFoundException`
- `OrderNotFoundException`
- `DuplicateEmailException`
- `MethodArgumentNotValidException`
- `IllegalArgumentException`
- `IllegalStateException`

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

# JPA 전환

## 21. JPA 전환 목적

기존에는 `MemoryRepository` 기반으로 데이터를 저장했지만, 이후 Spring Data JPA와 H2 Database를 적용하여 실제 데이터베이스 기반 구조로 전환했다.

전환 목적:

- 메모리 저장소에서 DB 저장소로 구조 확장
- Repository 인터페이스를 유지하면서 구현체만 교체
- JPA Entity 매핑 학습
- JPA 변경 감지 학습
- 트랜잭션 동작 학습
- 통합 테스트를 통해 실제 DB 저장/조회/수정/삭제 검증

---

## 22. Repository 구조

각 도메인은 Repository 인터페이스를 기준으로 동작한다.

예시:

```java
public interface BookRepository {

    Book save(Book book);

    Optional<Book> findById(Long id);

    List<Book> findAll();

    Page<Book> findAll(Pageable pageable);

    List<Book> search(String title, Integer minPrice, Integer maxPrice);

    Page<Book> search(String title, Integer minPrice, Integer maxPrice, Pageable pageable);

    void deleteById(Long id);

    boolean existsById(Long id);
}
```

JPA 구현체 예시:

```java
public interface JpaBookRepository extends JpaRepository<Book, Long>, BookRepository {
}
```

이 구조를 통해 Service 계층은 구체적인 저장소 구현체에 의존하지 않고, Repository 인터페이스에만 의존한다.

---

## 23. Entity 전환

### Book

```java
@Entity
@NoArgsConstructor
@Getter
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private int price;
}
```

---

### Member

```java
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
}
```

이메일 중복 검사를 위해 JPA Repository에서 다음 메서드를 사용한다.

```java
Optional<Member> findByEmail(String email);

boolean existsByEmail(String email);
```

---

### Order

```java
@Entity
@Table(name = "orders")
@NoArgsConstructor
@Getter
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    private int quantity;
    private int orderPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
```

`Order`는 SQL 예약어와 충돌할 수 있으므로 테이블명을 `orders`로 지정했다.

```java
@Table(name = "orders")
```

`OrderStatus`는 enum 순서 변경으로 인한 데이터 오류를 방지하기 위해 문자열로 저장한다.

```java
@Enumerated(EnumType.STRING)
```

---

## 24. JPQL 검색 기능

Book, Member, Order는 각각 검색 조건에 따라 조회할 수 있도록 JPQL을 사용했다.

### Book 검색

```java
@Query("""
    select b from Book b
    where (:title is null or :title = '' or b.title like concat('%', :title, '%'))
    and (:minPrice is null or b.price >= :minPrice)
    and (:maxPrice is null or b.price <= :maxPrice)
    """)
Page<Book> search(
        String title,
        Integer minPrice,
        Integer maxPrice,
        Pageable pageable
);
```

---

### Member 검색

```java
@Query("""
    select m from Member m
    where (:name is null or :name = '' or m.name like concat('%', :name, '%'))
    and (:email is null or :email = '' or m.email like concat('%', :email, '%'))
    """)
Page<Member> search(
        String name,
        String email,
        Pageable pageable
);
```

---

### Order 검색

```java
@Query("""
    select o from Order o
    where (:memberId is null or o.member.id = :memberId)
    and (:status is null or o.status = :status)
    """)
Page<Order> search(
        Long memberId,
        OrderStatus status,
        Pageable pageable
);
```

---

## 25. 트랜잭션과 변경 감지

JPA에서는 엔티티 값을 변경할 때 트랜잭션 안에서 실행되어야 변경 감지가 동작한다.

예를 들어 주문 취소 기능은 엔티티의 상태를 직접 변경한다.

```java
@Transactional
public Order cancelOrder(Long id) {
    Order order = findOrder(id);
    order.cancel();
    return order;
}
```

`order.cancel()`을 호출하면 `Order` 엔티티의 상태가 `CANCELED`로 변경되고, 트랜잭션 커밋 시점에 DB에 반영된다.

---

# 페이징과 정렬

## 26. Pageable 적용

목록 조회와 검색 API에 `Pageable`을 적용했다.

적용 대상:

```text
GET /books
GET /books/search
GET /members
GET /members/search
GET /orders
GET /orders/search
```

요청 예시:

```http
GET /books?page=0&size=5
GET /members?page=0&size=5&sort=age,desc
GET /orders/search?status=CANCELED&page=0&size=5
```

---

## 27. Page와 PageResponse

Repository에서는 `Page<T>`를 반환하고, Controller에서는 Entity를 Response DTO로 변환한 뒤 `PageResponse<T>`로 감싼다.

예시:

```java
@GetMapping
public ApiResponse<PageResponse<BookResponse>> findBooks(Pageable pageable) {
    Page<BookResponse> books = bookService.findBooks(pageable)
            .map(BookResponse::new);

    return ApiResponse.of(PageResponse.from(books));
}
```

`Page.map()`을 사용해 Entity를 Response DTO로 변환한다.

```java
Page<Book>
↓
Page<BookResponse>
```

---

## 28. MemoryRepository의 페이징 처리

JPA Repository는 Pageable을 자동 처리하지만, MemoryRepository는 직접 리스트를 잘라서 `PageImpl`로 반환해야 한다.

예시:

```java
@Override
public Page<Book> findAll(Pageable pageable) {
    List<Book> books = new ArrayList<>(store.values());

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), books.size());

    if (start >= books.size()) {
        return new PageImpl<>(List.of(), pageable, books.size());
    }

    List<Book> pageContent = books.subList(start, end);

    return new PageImpl<>(pageContent, pageable, books.size());
}
```

검색 페이징에서는 먼저 조건 검색을 하고, 그 결과를 페이징해야 한다.

```java
@Override
public Page<Order> search(Long memberId, OrderStatus status, Pageable pageable) {
    List<Order> orders = search(memberId, status);

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), orders.size());

    if (start >= orders.size()) {
        return new PageImpl<>(List.of(), pageable, orders.size());
    }

    List<Order> pageContent = orders.subList(start, end);

    return new PageImpl<>(pageContent, pageable, orders.size());
}
```

---

# 테스트

## 29. Service Test

### BookServiceTest

검증 내용:

- 책 등록
- 책 단건 조회
- 없는 책 조회 실패
- 책 전체 조회
- PUT 전체 수정
- PATCH 제목만 수정
- PATCH 가격만 수정
- 책 삭제
- 없는 책 삭제 실패
- 제목 검색
- 가격 범위 검색
- 제목 + 가격 검색

---

### MemberServiceTest

검증 내용:

- 회원 등록
- 이메일 중복 등록 실패
- 회원 단건 조회
- 없는 회원 조회 실패
- 회원 전체 조회
- PUT 전체 수정
- PUT 이메일 중복 수정 실패
- PATCH 이름만 수정
- PATCH 이메일만 수정
- PATCH 나이만 수정
- PATCH 이메일 중복 수정 실패
- 회원 삭제
- 없는 회원 삭제 실패
- 이름 검색
- 이메일 검색
- 이름 + 이메일 검색

---

### OrderServiceTest

검증 내용:

- 주문 생성
- 없는 회원으로 주문 생성 실패
- 없는 책으로 주문 생성 실패
- 주문 단건 조회
- 없는 주문 조회 실패
- 주문 전체 조회
- 주문 취소
- 이미 취소된 주문 다시 취소 실패
- 회원 ID로 주문 검색
- 상태로 주문 검색
- 회원 ID + 상태로 주문 검색

---

## 30. Controller Test

MockMvc를 사용해 API 요청/응답을 검증했다.

검증 내용:

- 등록 API `201 Created`
- 단건 조회 API `200 OK`
- 목록 조회 API `200 OK`
- 페이징 응답 구조 검증
- 수정 API `200 OK`
- 부분 수정 API `200 OK`
- 삭제 API `204 No Content`
- 검색 API `200 OK`
- Validation 실패 `400 Bad Request`
- 없는 리소스 조회 `404 Not Found`
- 중복 이메일 `400 Bad Request`
- 취소된 주문 재취소 `400 Bad Request`

페이징 응답 구조 변경으로 인해 테스트의 JSON 경로도 변경했다.

기존 List 응답:

```java
jsonPath("$.data.length()")
jsonPath("$.data[0].title")
```

현재 Page 응답:

```java
jsonPath("$.data.content.length()")
jsonPath("$.data.content[0].title")
jsonPath("$.data.page")
jsonPath("$.data.size")
jsonPath("$.data.totalElements")
jsonPath("$.data.totalPages")
jsonPath("$.data.first")
jsonPath("$.data.last")
```

standalone MockMvc 테스트에서는 Pageable을 처리하기 위해 다음 설정을 추가했다.

```java
.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
```

---

## 31. JPA Integration Test

실제 Spring Context, H2 Database, JPA를 사용한 통합 테스트를 작성했다.

### BookJpaIntegrationTest

검증 내용:

- 책 등록 시 DB 저장
- 책 조회
- 책 수정 시 변경 감지 동작
- 책 삭제
- 책 검색

---

### MemberJpaIntegrationTest

검증 내용:

- 회원 등록 시 DB 저장
- 회원 조회
- 이메일 중복 등록 실패
- 회원 수정 시 변경 감지 동작
- 회원 삭제
- 회원 검색

---

### OrderJpaIntegrationTest

검증 내용:

- 주문 생성 시 DB 저장
- 주문 조회
- 없는 주문 조회 실패
- 주문 취소 시 상태 변경
- 취소된 주문 재취소 실패
- memberId 검색
- status 검색
- memberId와 status 복합 검색

---

# 해결한 문제

## 32. Repository 메서드 반환 타입 충돌

`JpaRepository`와 직접 만든 Repository 인터페이스를 함께 상속할 때, 겹치는 메서드의 반환 타입이 다르면 충돌이 발생했다.

예를 들어 `findById`, `findAll`은 `JpaRepository`와 반환 타입을 맞춰야 한다.

```java
Optional<Member> findById(Long id);

List<Member> findAll();
```

---

## 33. JPQL 문법 오류

JPQL에서는 엔티티명을 기준으로 조회해야 한다.

잘못된 예:

```java
select o from Order order o
```

올바른 예:

```java
select o from Order o
```

---

## 34. JPQL 파라미터 공백 오류

JPQL 파라미터는 `:` 뒤에 공백 없이 작성해야 한다.

잘못된 예:

```java
o.member.id = : memberId
o.status =: status
```

올바른 예:

```java
o.member.id = :memberId
o.status = :status
```

---

## 35. SQL 예약어 충돌

`Order`는 SQL 예약어와 충돌할 수 있기 때문에 테이블명을 `orders`로 변경했다.

```java
@Entity
@Table(name = "orders")
public class Order {
}
```

---

## 36. 변경 감지 미동작

주문 취소 후 다시 조회했을 때 상태가 변경되지 않는 문제가 있었다.

원인은 상태 변경 메서드에 트랜잭션이 적용되지 않았기 때문이었다.

```java
@Transactional
public Order cancelOrder(Long id) {
    Order order = findOrder(id);
    order.cancel();
    return order;
}
```

트랜잭션을 적용한 후 JPA 변경 감지가 정상적으로 동작했다.

---

## 37. H2 파일 DB Lock 문제

H2 파일 DB를 여러 프로세스가 동시에 잡고 있으면 다음 문제가 발생했다.

```text
Database may be already in use
```

해결 방법:

- 중복 실행 중인 서버 종료
- 별도로 실행한 H2 앱 종료
- Spring Boot 앱에서 제공하는 `/h2-console` 사용

---

## 38. H2 Console 404 문제

Spring Boot 4 환경에서 H2 Console이 404로 뜨는 문제가 있었다.

해결을 위해 H2 Console 관련 의존성을 추가했다.

```gradle
developmentOnly 'org.springframework.boot:spring-boot-h2console'
```

---

## 39. H2 Console JDBC URL 문제

H2 Console 로그인 시 TCP URL을 사용하면 다음 오류가 발생했다.

```text
Connection refused: localhost
```

현재 프로젝트는 H2 TCP 서버를 띄운 것이 아니라 파일 DB를 사용하므로 아래 URL로 접속해야 한다.

```text
jdbc:h2:file:./data/mvc-crud
```

---

## 40. 잘못된 Page import 문제

OrderController에서 `Page` import를 잘못 가져와 컴파일 오류가 발생했다.

잘못된 import:

```java
import org.hibernate.query.Page;
```

올바른 import:

```java
import org.springframework.data.domain.Page;
```

---

## 41. MemoryRepository 검색 페이징 오류

검색 페이징 구현에서 조건 검색을 하지 않고 전체 데이터를 페이징해 테스트가 실패했다.

잘못된 코드:

```java
List<Order> orders = new ArrayList<>(store.values());
```

올바른 코드:

```java
List<Order> orders = search(memberId, status);
```

검색 페이징에서는 반드시 조건 검색 결과를 먼저 만들고, 그 결과를 페이징해야 한다.

---

# 현재 프로젝트 상태

## 42. 완료된 항목

- Book CRUD API 구현
- Member CRUD API 구현
- Order CRUD API 구현
- Request DTO / Response DTO 적용
- ApiResponse 공통 응답 구조 적용
- PageResponse 페이징 응답 구조 적용
- Bean Validation 적용
- GlobalExceptionHandler 적용
- MemoryRepository 기반 구현
- JPA Repository 기반 전환
- H2 Database 연동
- H2 파일 DB dev profile 구성
- H2 Console 설정
- 더미 데이터 자동 삽입
- JPQL 검색 기능 구현
- Pageable 기반 페이징/정렬 구현
- Order와 Member, Book 간 `@ManyToOne` 연관관계 적용
- Controller 단위 테스트 작성
- 페이징 응답 구조에 맞게 Controller 테스트 수정
- JPA 통합 테스트 작성

---

## 43. 현재 API 상태

```text
Book
- POST   /books
- GET    /books/{id}
- GET    /books?page=0&size=5
- GET    /books/search?title=자바&page=0&size=5
- PUT    /books/{id}
- PATCH  /books/{id}
- DELETE /books/{id}

Member
- POST   /members
- GET    /members/{id}
- GET    /members?page=0&size=5
- GET    /members/search?name=김&page=0&size=5
- PUT    /members/{id}
- PATCH  /members/{id}
- DELETE /members/{id}

Order
- POST   /orders
- GET    /orders/{id}
- GET    /orders?page=0&size=5
- GET    /orders/search?memberId=1&status=CANCELED&page=0&size=5
- PATCH  /orders/{id}/cancel
```

---

# 배운 핵심

## 44. Spring MVC

- Controller는 HTTP 요청과 응답을 담당한다.
- Service는 비즈니스 흐름을 담당한다.
- Repository는 저장소 접근을 담당한다.
- DTO를 사용하면 API 스펙과 내부 모델을 분리할 수 있다.
- Validation은 잘못된 요청을 빠르게 막아준다.
- GlobalExceptionHandler는 예외 응답을 일관되게 만든다.

---

## 45. JPA

- Entity는 DB 테이블과 매핑된다.
- `@Id`, `@GeneratedValue`로 식별자를 관리한다.
- `@Transactional` 안에서 Entity 값을 변경하면 변경 감지가 동작한다.
- enum은 `@Enumerated(EnumType.STRING)`으로 저장하는 것이 안전하다.
- SQL 예약어와 엔티티명이 충돌하면 `@Table`로 테이블명을 지정한다.
- `@ManyToOne(fetch = FetchType.LAZY)`로 다대일 연관관계를 표현할 수 있다.

---

## 46. 페이징

- `Pageable`은 요청의 `page`, `size`, `sort` 정보를 담는다.
- `Page<T>`는 실제 데이터와 페이징 메타데이터를 함께 가진다.
- `Page.map()`을 사용하면 `Page<Entity>`를 `Page<ResponseDto>`로 변환할 수 있다.
- API 응답에서는 `PageResponse<T>`로 필요한 정보만 노출한다.
- MemoryRepository에서는 `PageImpl`로 페이징 결과를 직접 만들어야 한다.

---

# 다음 개선 예정

## 47. 다음 단계 후보

- `createdAt`, `updatedAt` 추가
- BaseEntity 도입
- MySQL 또는 PostgreSQL 연동
- Docker Compose로 DB 실행 환경 구성
- Swagger 또는 Spring REST Docs로 API 문서화
- 인증/인가 적용
- 로그인 기능 추가
- 회원별 주문 조회 API 추가
- 주문 구조를 `OrderItem` 기반으로 확장
- `@ManyToOne` 연관관계에 대한 fetch join 최적화
- N+1 문제 실습
- 페이징 기본값 설정
- 정렬 가능한 필드 제한
- API 에러 응답 구조 개선
- CI 환경에서 테스트 자동 실행

---

## 48. 추천 다음 작업

다음 작업으로는 `createdAt`, `updatedAt`을 추가하여 생성일/수정일을 자동 관리하는 기능을 적용한다.

예상 학습 내용:

- JPA Auditing
- `@CreatedDate`
- `@LastModifiedDate`
- BaseEntity
- Entity 공통 필드 분리
- 테스트에서 시간 필드 검증


# JPA Auditing

## 생성일 / 수정일 자동 관리

각 Entity에 생성일과 수정일을 자동으로 기록하기 위해 JPA Auditing을 적용했다.

적용한 필드:

```text
createdAt
updatedAt
```

`createdAt`은 Entity가 처음 저장될 때 자동으로 입력되고,  
`updatedAt`은 Entity가 수정될 때 자동으로 갱신된다.

---

## BaseEntity

공통 시간 필드를 여러 Entity에 중복 작성하지 않기 위해 `BaseEntity`를 만들었다.

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

`@MappedSuperclass`를 사용했기 때문에 `BaseEntity` 자체는 테이블로 생성되지 않고,  
이를 상속한 Entity의 테이블에 `created_at`, `updated_at` 컬럼이 추가된다.

---

## Auditing 활성화

JPA Auditing을 사용하기 위해 메인 애플리케이션 클래스에 `@EnableJpaAuditing`을 추가했다.

```java
@EnableJpaAuditing
@SpringBootApplication
public class MvcCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(MvcCrudApplication.class, args);
    }
}
```

---

## Entity 상속 구조

`Book`, `Member`, `Order`는 모두 `BaseEntity`를 상속한다.

```java
public class Book extends BaseEntity {
}
```

```java
public class Member extends BaseEntity {
}
```

```java
public class Order extends BaseEntity {
}
```

이를 통해 각 Entity는 공통으로 `createdAt`, `updatedAt` 필드를 가진다.

---

## Response DTO에 시간 필드 노출

API 응답에서도 생성일과 수정일을 확인할 수 있도록 Response DTO에 `createdAt`, `updatedAt`을 추가했다.

예시:

```java
@Getter
public class BookResponse {

    private final Long id;
    private final String title;
    private final int price;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public BookResponse(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.price = book.getPrice();
        this.createdAt = book.getCreatedAt();
        this.updatedAt = book.getUpdatedAt();
    }
}
```

---

## 응답 예시

책 목록 조회 응답 예시:

```json
{
  "data": {
    "content": [
      {
        "id": 52,
        "title": "테스트 책",
        "price": 15000,
        "createdAt": "2026-07-08T15:12:45.51623",
        "updatedAt": "2026-07-08T15:13:30.677527"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 11,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

`createdAt`과 `updatedAt`이 다르다면 생성 이후 수정이 발생했다는 뜻이다.

---

## 기존 데이터의 null 문제

JPA Auditing을 적용하기 전에 이미 저장되어 있던 기존 H2 파일 DB 데이터는 `createdAt`, `updatedAt` 값이 `null`일 수 있다.

이유:

```text
1. 더미데이터가 먼저 저장됨
2. 이후 createdAt / updatedAt 컬럼 추가
3. ddl-auto: update가 컬럼만 추가
4. 기존 row의 시간 값은 자동으로 채워지지 않음
```

새로 저장되는 데이터부터는 JPA Auditing이 정상 적용된다.

개발용 H2 DB를 초기화하고 싶다면 서버를 종료한 뒤 아래 명령어로 파일 DB를 삭제한다.

```bash
rm -rf data
```

그 후 dev 프로필로 다시 실행하면 더미데이터가 새로 생성되고, 시간 필드도 함께 저장된다.

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

---

## JPA Auditing으로 배운 점

- 공통 필드는 `BaseEntity`로 분리할 수 있다.
- `@MappedSuperclass`는 부모 클래스의 필드를 자식 Entity 테이블에 포함시킨다.
- `@CreatedDate`는 생성 시간을 자동 저장한다.
- `@LastModifiedDate`는 수정 시간을 자동 갱신한다.
- JPA Auditing을 사용하려면 `@EnableJpaAuditing`이 필요하다.
- 기존 DB 데이터에는 새로 추가한 시간 컬럼이 `null`일 수 있다.

---

# MySQL + Docker Compose

## MySQL Docker 환경 구성

H2 외에도 실제 DB 환경에 가까운 개발을 위해 MySQL을 Docker Compose로 실행할 수 있도록 구성했다.

현재 DB 프로필은 다음과 같이 분리되어 있다.

```text
default profile → H2 Memory DB
dev profile     → H2 File DB + H2 Console
mysql profile   → Docker MySQL
```

---

## docker-compose.yml

프로젝트 루트에 `docker-compose.yml`을 추가했다.

```yaml
services:
  mysql:
    image: mysql:8.4
    container_name: mvc-crud-mysql
    ports:
      - "3306:3306"
    environment:
      MYSQL_DATABASE: mvc_crud
      MYSQL_USER: mvc_user
      MYSQL_PASSWORD: mvc_password
      MYSQL_ROOT_PASSWORD: root_password
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

---

## MySQL 프로필 설정

`application-mysql.yml`을 추가하여 MySQL 연결 설정을 분리했다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mvc_crud?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: mvc_user
    password: mvc_password

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        highlight_sql: true

logging:
  level:
    org.hibernate.SQL: debug
    org.hibernate.orm.jdbc.bind: trace
```

---

## MySQL 실행 방법

프로젝트 루트에서 Docker Compose를 실행한다.

```bash
docker compose up -d
```

컨테이너 실행 확인:

```bash
docker ps
```

정상 실행 시 `mvc-crud-mysql` 컨테이너가 보여야 한다.

```text
mvc-crud-mysql
0.0.0.0:3306->3306/tcp
```

MySQL 로그 확인:

```bash
docker logs mvc-crud-mysql
```

아래 문구가 보이면 MySQL 준비가 완료된 것이다.

```text
ready for connections
```

---

## MySQL 프로필로 Spring Boot 실행

```bash
SPRING_PROFILES_ACTIVE=mysql ./gradlew bootRun
```

정상 실행 시 로그에서 다음 내용을 확인할 수 있다.

```text
The following 1 profile is active: "mysql"
jdbc:mysql://localhost:3306/mvc_crud
```

---

## MySQL 환경에서 API 확인

MySQL 프로필로 서버를 실행한 뒤 Postman으로 확인한다.

```http
GET /books?page=0&size=5
GET /members?page=0&size=5
GET /orders?page=0&size=5
```

더미데이터가 적용되어 있다면 MySQL DB에도 초기 데이터가 자동으로 삽입된다.

`DataInitializer`는 다음 프로필에서 동작하도록 설정했다.

```java
@Profile({"dev", "mysql"})
```

---

## MySQL 종료

컨테이너 중지:

```bash
docker compose down
```

컨테이너와 볼륨까지 모두 삭제:

```bash
docker compose down -v
```

`-v` 옵션을 사용하면 MySQL 데이터도 함께 삭제되므로 주의한다.

---

## MySQL 적용으로 배운 점

- Docker Compose로 로컬 DB 환경을 구성할 수 있다.
- H2와 MySQL을 Spring Profile로 분리할 수 있다.
- `application-dev.yml`, `application-mysql.yml`처럼 환경별 설정을 나눌 수 있다.
- MySQL 컨테이너가 완전히 준비되기 전에는 Spring Boot 연결이 실패할 수 있다.
- `docker logs`로 DB 준비 상태를 확인할 수 있다.
- JPA Entity와 Repository 코드는 그대로 두고 DB만 교체할 수 있다.


- Docker Compose 기반 MySQL 실행 환경 구성
- mysql profile 추가
- H2 / MySQL 환경 분리
- MySQL 환경에서 JPA 동작 확인
- MySQL 환경에서 Postman API 테스트 완료

#Swagger / OpenAPI
## Swagger 적용
API 문서화를 위해 SpringDoc OpenAPI를 적용했다.

Swagger UI를 통해 Book, Member, Order API의 요청/응답 구조를 확인하고 직접 테스트할 수 있다.

---
## Swagger UI 접속
서버 실행 후 아래 주소로 접속한다. 
```text
http://localhost:8080/swagger-ui/index.html
```
OpenAPI JSON 문서는 아래 주소에서 확인할 수 있다.
```text
http://localhost:8080/v3/api-docs
```
---
## 적용 내용
- Swagger UI 추가
- OpenAPI 문서 제목, 설명, 버전 설정
- Controller별 API 그룹화
- API별 설명 추가 
- Request DTO / Response DTO 필드 설명 추가

---
## API 그룹
```text
Book API
Member API
Order API
```
## 예시
```java
@Tag(name = "Book API", description = "책 등록, 조회, 수정, 삭제, 검색 API")
@RestController
@RequestMapping("/books")
public class BookController {
}
```
```java
@Operation(summary = "책 등록", description = "책 제목과 가격을 입력받아 새 책을 등록합니다.")
@PostMapping
public ResponseEntity<ApiResponse<BookResponse>> createBook(...){
}
```
```java
@Schema(description = "책 제목", example = "데미안")
private String title;
```
## Swagger 적용으로 배운 점
- SpringDoc OpenAPI로 API 문서를 자동 생성할 수 있다.
- Controller의 `@Operation`으로 API설명을 추가할 수 있다.
- `@Tag`로 API 그룹을 나눌 수 있다.
- DTO 필드에 `@Schema`를 붙여 요청/응답 스키마 설명을 제공할 수 있다.
- Postman 없이 브라우저에서 API를 테스트할 수 있다.


## Redis 캐시 적용

### 적용 대상

- `GET /books/{id}` 책 단건 조회 API

### 적용 이유

자주 조회되는 책 상세 정보를 Redis에 캐싱하여 반복 조회 시 MySQL 접근을 줄이기 위해 적용했다.

### 동작 흐름

1. 클라이언트가 `GET /books/{id}` 요청
2. Redis에 `book::{id}` 캐시가 있는지 확인
3. 캐시가 있으면 Redis에서 바로 반환
4. 캐시가 없으면 MySQL에서 조회
5. 조회 결과를 Redis에 저장
6. 이후 같은 요청은 Redis에서 반환

### 캐시 무효화

책 정보가 수정되거나 삭제되면 기존 캐시가 낡은 데이터가 될 수 있으므로 캐시를 삭제한다.

- `PUT /books/{id}` → `book::{id}` 캐시 삭제
- `PATCH /books/{id}` → `book::{id}` 캐시 삭제
- `DELETE /books/{id}` → 삭제 성공 시 `book::{id}` 캐시 삭제

### 예외 처리

주문에서 참조 중인 책을 삭제하면 외래키 제약조건 때문에 삭제할 수 없다.  
이 경우 `DataIntegrityViolationException`을 처리하여 `409 Conflict`를 반환한다.