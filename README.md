# Spring MVC CRUD Practice

Spring Boot 기반의 Book / Member / Order CRUD REST API 프로젝트입니다.

처음에는 `MemoryRepository` 기반으로 시작했고, 이후 Spring Data JPA, H2, MySQL, Redis, Docker Compose, Swagger, GitHub Actions CI까지 확장하며 백엔드 애플리케이션의 기본 구조와 인프라 연결 흐름을 학습했습니다.

---

## 프로젝트 요약

이 프로젝트는 단순 CRUD API 구현을 넘어, Spring 백엔드 애플리케이션의 계층 구조와 실무에서 자주 사용하는 개발 흐름을 직접 구현하며 학습하는 것을 목표로 합니다.

주요 구현 내용은 다음과 같습니다.

- Controller / Service / Repository 계층 구조
- Request DTO / Response DTO 분리
- 공통 응답 구조
- Bean Validation
- Global Exception Handling
- JPA Entity 매핑
- JPA 변경 감지
- JPA Auditing
- `@ManyToOne` 연관관계
- JPQL 검색
- Pageable 기반 페이징 / 정렬
- 정렬 가능한 필드 제한
- Lazy Loading과 OSIV 문제 해결
- N+1 문제 확인 및 `@EntityGraph` 최적화
- Redis Cache 적용
- Redis JSON 직렬화
- Docker Compose 기반 App + MySQL + Redis 실행 환경
- Swagger / OpenAPI 문서화
- GitHub Actions CI 테스트 자동화

---

## 기술 스택

### Backend

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Data JPA
- Spring Cache
- Bean Validation
- Lombok

### Database / Cache

- H2 Database
- MySQL 8.4
- Redis 7.4
- Hibernate

### Test

- JUnit 5
- AssertJ
- MockMvc
- Spring Boot Test
- JPA Integration Test

### Infra / Tooling

- Gradle
- Docker
- Docker Compose
- Swagger / SpringDoc OpenAPI
- GitHub Actions

---

## 기술 선택 이유

### Spring Boot

REST API 서버를 빠르게 구성하고, Validation, JPA, Cache, Test 등 백엔드 개발에 필요한 기능을 통합적으로 사용하기 위해 선택했습니다.

### Spring Data JPA

SQL 중심이 아니라 객체 중심으로 도메인을 다루고, Entity 매핑, 변경 감지, 연관관계, 트랜잭션을 학습하기 위해 사용했습니다.

### H2

초기 개발과 테스트 단계에서 빠르게 DB 기반 기능을 검증하기 위해 사용했습니다.

### MySQL

H2보다 실제 운영 환경에 가까운 관계형 데이터베이스를 경험하기 위해 Docker 기반 MySQL을 적용했습니다.

### Redis

반복 조회되는 책 상세 정보를 캐싱하여 MySQL 조회를 줄이고, Cache Hit / Miss, TTL, 캐시 무효화 흐름을 학습하기 위해 사용했습니다.

### Docker Compose

Spring Boot 애플리케이션, MySQL, Redis를 한 번에 실행할 수 있는 로컬 개발 환경을 구성하기 위해 사용했습니다.

### GitHub Actions

push 시 자동으로 테스트를 실행하여 변경사항이 기존 기능을 깨뜨리지 않는지 검증하기 위해 사용했습니다.

---

## 전체 구조

```text
Client
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
MySQL
```

Redis 캐시가 적용된 책 단건 조회 흐름은 다음과 같습니다.

```text
Client
  ↓
BookController
  ↓
BookService
  ↓
Redis Cache 확인
  ├─ Cache Hit  → Redis 값 반환
  └─ Cache Miss → MySQL 조회 → Redis 저장 → 응답
```

---

## 패키지 구조

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
 │   ├── GlobalExceptionHandler
 │   ├── BaseEntity
 │   ├── SortValidator
 │   ├── InvalidSortException
 │   └── RedisCacheConfig
 │
 └── common
     └── DataInitializer
