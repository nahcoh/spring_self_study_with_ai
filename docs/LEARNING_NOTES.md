# Spring MVC CRUD Practice 학습 노트

이 문서는 `Spring MVC CRUD Practice` 프로젝트를 구현하면서 학습한 내용, 만난 문제, 해결 과정, 기술 선택 이유를 정리한 학습 기록입니다.

README는 프로젝트를 보여주기 위한 문서이고, 이 문서는 학습 과정을 복기하기 위한 문서입니다.

---

## 2026-07-26

Redis 캐시, Docker Compose, GitHub Actions CI까지 연결 성공.

---

## 2026-07-30

AWS EC2 Ubuntu 서버에 Docker Compose 기반으로 Spring Boot App, MySQL, Redis 배포 성공.

Health Check 응답:

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

---

# 1. 프로젝트 시작 목적

처음 목표는 단순한 CRUD API를 만드는 것이었습니다.

하지만 단순히 Controller에서 요청을 받고 Repository에 저장하는 수준이 아니라, 실제 백엔드 애플리케이션이 어떤 구조로 확장되는지 직접 경험하는 것을 목표로 삼았습니다.

최종적으로 다음 흐름을 경험했습니다.

```text
MemoryRepository
→ Spring Data JPA
→ H2
→ MySQL
→ Redis Cache
→ Docker Compose
→ GitHub Actions CI
→ Actuator Health Check
→ AWS EC2 배포
```

---

# 2. 계층 구조 학습

## Controller

Controller는 HTTP 요청과 응답을 담당합니다.

역할:

- URL 매핑
- Request DTO 받기
- Validation 적용
- Pageable 요청 받기
- Service 호출
- Response DTO 반환
- HTTP Status Code 반환

Controller는 비즈니스 로직을 직접 처리하지 않고 Service에 위임해야 합니다.

---

## Service

Service는 비즈니스 흐름을 담당합니다.

역할:

- 등록
- 조회
- 목록 조회
- 수정
- 부분 수정
- 삭제
- 검색
- 주문 생성
- 주문 취소
- 예외 처리
- 트랜잭션 관리

Service에서 가장 중요했던 점은 트랜잭션 범위였습니다.

JPA 변경 감지, Lazy Loading, DTO 변환 위치를 이해하려면 Service의 트랜잭션 범위를 이해해야 했습니다.

---

## Repository

Repository는 저장소 접근을 담당합니다.

처음에는 메모리 기반 Repository를 만들었습니다.

```text
BookRepository
 ├── MemoryBookRepository
 └── JpaBookRepository
```

Service는 `MemoryBookRepository`나 `JpaBookRepository` 같은 구체 클래스에 직접 의존하지 않고, `BookRepository` 인터페이스에 의존하도록 만들었습니다.

이를 통해 저장소 구현체가 바뀌어도 Service 코드를 크게 바꾸지 않는 구조를 경험했습니다.

---

# 3. DTO를 사용하는 이유

Entity를 API 요청/응답에 직접 사용하지 않기 위해 DTO를 분리했습니다.

사용한 DTO 예시:

```text
BookCreateRequest
BookUpdateRequest
BookPatchRequest
BookSearchRequest
BookResponse
```

DTO를 분리하면 다음 장점이 있습니다.

- API 스펙과 내부 Entity 구조를 분리할 수 있다.
- 요청 검증을 DTO에 적용할 수 있다.
- 응답에서 필요한 필드만 노출할 수 있다.
- Entity 변경이 곧바로 API 변경으로 이어지는 것을 막을 수 있다.

---

# 4. 공통 응답 구조

성공 응답은 `ApiResponse<T>`로 감쌌습니다.

예시:

```json
{
  "data": {
    "id": 1,
    "title": "데미안",
    "price": 15000
  }
}
```

목록 응답은 `PageResponse<T>`로 통일했습니다.

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

배운 점:

- API 응답 구조를 통일하면 클라이언트가 응답을 예측하기 쉽다.
- Page 객체 전체를 그대로 노출하지 않고 필요한 정보만 가공하는 것이 좋다.
- record는 값 전달용 DTO에 적합하다.

---