```

---

## 실행 방법

### 테스트 실행

```bash
./gradlew test
```

### dev 프로필 실행

dev 프로필은 H2 파일 DB를 사용합니다.

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

H2 Console:

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

### mysql 프로필 실행

MySQL과 Redis를 Docker로 실행한 뒤 Spring Boot를 로컬에서 실행합니다.

```bash
docker compose up -d mysql redis
```

```bash
SPRING_PROFILES_ACTIVE=mysql ./gradlew bootRun
```

### docker 프로필 실행

Spring Boot App, MySQL, Redis를 모두 Docker Compose로 실행합니다.

```bash
docker compose up -d --build
```

종료:

```bash
docker compose down
```

볼륨까지 삭제:

```bash
docker compose down -v
```

`-v` 옵션은 MySQL 데이터도 삭제하므로 주의합니다.

---

## Docker Compose 구성

이 프로젝트는 Docker Compose를 통해 세 개의 서비스를 함께 실행할 수 있습니다.

```text
app   → Spring Boot API 서버
mysql → MySQL 데이터베이스
redis → Redis 캐시 서버
```

접속 정보:

```text
API 서버   : http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui/index.html
MySQL     : localhost:3306
Redis     : localhost:6379
```

Docker 환경에서는 `docker` 프로필을 사용합니다.

```yaml
SPRING_PROFILES_ACTIVE: docker
```

Docker 컨테이너 내부에서는 `localhost`가 아니라 Compose 서비스 이름으로 접근해야 합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/mvc_crud

  data:
    redis:
      host: redis
```

---

## Swagger / OpenAPI

API 문서화를 위해 SpringDoc OpenAPI를 적용했습니다.

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

적용 내용:

- Swagger UI 추가
- OpenAPI 제목, 설명, 버전 설정
- Controller별 API 그룹화
- API별 설명 추가
- Request DTO / Response DTO 필드 설명 추가

API 그룹:

```text
Book API
Member API
Order API
```

---

## API 목록

### Book API

```text
POST   /books
GET    /books/{id}
GET    /books
GET    /books/search
PUT    /books/{id}
PATCH  /books/{id}
DELETE /books/{id}
```

검색 예시:

```http
GET /books/search?title=자바&page=0&size=5
GET /books/search?minPrice=10000&maxPrice=30000&page=0&size=5
GET /books/search?title=자바&minPrice=10000&maxPrice=30000&page=0&size=5
```

정렬 예시:

```http
GET /books?page=0&size=5&sort=price,desc
GET /books/search?title=자바&page=0&size=5&sort=createdAt,desc
```

---

### Member API

```text
POST   /members
GET    /members/{id}
GET    /members
GET    /members/search
PUT    /members/{id}
PATCH  /members/{id}
DELETE /members/{id}
```

검색 예시:

```http
GET /members/search?name=김&page=0&size=5
GET /members/search?email=test.com&page=0&size=5
GET /members/search?name=김&email=test.com&page=0&size=5
```

정렬 예시:

```http
GET /members?page=0&size=5&sort=age,desc
GET /members/search?name=김&page=0&size=5&sort=name,asc
```

---

### Order API

```text
POST   /orders
GET    /orders/{id}
GET    /orders
GET    /orders/search
PATCH  /orders/{id}/cancel
```

검색 예시:

```http
GET /orders/search?memberId=1&page=0&size=5
GET /orders/search?status=ORDERED&page=0&size=5
GET /orders/search?memberId=1&status=CANCELED&page=0&size=5
```

정렬 예시:

```http
GET /orders?page=0&size=5&sort=orderPrice,desc
GET /orders/search?status=ORDERED&page=0&size=5&sort=createdAt,desc
```

---

## 도메인 설명

### Book

책 정보를 표현하는 도메인입니다.

필드:

```text
id
title
price
createdAt
updatedAt
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

### Member

회원 정보를 표현하는 도메인입니다.

필드:

```text
id
name
email
age
createdAt
updatedAt
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

### Order

회원이 책을 주문한 정보를 표현하는 도메인입니다.

현재 구조에서 주문 1개는 회원 1명이 책 1종류를 수량 N개 주문한 기록을 의미합니다.

필드:

```text
id
member
book
quantity
orderPrice
status
createdAt
updatedAt
```

응답에서는 편의를 위해 다음 값을 함께 반환합니다.

```text
memberId
memberName
bookId
bookTitle
totalPrice
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
getMemberId()
getBookId()
```

---

## Order 연관관계

`Order`는 `Member`, `Book`과 다대일 관계를 가집니다.

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

현재 프로젝트에서는 주문 하나가 책 한 종류만 담는 단순 구조입니다.

실무처럼 주문 하나에 여러 책을 담으려면 `OrderItem` 중간 엔티티를 추가하는 구조가 더 적합합니다.

---

## 공통 응답 구조

성공 응답은 `ApiResponse<T>`로 감쌉니다.

```json
{
  "data": {
    "id": 1,
    "title": "데미안",
    "price": 15000
  }
}
```

목록과 검색 결과는 `PageResponse<T>`로 반환합니다.

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

## PageResponse

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

## 예외 처리

공통 예외 처리를 위해 `GlobalExceptionHandler`를 사용했습니다.

처리한 예외:

- `BookNotFoundException`
- `MemberNotFoundException`
- `OrderNotFoundException`
- `DuplicateEmailException`
- `InvalidSortException`
- `MethodArgumentNotValidException`
- `IllegalArgumentException`
- `IllegalStateException`
- `DataIntegrityViolationException`

---

### 없는 리소스 조회

없는 책, 회원, 주문을 조회하면 `404 Not Found`를 반환합니다.

```json
{
  "status": 404,
  "message": "책을 찾을 수 없습니다."
}
```

---

### 검증 실패

검증 실패 시 `400 Bad Request`를 반환합니다.

```json
{
  "status": 400,
  "message": "검증에 실패했습니다.",
  "errors": [
    "name: 이름은 필수입니다.",
    "email: 이메일 형식이 올바르지 않습니다."
  ]
}
```

---

### 정렬 불가능한 필드

허용되지 않은 필드로 정렬을 요청하면 `400 Bad Request`를 반환합니다.

```http
GET /books?sort=unknownField,asc
```

```json
{
  "status": 400,
  "message": "정렬할 수 없는 필드입니다: unknownField"
}
```

---

### 참조 중인 데이터 삭제

주문에서 참조 중인 책을 삭제하면 외래키 제약조건 때문에 삭제할 수 없습니다.

이 경우 `DataIntegrityViolationException`을 처리하여 `409 Conflict`를 반환합니다.

```json
{
  "status": 409,
  "message": "참조 중인 데이터가 있어 삭제할 수 없습니다."
}
```

---

## Validation

요청 값 검증에는 Bean Validation을 사용했습니다.

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

잘못된 요청이 들어오면 `400 Bad Request`를 반환합니다.

---

## JPA 전환

기존에는 `MemoryRepository` 기반으로 데이터를 저장했지만, 이후 Spring Data JPA와 DB 기반 구조로 전환했습니다.

전환 목적:

- 메모리 저장소에서 DB 저장소로 구조 확장
- Repository 인터페이스를 유지하면서 구현체만 교체
- JPA Entity 매핑 학습
- JPA 변경 감지 학습
- 트랜잭션 동작 학습
- 실제 DB 저장 / 조회 / 수정 / 삭제 검증

Repository 구조:

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

Service 계층은 구체적인 저장소 구현체가 아니라 Repository 인터페이스에 의존합니다.

---

## JPQL 검색

Book, Member, Order는 각각 검색 조건에 따라 조회할 수 있도록 JPQL을 사용했습니다.

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

## 트랜잭션과 변경 감지

JPA에서는 엔티티 값을 변경할 때 트랜잭션 안에서 실행되어야 변경 감지가 동작합니다.

예를 들어 주문 취소 기능은 엔티티의 상태를 직접 변경합니다.

```java
@Transactional
public Order cancelOrder(Long id) {
    Order order = findOrder(id);
    order.cancel();
    return order;
}
```

`order.cancel()`을 호출하면 `Order` 엔티티의 상태가 `CANCELED`로 변경되고, 트랜잭션 커밋 시점에 DB에 반영됩니다.

---

## JPA Auditing

각 Entity에 생성일과 수정일을 자동으로 기록하기 위해 JPA Auditing을 적용했습니다.

적용한 필드:

```text
createdAt
updatedAt
```

`createdAt`은 Entity가 처음 저장될 때 자동으로 입력되고, `updatedAt`은 Entity가 수정될 때 자동으로 갱신됩니다.

---

## BaseEntity

공통 시간 필드를 여러 Entity에 중복 작성하지 않기 위해 `BaseEntity`를 만들었습니다.

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

`@MappedSuperclass`를 사용했기 때문에 `BaseEntity` 자체는 테이블로 생성되지 않고, 이를 상속한 Entity의 테이블에 `created_at`, `updated_at` 컬럼이 추가됩니다.

---

## Auditing 활성화

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

## Lazy Loading과 OSIV

`spring.jpa.open-in-view=false` 설정을 적용했습니다.

이 설정을 사용하면 Service 계층의 트랜잭션이 끝난 뒤 Controller에서 Lazy Loading이 발생할 수 없습니다.

처음에는 Controller에서 `OrderResponse`를 생성하면서 다음 문제가 발생했습니다.

```text
LazyInitializationException
```

원인:

```text
Service 트랜잭션 종료
→ Controller에서 order.getMember().getName() 접근
→ 영속성 컨텍스트가 닫혀 있어 Lazy Loading 실패
```

해결:

```text
OrderResponse 변환을 Service 계층의 트랜잭션 안에서 수행
```

이를 통해 Lazy Loading이 필요한 값을 트랜잭션 안에서 안전하게 조회하도록 변경했습니다.

---

## N+1 문제와 EntityGraph

Order 목록 조회에서 `Order`를 조회한 뒤 각 주문의 `Member`, `Book`을 조회하면서 N+1 문제가 발생했습니다.

기존 흐름:

```text
orders 조회 1번
member 조회 N번
book 조회 N번
```

해결을 위해 `@EntityGraph`를 적용했습니다.

```java
@Override
@EntityGraph(attributePaths = {"member", "book"})
Page<Order> findAll(Pageable pageable);
```

검색 API에도 동일하게 적용했습니다.

```java
@EntityGraph(attributePaths = {"member", "book"})
@Query("""
    select o from Order o
    where (:memberId is null or o.member.id = :memberId)
    and (:status is null or o.status = :status)
    """)
Page<Order> search(Long memberId, OrderStatus status, Pageable pageable);
```

적용 후 Order 목록 조회 시 `member`, `book`을 함께 조회하여 추가 select를 줄였습니다.

---

## 페이징과 정렬

목록 조회와 검색 API에 `Pageable`을 적용했습니다.

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

## 페이징 기본값과 최대 크기

기본 페이지 크기와 최대 페이지 크기를 설정했습니다.

```yaml
spring:
  data:
    web:
      pageable:
        default-page-size: 10
        max-page-size: 50
        one-indexed-parameters: false
```

Controller에서는 `@PageableDefault`를 사용해 기본 정렬 기준을 지정했습니다.

```java
@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
Pageable pageable
```

---

## 정렬 가능한 필드 제한

허용되지 않은 필드로 정렬을 요청하면 JPA 오류가 발생할 수 있으므로 `SortValidator`를 추가했습니다.

Book 허용 필드:

```text
id
title
price
createdAt
updatedAt
```

Member 허용 필드:

```text
id
name
email
age
createdAt
updatedAt
```

Order 허용 필드:

```text
id
quantity
orderPrice
status
createdAt
updatedAt
```

예시:

```http
GET /books?sort=unknownField,asc
```

응답:

```json
{
  "status": 400,
  "message": "정렬할 수 없는 필드입니다: unknownField"
}
```

---

## Redis 캐시 적용

책 단건 조회 API에 Redis 캐시를 적용했습니다.

적용 대상:

```http
GET /books/{id}
```

적용 이유:

```text
자주 조회되는 책 상세 정보를 Redis에 캐싱하여 반복 조회 시 MySQL 접근을 줄이기 위해 적용
```