# 5. Validation 학습

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
```

배운 점:

- Controller 진입 시점에 잘못된 요청을 빠르게 차단할 수 있다.
- Validation 실패는 `MethodArgumentNotValidException`으로 처리할 수 있다.
- GlobalExceptionHandler를 사용하면 에러 응답을 일관되게 만들 수 있다.

---

# 6. 예외 처리 학습

공통 예외 처리를 위해 `GlobalExceptionHandler`를 사용했습니다.

처리한 예외:

```text
BookNotFoundException
MemberNotFoundException
OrderNotFoundException
DuplicateEmailException
InvalidSortException
MethodArgumentNotValidException
IllegalArgumentException
IllegalStateException
DataIntegrityViolationException
```

배운 점:

- 없는 리소스 조회는 `404 Not Found`
- 검증 실패는 `400 Bad Request`
- 중복 이메일은 `400 Bad Request`
- 잘못된 상태 변경은 `400 Bad Request`
- 참조 중인 데이터 삭제 실패는 `409 Conflict`

---

# 7. JPA 전환

처음에는 `MemoryRepository`로 데이터를 저장했지만, 이후 Spring Data JPA로 전환했습니다.

전환 목적:

- 실제 DB 기반 저장 구조 경험
- Entity 매핑 학습
- Repository 추상화 학습
- 트랜잭션과 변경 감지 학습
- 통합 테스트 작성

JPA Repository 예시:

```java
public interface JpaBookRepository extends JpaRepository<Book, Long>, BookRepository {
}
```

문제:

`JpaRepository`와 직접 만든 Repository 인터페이스를 함께 상속할 때, 같은 메서드의 반환 타입이 다르면 충돌이 발생했습니다.

해결:

`findById`, `findAll` 같은 메서드는 `JpaRepository`의 시그니처와 맞췄습니다.

```java
Optional<Member> findById(Long id);
List<Member> findAll();
```

배운 점:

- Spring Data JPA를 직접 만든 Repository 인터페이스와 함께 쓸 때는 메서드 시그니처를 조심해야 한다.
- Service가 Repository 인터페이스에 의존하면 저장소 구현체 변경이 쉬워진다.

---

# 8. Entity 매핑

## Book

```java
@Entity
@NoArgsConstructor
@Getter
public class Book extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private int price;
}
```

## Member

```java
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
}
```

## Order

```java
@Entity
@Table(name = "orders")
@NoArgsConstructor
@Getter
public class Order extends BaseEntity {

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

배운 점:

- `Order`는 SQL 예약어와 충돌할 수 있으므로 테이블명을 `orders`로 지정했다.
- enum은 `EnumType.STRING`으로 저장하는 것이 안전하다.
- 다대일 관계는 `@ManyToOne`으로 표현할 수 있다.
- 기본 fetch 전략을 그대로 쓰기보다 명시적으로 `LAZY`를 설정하는 것이 좋다.

---

# 9. JPQL 검색

Book, Member, Order에 검색 기능을 구현했습니다.

Book 검색:

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

문제:

JPQL 문법에서 엔티티명을 잘못 쓰거나 파라미터 앞에 공백을 넣어 오류가 발생했습니다.

잘못된 예:

```java
select o from Order order o
o.member.id = : memberId
```

올바른 예:

```java
select o from Order o
o.member.id = :memberId
```

배운 점:

- JPQL은 테이블명이 아니라 Entity명을 기준으로 작성한다.
- `:` 뒤에는 공백 없이 파라미터명을 작성해야 한다.
- 검색 조건이 없으면 무시하고, 있으면 조건에 포함하는 방식으로 동적 검색을 구성할 수 있다.

---

# 10. 트랜잭션과 변경 감지

주문 취소 기능에서 JPA 변경 감지를 학습했습니다.

```java
@Transactional
public Order cancelOrder(Long id) {
    Order order = findOrder(id);
    order.cancel();
    return order;
}
```

처음 문제:

주문 취소 후 다시 조회했을 때 상태가 변경되지 않았습니다.

원인:

트랜잭션이 없는 상태에서 Entity 값을 변경했기 때문에 변경 감지가 동작하지 않았습니다.

해결:

상태 변경 메서드에 `@Transactional`을 적용했습니다.

배운 점:

- JPA 변경 감지는 트랜잭션 안에서 동작한다.
- Entity의 상태 변경은 setter보다 의미 있는 행위 메서드로 표현하는 것이 좋다.
- `cancel()` 같은 도메인 메서드는 단순 필드 변경보다 의도가 명확하다.

---

# 11. JPA Auditing

생성일과 수정일을 자동 관리하기 위해 JPA Auditing을 적용했습니다.

BaseEntity:

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

메인 클래스:

```java
@EnableJpaAuditing
@SpringBootApplication
public class MvcCrudApplication {
}
```

배운 점:

- 공통 필드는 `BaseEntity`로 분리할 수 있다.
- `@MappedSuperclass`는 부모 클래스의 필드를 자식 Entity 테이블에 포함시킨다.
- `@CreatedDate`는 생성 시간을 자동 저장한다.
- `@LastModifiedDate`는 수정 시간을 자동 갱신한다.
- JPA Auditing을 사용하려면 `@EnableJpaAuditing`이 필요하다.
- 기존 DB 데이터에는 새로 추가된 시간 컬럼이 `null`일 수 있다.

---

# 12. Lazy Loading과 OSIV

`spring.jpa.open-in-view=false`를 설정했습니다.

이 설정을 사용하면 Service 계층의 트랜잭션이 끝난 뒤 Controller에서 Lazy Loading이 발생할 수 없습니다.

문제:

Controller에서 `OrderResponse`를 만들 때 다음 코드가 실행되었습니다.

```java
order.getMember().getName()
order.getBook().getTitle()
```

하지만 Service 트랜잭션이 이미 끝난 뒤였기 때문에 `LazyInitializationException`이 발생했습니다.

원인:

```text
Service 트랜잭션 종료
→ Controller에서 Lazy 필드 접근
→ 영속성 컨텍스트 종료 상태
→ Lazy Loading 실패
```

해결:

DTO 변환을 Service 트랜잭션 안에서 수행했습니다.

```java
public OrderResponse findOrderResponse(Long id) {
    Order order = findOrder(id);
    return new OrderResponse(order);
}
```

배운 점:

- Lazy Loading은 영속성 컨텍스트가 열려 있어야 동작한다.
- OSIV를 끄면 Controller에서 Lazy Loading을 기대하면 안 된다.
- DTO 변환 위치도 트랜잭션과 관련이 있다.
- Service 계층에서 응답 DTO 변환을 수행하면 Lazy Loading 문제를 줄일 수 있다.

---

# 13. N+1 문제와 EntityGraph

Order 목록 조회에서 N+1 문제가 발생했습니다.

기존 흐름:

```text
orders 조회 1번
member 조회 N번
book 조회 N번
```

원인:

`Order`는 `Member`, `Book`을 LAZY로 가지고 있고, 응답 DTO 변환 과정에서 `memberName`, `bookTitle`에 접근했기 때문입니다.

해결:

`@EntityGraph`를 적용했습니다.

```java
@Override
@EntityGraph(attributePaths = {"member", "book"})
Page<Order> findAll(Pageable pageable);
```

검색 API에도 적용했습니다.

```java
@EntityGraph(attributePaths = {"member", "book"})
@Query("""
    select o from Order o
    where (:memberId is null or o.member.id = :memberId)
    and (:status is null or o.status = :status)
    """)
Page<Order> search(Long memberId, OrderStatus status, Pageable pageable);
```

배운 점:

- LAZY는 무조건 좋은 것이 아니라 조회 패턴에 따라 최적화가 필요하다.
- 목록 조회에서 연관 객체를 응답에 사용하면 N+1 문제가 발생할 수 있다.
- `@EntityGraph`를 사용하면 필요한 연관 객체를 함께 조회할 수 있다.
- fetch join과 EntityGraph는 N+1 문제를 해결하는 대표적인 방법이다.

---

# 14. 페이징과 정렬

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

Controller에서는 기본값을 설정했습니다.

```java
@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
Pageable pageable
```

application.yml에는 최대 크기를 제한했습니다.

```yaml
spring:
  data:
    web:
      pageable:
        default-page-size: 10
        max-page-size: 50
        one-indexed-parameters: false
```

배운 점:

- `Pageable`은 page, size, sort 정보를 담는다.
- `Page<T>`는 데이터와 페이징 메타데이터를 함께 가진다.
- `Page.map()`을 사용하면 `Page<Entity>`를 `Page<ResponseDto>`로 변환할 수 있다.
- API 응답에서는 `PageResponse<T>`로 필요한 정보만 노출하는 것이 좋다.

---

# 15. MemoryRepository 페이징

JPA Repository는 Pageable을 자동 처리하지만, MemoryRepository는 직접 리스트를 잘라야 했습니다.

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

검색 페이징 문제:

조건 검색 결과를 페이징해야 하는데 전체 데이터를 페이징해서 테스트가 실패했습니다.

잘못된 코드:

```java
List<Order> orders = new ArrayList<>(store.values());
```

올바른 코드:

```java
List<Order> orders = search(memberId, status);
```

배운 점:

- 페이징은 전체 데이터가 아니라 최종 검색 결과를 대상으로 해야 한다.
- 테스트가 있어야 이런 실수를 빠르게 발견할 수 있다.

---

# 16. 정렬 가능한 필드 제한

Swagger에서 잘못된 sort 값이 들어오거나, 존재하지 않는 필드로 정렬을 요청하면 JPA 오류가 발생할 수 있었습니다.

해결:

`SortValidator`를 추가했습니다.

```java
@Component
public class SortValidator {

    public void validate(Pageable pageable, Set<String> allowedFields) {
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();

            if (!allowedFields.contains(property)) {
                throw new InvalidSortException(property);
            }
        }
    }
}
```

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

응답 예시:

```json
{
  "status": 400,
  "message": "정렬할 수 없는 필드입니다: unknownField"
}
```

배운 점:

- 클라이언트 입력값을 그대로 Repository까지 보내면 안 된다.
- 정렬 가능한 필드는 API 정책으로 제한할 수 있다.
- 잘못된 요청은 500이 아니라 400으로 응답하는 것이 적절하다.

---

# 17. Redis 캐시 적용

책 단건 조회 API에 Redis 캐시를 적용했습니다.

대상:

```http
GET /books/{id}
```

흐름:

```text
첫 번째 GET /books/1
→ Cache Miss
→ MySQL 조회
→ Redis 저장

두 번째 GET /books/1
→ Cache Hit
→ MySQL 조회 없음
→ Redis 값 반환
```

Service 메서드:

```java
@Cacheable(value = "book", key = "#id")
public BookResponse findBookResponse(Long id) {
    Book book = findBook(id);
    return new BookResponse(book);
}
```

배운 점:

- Redis는 JPA를 타지 않는 별도의 메모리 저장소다.
- Cache Hit이면 Repository, JPA, MySQL까지 가지 않는다.
- Cache Miss이면 DB에서 조회한 뒤 Redis에 저장한다.
- 자주 조회되고 자주 바뀌지 않는 데이터가 캐시에 적합하다.

---

# 18. 캐시 무효화

책이 수정되거나 삭제되면 Redis에 남아 있는 기존 캐시는 낡은 데이터가 됩니다.

그래서 수정 / 삭제 시 캐시를 삭제했습니다.

```text
PUT /books/{id}    → book::{id} 캐시 삭제
PATCH /books/{id}  → book::{id} 캐시 삭제
DELETE /books/{id} → 삭제 성공 시 book::{id} 캐시 삭제
```

배운 점:

- 캐시는 조회 성능을 올리지만 데이터 불일치 위험을 만든다.
- 수정 / 삭제 시 캐시 무효화가 필요하다.
- 삭제가 실패하면 실제 데이터가 남아 있으므로 캐시도 유지되는 것이 자연스럽다.

---

# 19. Redis JSON 직렬화

처음 Redis에 저장된 값은 Java 직렬화 형태였습니다.

```text
\xac\xed\x00\x05sr...
```

문제:

Redis CLI에서 사람이 읽기 어려웠습니다.

해결:

`RedisCacheConfig`를 추가해 JSON 직렬화로 변경했습니다.

저장 예시:

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

배운 점:

- Redis 값 직렬화 방식을 바꿀 수 있다.
- JSON 직렬화는 사람이 확인하기 쉽다.
- 기존 Java 직렬화 캐시가 남아 있으면 설정 변경 후 문제가 생길 수 있으므로 `flushall`로 비우고 테스트하는 것이 좋다.

---

# 20. BookResponse record 변경

Redis JSON 역직렬화 과정에서 문제가 발생했습니다.

에러 요지:

```text
Cannot construct instance of BookResponse
no Creators, like default constructor, exist
```

원인:

기존 `BookResponse`는 `Book`을 받는 생성자만 가지고 있었습니다.

```java
public BookResponse(Book book) {
    ...
}
```

Jackson은 Redis에 저장된 JSON의 `id`, `title`, `price`, `createdAt`, `updatedAt` 값을 넣어 객체를 만들 생성자를 찾지 못했습니다.

해결:

`BookResponse`를 record로 변경했습니다.

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

배운 점:

- record는 값 전달용 DTO에 적합하다.
- record는 필드 기반 생성자를 자동으로 제공한다.
- JSON 직렬화 / 역직렬화에 유리하다.
- JPA Entity에는 record를 사용하지 않는 것이 좋다.

---

# 21. Docker Compose

처음에는 MySQL만 Docker Compose로 실행했습니다.

이후 Redis와 Spring Boot App까지 Compose에 포함했습니다.

구성:

```text
app
mysql
redis
```

배운 점:

- Docker Compose로 애플리케이션과 인프라를 한 번에 실행할 수 있다.
- 컨테이너 내부에서는 `localhost`가 아니라 서비스 이름으로 통신해야 한다.
- MySQL은 `mysql`, Redis는 `redis`로 접근한다.
- `depends_on`과 healthcheck를 사용하면 의존 컨테이너가 준비된 뒤 app을 실행할 수 있다.

---

# 22. Docker Java 버전 문제

Dockerfile에서 Java 21 이미지를 사용했는데, Gradle toolchain은 Java 17이었습니다.

에러 요지:

```text
Cannot find a Java installation matching languageVersion=17
```

원인:

Docker builder 이미지에는 Java 21만 있고, 프로젝트는 Java 17 toolchain을 요구했습니다.

해결:

Dockerfile을 Java 17로 변경했습니다.

```dockerfile
FROM eclipse-temurin:17-jdk AS builder
FROM eclipse-temurin:17-jre
```

배운 점:

- Dockerfile의 Java 버전과 Gradle toolchain 버전은 맞춰야 한다.
- 로컬에서 잘 돌아가도 Docker 빌드 환경에서는 다르게 실패할 수 있다.

---

# 23. Docker DB 이름 불일치

EC2 배포 전 로컬 Docker Compose 실행 중 app 컨테이너가 죽었습니다.

에러 요지:

```text
Access denied for user 'mvc_user'@'%' to database 'mvc-crud'
```

원인:

`docker-compose.yml`의 DB 이름은 `mvc_crud`였는데, `application-docker.yml`에서는 `mvc-crud`로 접속하려고 했습니다.

잘못된 값:

```text
mvc-crud
```

올바른 값:

```text
mvc_crud
```

배운 점:

- DB 이름의 하이픈과 언더스코어 차이도 완전히 다른 이름으로 처리된다.
- 환경 설정 파일과 docker-compose 환경변수 값은 반드시 일치해야 한다.

---

# 24. 환경변수 분리

처음에는 `application-docker.yml`에 DB 주소와 계정 정보가 직접 들어 있었습니다.

클라우드 배포를 고려해 환경변수로 분리했습니다.

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
```

docker-compose.yml:

```yaml
app:
  environment:
    SPRING_PROFILES_ACTIVE: docker
    DB_URL: jdbc:mysql://mysql:3306/mvc_crud?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    DB_USERNAME: mvc_user
    DB_PASSWORD: mvc_password
    REDIS_HOST: redis
    REDIS_PORT: 6379
```

문제:

`REDIS_PORT`를 `REDIS_PROT`로 오타내서 app 컨테이너가 실행되지 않았습니다.

에러 요지:

```text
Failed to bind properties under 'spring.data.redis.port' to int
Value: "${REDIS_PROT}"
```

해결:

```yaml
port: ${REDIS_PORT}
```

배운 점:

- 환경변수 이름 오타도 런타임 장애로 이어진다.
- Docker Compose logs를 보면 app 컨테이너 실패 원인을 찾을 수 있다.
- 클라우드 배포 전에는 로컬 Docker Compose로 먼저 검증하는 것이 좋다.

---

# 25. Actuator Health Check

Spring Boot Actuator를 추가했습니다.

목표:

```http
GET /actuator/health
```

응답:

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

설정:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: never
```

문제:

처음에 `management.endpoint.web.exposure`로 잘못 작성했습니다.

올바른 설정은 `endpoints` 복수형입니다.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
```

또 다른 문제:

build.gradle에서 actuator 의존성에 오타가 있었습니다.

잘못된 예:

```gradle
implementation 'org.springframework.boot::spring-boot-starter-actuator'
```

올바른 예:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

배운 점:

- Actuator는 서버 상태를 확인하는 데 사용된다.
- Docker, 클라우드, Kubernetes에서 health check는 중요하다.
- YAML 속성 이름은 정확해야 한다.
- Gradle 의존성 표기에서 콜론 하나 차이도 빌드 실패를 만든다.

---

# 26. GitHub Actions CI

GitHub Actions로 CI를 구성했습니다.

목표:

```text
main 브랜치에 push 또는 pull request 발생 시 자동으로 테스트 실행
```

workflow:

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

문제:

SortValidator를 Controller 생성자에 추가한 뒤 ControllerTest가 깨졌습니다.

원인:

테스트 코드에서는 여전히 예전 생성자를 사용하고 있었습니다.

```java
new BookController(bookService)
```

해결:

```java
new BookController(bookService, new SortValidator())
```

배운 점:

- 생성자 의존성이 바뀌면 테스트 코드도 함께 수정해야 한다.
- CI가 있으면 push 후 자동으로 테스트 실패 여부를 확인할 수 있다.
- 초록불은 최소한 기존 테스트가 깨지지 않았다는 신호다.

---

# 27. AWS EC2 배포

AWS EC2 Ubuntu 서버에 Docker Compose 기반으로 배포했습니다.

구성:

```text
EC2 Ubuntu 24.04
Docker
Docker Compose
Spring Boot App
MySQL
Redis
```

보안 그룹:

```text
22   → SSH
80   → HTTP
8080 → Spring Boot API
```

EC2 접속:

```bash
chmod 400 mvc-crud-key.pem
ssh -i mvc-crud-key.pem ubuntu@<EC2_PUBLIC_IP>
```

Docker 설치 확인:

```bash
docker --version
docker compose version
```

프로젝트 clone:

```bash
git clone https://github.com/nahcoh/spring_self_study_with_ai.git
cd spring_self_study_with_ai
```

배포:

```bash
docker compose up -d --build
```

확인:

```bash
docker ps
docker logs -f mvc-crud-app
```

Health Check:

```http
GET http://<EC2_PUBLIC_IP>:8080/actuator/health
```

응답:

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

배운 점:

- EC2 보안 그룹에서 8080 포트를 열어야 외부 접속이 가능하다.
- EC2 내부에 Docker와 Docker Compose를 설치해 컨테이너 기반 배포를 할 수 있다.
- GitHub 저장소를 clone한 뒤 Docker Compose로 배포할 수 있다.
- 서버에서는 `git pull` 후 `docker compose up -d --build`로 변경사항을 반영할 수 있다.
- 이제 프로젝트는 로컬뿐 아니라 인터넷에서 접근 가능한 서버에 배포된 상태가 되었다.

---

# 28. Git 보안 실수 방지

EC2 키 파일인 `.pem` 파일이 Git에 올라갈 뻔했습니다.

문제:

```text
mvc-crud-key.pem
```

이 파일은 절대 GitHub에 올리면 안 됩니다.

해결:

`.gitignore`에 추가했습니다.

```gitignore
# Local database files
data/

# AWS key files
*.pem
```

배운 점:

- AWS 키 파일은 절대 커밋하면 안 된다.
- `git status`에서 untracked 파일을 항상 확인해야 한다.
- 실수로 `git add .`를 하기 전에 민감한 파일이 포함되어 있는지 확인해야 한다.


---
# 29. Nginx Reverse Proxy

EC2에 배포한 Spring Boot 애플리케이션은 기본적으로 8080 포트에서 실행된다.

처음에는 다음 주소로 직접 접근했다.
```text
http://<EC2_PUBLIC_IP>:8080
```
하지만 운영 환경에서는 애플리케이션 포트를 외부에 직접 노출하기보다 Nginx가 80번 포트에서 요청을 받고 내부의 Srping Boot 8080 포트를 전달하는 구조를 많이 사용한다.

적용 후 구조:
```text
Client
  ↓
Nginx :80
  ↓
Spring Boot :8080
```
Nginx 설정 중 오타로 문제가 발생했다.

잘못된 설정: 
```text
server_name_
```
올바른 설정: 
```text
server_name _;
```
`server_name`과 `_` 사이에는 공백이 있어야 하고, 마지막에는 세미콜론이 필요하다.

배운 점:
- Nginx는 Reverse Proxy로 사용할 수 있다. 
- 외부에는 80 포트만 열고, Spring Boot 8080 포트는 내부에서만 사용하게 만들 수 있다.
- `sudo nginx -t`로 설정 문법을 검사한 뒤 재시작해야 한다.
- Nginx설정은 작은 오타 하나로도 실행 실패할 수 있다.


---
# 30. GitHub Actions EC2 자동 배포
기존에는 EC2에 직접 SSH 접속해서 수동으로 배포했다.
```bash
git pull
docker compose up -d --build
```
이를 Github Action workflow에 추가해 main브랜치에 push하면 자동 배포되도록 만들었다.

### 발생한 문제
처음에는 deploy 단계에서 다음 오류가 발생했다.
```text
ssh.ParsePrivateKey: ssh: no key found
dial tcp ***:22: i/o timeout
```
원인은 두 가지였다.
1. GitHub Secret에 등록한 `EC2_SSH_KEY`값이 올바른 private key 형식이 아니었다.
2. EC2 보안그룹에서 SSH 22번 포트가 내 IP만 허용되어 있어 GitHub Actions 서버가 접속할 수 없었다.

### 해결 
- `mvc-crud-key.pem` 내용을 줄바꿈 포함해서 Github Secret에 다시 등록했다.
- EC2 보안그룹에서 SSH 22번을 GitHub Actions가 접근할 수 있도록 수정했다.
- workflow에서는 `secrets.EC2_HOST`,`secrets.EC2_USER`,`secrets.EC2_SSH_KEY`를 사용했다.

### 배운 점
- GitHub Actions에서 EC2로 배포하려면 SSH key를 Github Secrets에 안전하게 저장해야 한다.
- Secret 이름은 workflow에서 사용하는 이름과 정확히 일치해야 한다.
- `secret`이 아니라 `secrets`를 사용해야 한다.
- EC2 보안그룹이 막혀 있으면 workflow에서 SSH 접속이 timeout 된다.
- CI/CD가 구성되면 push 이후 테스트와 배포가 자동화된다.
---
# 31. GHCR 기반 Docker 이미지 배포
기존 배포 방식은 EC2 서버에서 직접 Dokcer 이미지를 빌드하는 구조였다.
```bash
git pull origin main
docker compose up -d --build
```
이 방식은 단순하지만 EC2 서버에 빌드 부담이 생긴다. 특히 t3.micro처럼 작은 서버에서는 Dokcer build가 메모리와 디스크를 많이 사용할 수 있다.

이를 개선하기 위해 GitHub Actions에서 Dokcer 이미지를 빌즈하고 GitHub Container Registry에 push하도록 변경했다.

변경 후 흐름:
```text
1. main 브랜치에 push
2. GitHub Actions에서 테스트 실행
3. 테스트 성공 시 Docker 이미지 빌드
4. GHCR에 latest와 commit sha 태그로 push
5. EC2에 SSH 접속
6. docker compose pull app
7. docker compose up -d
```
발생한 문제:
```text
unalbe to prepare context: path ".true" not found
```
원인:
`push: true` 또는 `context: .`설정이 잘못 작성되어 build context가 `.true`로 헤석되었다.
올바른 설정:
```yaml
context: .
push: true
```
배운 점:
- GitHub Actions에서 Docker 이미지를 빌드할 수 있다.
- GHCR에 이미지를 push하려면 `packages: write`권한이 필요하다.
- EC2에서 직접 빌드하지 않고 이미지를 pull하는 방식이 더 안정적이다.
- YAML 오타 하나로 workflow 해석이 완전히 달라질 수 있다.
---
# 32. Spring Security + JWT 학습 정리
## 1. 로그인은 왜 POST인가?
로그인은 단순 조회가 아니라 인증 처리 요청이다.

처리 흐름은 다음과 같다.
```text
email/password 제출
-> 서버가 회원 조회
-> 비밀번호 검증
-> 인증 성공 여부 판단
-> JWT Access Token 발급
```
GET 요청은 URL에 데이터가 노출될 수 있다.
```http request
GET /auth/login?email=kim@test.com&password=password1234
```
이 방식은 비밀번호가 브라우저 히스토리, 서버 로그, 프록시 로그 등에 남을 수 있기 때문에 부적절하다.

따라서 로그인은 요청 body에 자격 증명을 담을 수 있는 POST를 사용한다.
```http request
POST /auth/login
```

```json
{
  "email": "kim@test.com",
  "password": "password1234"
}
```
POST는 반드시 DB에 데이터를 저장한다는 뜻이 아니라, 서버에 어떤 처리를 요청한다는 의미로 볼 수 있다. 

로그인의 경우 서버는 입력된 자격 증명을 검증하고, 성공하면 JWT를 발급한다.

## JWT란?
JWT는 로그인 성공 후 서버가 발급하는 서명된 토큰이다.

클라이언트는 이후 요청마다 다음 헤더에 토큰을 담아 보낸다.
```http request
Authorization: Bearer <accessToken>
```
서버는 JWT를 검증해서 요청을 보낸 사용자가 누구인지 식별한다.

JWT는 암호화가 아니라 서명 기반이다. 
따라서 Payload는 누구나 디코딩할 수 있으므로 비밀번호나 민감정보를 넣으면 안된다.

현재 프로젝트에서는 JWT에 다음 정보를 담았다.
```text
subject: memberId
email: 회원 이메일
issueAt: 발급 시간
expiration: 만료 시간
```
## 3.JwtProvider의 역할
`JwtProvider`는 JWT를 생성하고 검증하는 책임을 가진다.
주요 역할:
```text
1. 로그인 성공 시 Access Token 생성
2. 토큰 유효성 검증
3. 토큰에서 memberId 추출
4. 토큰에서 email 추출
```
토큰 생성 흐름:
```java
Jwts.builder()
    .subject(String.valueOf(member.getId()))
    .claim("email", member.getEmail())
    .issuedAt(now)
    .expiration(expiration)
    .signWith(secretKey)
    .compact();
```
## 4.JwtAuthenticationFilter의 역할
`JwtAuthenticationFilter`는 요청마다 Authorization 헤더를 확인한다.
처리 흐름: 
```text
1. Authorization 헤더 확인
2. Bearer 토큰인지 확인
3. JWT 유효성 검증
4. 토큰에서 memberId/email 추출
5. CustomUserPrincipal 생성
6. Authentication 객체 생성
7. SecurityContextHolder에 저장
```
즉, 필터는 토큰을 검사해서 SpringSecurity가 이해할 수 있는 인증 정보로 바꿔주는 역할을 한다.

## 5. SecurityContext란? 
`SecurityContextHolder`는 현재 요청의 인증 정보를 저장하는 공간이다. 

JWT 필터가 인증에 성공한면 다음과 같이 인증 객체를 저장한다.
```java
SecurityContextHolder.getContext().setAuthentication(authentication);
```
이후 Controller나 Service에서는 현재 로그인 사용자를 참조할 수 있다. 

## 6. ScurityConfig의 역할
`SecurityConfig`는 어떤 API를 열고 잠글지를 정의한다.
현재 설정: 
```java
.requestMatchers(
    "/actuator/healt/**",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/h2-console/**",
    "/auth/login",
    "/members"
).permitAll()
.requestMatchers(HttpMethod.POST,"/orders").authenticated()
.anyRequest().permitAll()
```
의미:
```text
/auth/login, /members 등은 누구나 접근 가능
POST /orders는 로그인한 사용자만 접근 가능
나머지는 아직 임시로 허용
```
## 7. 이번 단계에서 배운 점
- 로그인은 단순 조회가 아니라 인증 처리 요청이므로 POST를 사용한다.
- 비밀번호는 URL에 노출되면 안되므로 GET 로그인은 부적절하다.
- JWT 발급은 로그인 성공의 결과다.
- JWT 필터는 Authorization 헤더의 토큰을 검증한다.
- 검증된 사용자 정보는 SecurityContext에 저장된다.
- `authenticated()`를 사용하면 특정 API를 로그인 사용자에게만 허용할 수 있다.
- 보안 설정은 단위 테스트보다 통합 테스트로 검증하는 것이 더 적합하다.
---
# . 현재까지의 한 줄 요약

```text
Spring Boot 기반 CRUD API를 구현하고, JPA/MySQL/Redis/Docker Compose/GitHub Actions/Actuator를 적용한 뒤 AWS EC2에 배포했다.
```

---

# 30. 다음 학습 후보

- HTTPS 적용
- 운영 DB 분리
- Docker 이미지 빌드 및 배포 자동화
- GitHub Actions에서 EC2 배포 자동화
- Spring Security 기반 로그인
- JWT 인증 / 인가
- 회원별 주문 조회 API
- OrderItem 기반 주문 구조 개선
- Redis TTL 전략 정리
- Kubernetes 맛보기