동작 흐름:

```text
1. 클라이언트가 GET /books/{id} 요청
2. Redis에 book::{id} 캐시가 있는지 확인
3. Cache Hit이면 Redis에서 바로 반환
4. Cache Miss이면 MySQL에서 조회
5. 조회 결과를 Redis에 저장
6. 이후 같은 요청은 Redis에서 반환
```

---

## Cache Hit / Cache Miss

첫 번째 요청:

```text
GET /books/1
→ Cache Miss
→ MySQL 조회
→ Redis 저장
```

두 번째 요청:

```text
GET /books/1
→ Cache Hit
→ MySQL 조회 없음
→ Redis 값 반환
```

---

## 캐시 무효화

책 정보가 수정되거나 삭제되면 기존 캐시는 낡은 데이터가 될 수 있으므로 캐시를 삭제합니다.

```text
PUT /books/{id}    → book::{id} 캐시 삭제
PATCH /books/{id}  → book::{id} 캐시 삭제
DELETE /books/{id} → 삭제 성공 시 book::{id} 캐시 삭제
```

삭제가 외래키 제약조건으로 실패하면 실제 데이터가 삭제되지 않았기 때문에 캐시도 유지됩니다.

---

## Redis JSON 직렬화

기본 Redis Cache 설정을 사용하면 값이 Java 직렬화 형태로 저장되어 Redis CLI에서 사람이 읽기 어렵습니다.

기본 Java 직렬화 예시:

```text
\xac\xed\x00\x05sr...
```

이를 개선하기 위해 `RedisCacheConfig`를 추가하고 Redis 캐시 값을 JSON 형태로 직렬화하도록 설정했습니다.

Redis 저장 예시:

```json
{
  "@class": "com.example.mvccrud.book.BookResponse",
  "id": 1,
  "title": "테스트 책",
  "price": 15000,
  "createdAt": "2026-07-08T15:55:28.938482",
  "updatedAt": "2026-07-26T18:44:46.936939"
}
```

---

## BookResponse record 변경

Redis JSON 역직렬화를 위해 `BookResponse`를 record로 변경했습니다.

기존 클래스는 `Book`을 받는 생성자만 가지고 있었기 때문에 Jackson이 Redis에 저장된 JSON을 다시 `BookResponse` 객체로 복원하지 못했습니다.

record로 변경하면 필드 기반 생성자가 자동으로 제공되어 JSON 데이터를 객체로 복원하기 쉽습니다.

```java
public record BookResponse(
    Long id,
    String title,
    int price,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) implements Serializable {

    public BookResponse(Book book) {
        this(
            book.getId(),
            book.getTitle(),
            book.getPrice(),
            book.getCreatedAt(),
            book.getUpdatedAt()
        );
    }
}
```

---

## GitHub Actions CI

GitHub Actions를 사용해 CI 환경을 구성했습니다.

목표:

```text
main 브랜치에 push 또는 pull request 발생 시 자동으로 테스트 실행
```

Workflow 파일:

```text
.github/workflows/ci.yml
```

예시:

```yaml
name: Java CI

on:
  push:
    branches:
      - main
  pull_request:
    branches:
      - main

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout source code
        uses: actions/checkout@v4

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
          cache: gradle

      - name: Grant execute permission for Gradle
        run: chmod +x gradlew

      - name: Run tests
        run: ./gradlew clean test
```

GitHub Actions에서 테스트가 성공하면 초록 체크가 표시됩니다.

---

## 테스트

### Service Test

검증 내용:

- 등록
- 단건 조회
- 전체 조회
- 수정
- 부분 수정
- 삭제
- 검색
- 예외 상황 검증

대상:

```text
BookServiceTest
MemberServiceTest
OrderServiceTest
```

---

### Controller Test

MockMvc를 사용해 API 요청 / 응답을 검증했습니다.

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

standalone MockMvc 테스트에서는 Pageable을 처리하기 위해 다음 설정을 추가했습니다.

```java
.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
```

SortValidator를 Controller 생성자에 추가한 뒤 테스트 코드에서도 `new SortValidator()`를 주입하도록 수정했습니다.

---

### JPA Integration Test

실제 Spring Context, H2 Database, JPA를 사용한 통합 테스트를 작성했습니다.

검증 내용:

- DB 저장
- DB 조회
- 변경 감지
- 삭제
- 검색
- 주문 취소
- 예외 상황

대상:

```text
BookJpaIntegrationTest
MemberJpaIntegrationTest
OrderJpaIntegrationTest
```

---

## 해결한 문제

### Repository 메서드 반환 타입 충돌

`JpaRepository`와 직접 만든 Repository 인터페이스를 함께 상속할 때, 겹치는 메서드의 반환 타입이 다르면 충돌이 발생했습니다.

해결:

```java
Optional<Member> findById(Long id);
List<Member> findAll();
```

---

### JPQL 문법 오류

JPQL에서는 테이블명이 아니라 엔티티명을 기준으로 조회해야 합니다.

잘못된 예:

```java
select o from Order order o
```

올바른 예:

```java
select o from Order o
```

---

### JPQL 파라미터 공백 오류

JPQL 파라미터는 `:` 뒤에 공백 없이 작성해야 합니다.

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

### SQL 예약어 충돌

`Order`는 SQL 예약어와 충돌할 수 있기 때문에 테이블명을 `orders`로 변경했습니다.

```java
@Entity
@Table(name = "orders")
public class Order {
}
```

---

### 변경 감지 미동작

주문 취소 후 다시 조회했을 때 상태가 변경되지 않는 문제가 있었습니다.

원인은 상태 변경 메서드에 트랜잭션이 적용되지 않았기 때문이었습니다.

```java
@Transactional
public Order cancelOrder(Long id) {
    Order order = findOrder(id);
    order.cancel();
    return order;
}
```

---

### H2 파일 DB Lock 문제

H2 파일 DB를 여러 프로세스가 동시에 잡고 있으면 다음 문제가 발생했습니다.

```text
Database may be already in use
```

해결 방법:

- 중복 실행 중인 서버 종료
- 별도로 실행한 H2 앱 종료
- Spring Boot 앱에서 제공하는 `/h2-console` 사용

---

### H2 Console 404 문제

Spring Boot 4 환경에서 H2 Console이 404로 뜨는 문제가 있었습니다.

해결을 위해 H2 Console 관련 의존성을 추가했습니다.

```gradle
developmentOnly 'org.springframework.boot:spring-boot-h2console'
```

---

### 잘못된 Page import 문제

OrderController에서 `Page` import를 잘못 가져와 컴파일 오류가 발생했습니다.

잘못된 import:

```java
import org.hibernate.query.Page;
```

올바른 import:

```java
import org.springframework.data.domain.Page;
```

---

### MemoryRepository 검색 페이징 오류

검색 페이징 구현에서 조건 검색을 하지 않고 전체 데이터를 페이징해 테스트가 실패했습니다.

잘못된 코드:

```java
List<Order> orders = new ArrayList<>(store.values());
```

올바른 코드:

```java
List<Order> orders = search(memberId, status);
```

---

### LazyInitializationException

`open-in-view=false` 설정 후 Controller에서 Lazy Loading 필드에 접근하면서 문제가 발생했습니다.

해결:

```text
DTO 변환을 Service 트랜잭션 안에서 수행
```

---

### N+1 문제

Order 목록 조회 시 주문 수만큼 Member와 Book 조회가 추가로 발생했습니다.

해결:

```java
@EntityGraph(attributePaths = {"member", "book"})
```

---

### Redis JSON 역직렬화 실패

Redis에 JSON으로 저장된 `BookResponse`를 다시 객체로 복원할 때 생성자 문제로 실패했습니다.

해결:

```text
BookResponse를 record로 변경
```

---

### Docker Java 버전 불일치

Gradle toolchain은 Java 17인데 Dockerfile이 Java 21 이미지를 사용해 빌드에 실패했습니다.

해결:

```dockerfile
FROM eclipse-temurin:17-jdk AS builder
FROM eclipse-temurin:17-jre
```

---

### Docker DB 이름 불일치

`docker-compose.yml`의 DB 이름과 `application-docker.yml`의 JDBC URL이 달라 app 컨테이너가 실행되지 않았습니다.

잘못된 예:

```text
mvc-crud
```

올바른 예:

```text
mvc_crud
```

---

## 현재 프로젝트 상태

완료된 항목:

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
- MySQL Docker 환경 구성
- Redis Docker 환경 구성
- Docker Compose 기반 App + MySQL + Redis 실행 환경 구성
- 더미 데이터 자동 삽입
- JPQL 검색 기능 구현
- Pageable 기반 페이징 / 정렬 구현
- 정렬 가능한 필드 제한
- JPA Auditing 적용
- BaseEntity 적용
- Order와 Member, Book 간 `@ManyToOne` 연관관계 적용
- Lazy Loading 문제 해결
- N+1 문제 확인 및 `@EntityGraph` 최적화
- Redis Cache 적용
- Redis JSON 직렬화 적용
- Swagger / OpenAPI 적용
- Controller 단위 테스트 작성
- JPA 통합 테스트 작성
- GitHub Actions CI 구성
- GitHub push 시 자동 테스트 성공 확인

---

## 배운 핵심

### Spring MVC

- Controller는 HTTP 요청과 응답을 담당한다.
- Service는 비즈니스 흐름을 담당한다.
- Repository는 저장소 접근을 담당한다.
- DTO를 사용하면 API 스펙과 내부 모델을 분리할 수 있다.
- Validation은 잘못된 요청을 빠르게 막아준다.
- GlobalExceptionHandler는 예외 응답을 일관되게 만든다.

### JPA

- Entity는 DB 테이블과 매핑된다.
- `@Id`, `@GeneratedValue`로 식별자를 관리한다.
- `@Transactional` 안에서 Entity 값을 변경하면 변경 감지가 동작한다.
- enum은 `@Enumerated(EnumType.STRING)`으로 저장하는 것이 안전하다.
- SQL 예약어와 엔티티명이 충돌하면 `@Table`로 테이블명을 지정한다.
- `@ManyToOne(fetch = FetchType.LAZY)`로 다대일 연관관계를 표현할 수 있다.
- Lazy Loading은 트랜잭션 범위 안에서 다뤄야 한다.
- N+1 문제는 fetch join이나 EntityGraph로 개선할 수 있다.

### Redis

- Redis는 JPA를 타지 않는 별도의 메모리 저장소다.
- Cache Hit이면 DB 조회 없이 Redis 값을 반환한다.
- Cache Miss이면 DB에서 조회한 뒤 Redis에 저장한다.
- 수정 / 삭제 시 캐시 무효화가 필요하다.
- JSON 직렬화를 적용하면 Redis CLI에서 캐시 값을 사람이 읽기 쉬워진다.

### Docker

- Docker Compose로 애플리케이션과 인프라를 함께 실행할 수 있다.
- 컨테이너 내부에서는 `localhost`가 아니라 서비스 이름으로 통신한다.
- MySQL은 `mysql`, Redis는 `redis`로 접근한다.
- Dockerfile의 Java 버전과 Gradle toolchain 버전을 맞춰야 한다.

### CI

- GitHub Actions로 push 시 테스트를 자동 실행할 수 있다.
- CI가 통과하면 변경사항이 기존 테스트를 깨뜨리지 않았다는 최소한의 안전장치가 생긴다.

---

## 다음 개선 예정

- Spring Boot Actuator health check 추가
- 클라우드 배포
- 운영 환경용 환경변수 분리
- Docker 이미지 빌드 및 배포 자동화
- 인증 / 인가 적용
- 로그인 기능 추가
- 회원별 주문 조회 API 추가
- 주문 구조를 `OrderItem` 기반으로 확장
- Redis TTL 전략 정리
- 캐시 대상 확장
- API 에러 응답 구조 추가 개선
- Kubernetes 맛보기


## Actuator Health Check

Spring Boot Actuator를 추가하여 애플리케이션 상태 확인 API를 제공한다.

### Health Check

```http
GET /actuator/health