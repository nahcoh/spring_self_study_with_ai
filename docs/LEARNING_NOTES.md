# Spring MVC CRUD Practice 학습 노트

이 문서는 `Spring MVC CRUD Practice` 프로젝트를 구현하면서 학습한 내용, 만난 문제, 해결 과정, 기술 선택 이유를 정리한 학습 기록입니다.

README는 프로젝트를 보여주기 위한 문서이고, 이 문서는 학습 과정을 복기하기 위한 문서입니다.

---

## 학습 타임라인

```text
2026-07-26
- Redis Cache 적용
- Docker Compose 구성
- GitHub Actions CI 연결

2026-07-30
- AWS EC2 Ubuntu 서버 배포
- Spring Boot App + MySQL + Redis Docker Compose 배포
- Actuator Health Check 확인

2026-08
- Spring Security 적용
- BCrypt 비밀번호 암호화
- 로그인 API 구현
- JWT Access Token 발급
- JWT 인증 필터 구현
- SecurityContext 기반 현재 사용자 식별
- 주문 소유권 기반 인가 구현
- Role 기반 USER / ADMIN 권한 분리
- 관리자 API 구현
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
→ Nginx Reverse Proxy
→ GHCR 기반 Docker 이미지 배포
→ Spring Security
→ JWT 인증/인가
→ Role 기반 관리자 API
```

이 프로젝트를 통해 단순 CRUD에서 시작해 인증, 인가, 배포, 캐시, 테스트, CI/CD까지 백엔드 애플리케이션의 기본 흐름을 직접 경험했습니다.

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
- 로그인 검증
- 권한 검증
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

배운 점:

- Controller는 HTTP 계층만 담당하는 것이 좋다.
- Service는 비즈니스 흐름과 트랜잭션을 담당한다.
- Repository는 저장소 접근을 담당한다.
- Service가 Repository 인터페이스에 의존하면 구현체 교체가 쉬워진다.

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

MemberCreateRequest
MemberUpdateRequest
MemberPatchRequest
MemberSearchRequest
MemberResponse

OrderCreateRequest
OrderSearchRequest
OrderResponse

LoginRequest
LoginResponse
```

DTO를 분리하면 다음 장점이 있습니다.

- API 스펙과 내부 Entity 구조를 분리할 수 있다.
- 요청 검증을 DTO에 적용할 수 있다.
- 응답에서 필요한 필드만 노출할 수 있다.
- Entity 변경이 곧바로 API 변경으로 이어지는 것을 막을 수 있다.
- 비밀번호 같은 민감한 필드를 응답에서 제외할 수 있다.

배운 점:

- Entity를 그대로 API에 노출하면 내부 구조가 외부 API가 되어버린다.
- Request DTO와 Response DTO를 분리하면 역할이 명확해진다.
- DTO는 API 경계에서 매우 중요하다.

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
- 요청 DTO는 검증 책임을 가지기 좋은 위치다.

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
ForbiddenException
LoginFailedException
MethodArgumentNotValidException
IllegalArgumentException
IllegalStateException
DataIntegrityViolationException
```

상황별 응답:

```text
없는 리소스 조회        → 404 Not Found
검증 실패              → 400 Bad Request
중복 이메일            → 400 Bad Request
잘못된 상태 변경       → 400 Bad Request
참조 중인 데이터 삭제  → 409 Conflict
로그인 실패            → 401 Unauthorized
권한 없는 접근         → 403 Forbidden
```

배운 점:

- 예외 처리를 한곳에 모으면 Controller가 깔끔해진다.
- 비즈니스 예외를 직접 정의하면 실패 원인을 명확히 표현할 수 있다.
- 인증 실패와 인가 실패는 구분해야 한다.
- 인증 실패는 401, 권한 부족은 403이 적절하다.

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
- MemoryRepository로 먼저 구현한 뒤 JPA로 전환하면 Repository 추상화의 의미를 체감할 수 있다.

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
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
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
- Role 같은 권한 값도 enum으로 표현하면 의미가 명확해진다.

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

Order 검색:

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
- Service의 트랜잭션 범위가 Entity 상태 변경에 직접적인 영향을 준다.

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
@Transactional(readOnly = true)
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
GET /orders/my
GET /admin/members
GET /admin/orders
GET /admin/orders/search
```

요청 예시:

```http
GET /books?page=0&size=5
GET /members?page=0&size=5&sort=age,desc
GET /admin/orders/search?status=CANCELED&page=0&size=5
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
role
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
- 로컬뿐 아니라 인터넷에서 접근 가능한 서버에 프로젝트를 배포했다.

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

EC2에 배포한 Spring Boot 애플리케이션은 기본적으로 8080 포트에서 실행됩니다.

처음에는 다음 주소로 직접 접근했습니다.

```text
http://<EC2_PUBLIC_IP>:8080
```

하지만 운영 환경에서는 애플리케이션 포트를 외부에 직접 노출하기보다 Nginx가 80번 포트에서 요청을 받고 내부의 Spring Boot 8080 포트로 전달하는 구조를 많이 사용합니다.

적용 후 구조:

```text
Client
  ↓
Nginx :80
  ↓
Spring Boot :8080
```

Nginx 설정 중 오타로 문제가 발생했습니다.

잘못된 설정:

```text
server_name_
```

올바른 설정:

```text
server_name _;
```

`server_name`과 `_` 사이에는 공백이 있어야 하고, 마지막에는 세미콜론이 필요합니다.

배운 점:

- Nginx는 Reverse Proxy로 사용할 수 있다.
- 외부에는 80 포트만 열고, Spring Boot 8080 포트는 내부에서만 사용하게 만들 수 있다.
- `sudo nginx -t`로 설정 문법을 검사한 뒤 재시작해야 한다.
- Nginx 설정은 작은 오타 하나로도 실행 실패할 수 있다.

---

# 30. GitHub Actions EC2 자동 배포

기존에는 EC2에 직접 SSH 접속해서 수동으로 배포했습니다.

```bash
git pull
docker compose up -d --build
```

이를 GitHub Actions workflow에 추가해 main 브랜치에 push하면 자동 배포되도록 만들었습니다.

발생한 문제:

```text
ssh.ParsePrivateKey: ssh: no key found
dial tcp ***:22: i/o timeout
```

원인은 두 가지였습니다.

```text
1. GitHub Secret에 등록한 EC2_SSH_KEY 값이 올바른 private key 형식이 아니었다.
2. EC2 보안 그룹에서 SSH 22번 포트가 내 IP만 허용되어 있어 GitHub Actions 서버가 접속할 수 없었다.
```

해결:

- `mvc-crud-key.pem` 내용을 줄바꿈 포함해서 GitHub Secret에 다시 등록했다.
- EC2 보안 그룹에서 SSH 22번을 GitHub Actions가 접근할 수 있도록 수정했다.
- workflow에서는 `secrets.EC2_HOST`, `secrets.EC2_USER`, `secrets.EC2_SSH_KEY`를 사용했다.

배운 점:

- GitHub Actions에서 EC2로 배포하려면 SSH key를 GitHub Secrets에 안전하게 저장해야 한다.
- Secret 이름은 workflow에서 사용하는 이름과 정확히 일치해야 한다.
- `secret`이 아니라 `secrets`를 사용해야 한다.
- EC2 보안 그룹이 막혀 있으면 workflow에서 SSH 접속이 timeout 된다.
- CI/CD가 구성되면 push 이후 테스트와 배포가 자동화된다.

---

# 31. GHCR 기반 Docker 이미지 배포

기존 배포 방식은 EC2 서버에서 직접 Docker 이미지를 빌드하는 구조였습니다.

```bash
git pull origin main
docker compose up -d --build
```

이 방식은 단순하지만 EC2 서버에 빌드 부담이 생깁니다. 특히 작은 서버에서는 Docker build가 메모리와 디스크를 많이 사용할 수 있습니다.

이를 개선하기 위해 GitHub Actions에서 Docker 이미지를 빌드하고 GitHub Container Registry에 push하도록 변경했습니다.

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
unable to prepare context: path ".true" not found
```

원인:

`push: true` 또는 `context: .` 설정이 잘못 작성되어 build context가 `.true`로 해석되었습니다.

올바른 설정:

```yaml
context: .
push: true
```

배운 점:

- GitHub Actions에서 Docker 이미지를 빌드할 수 있다.
- GHCR에 이미지를 push하려면 `packages: write` 권한이 필요하다.
- EC2에서 직접 빌드하지 않고 이미지를 pull하는 방식이 더 안정적이다.
- YAML 오타 하나로 workflow 해석이 완전히 달라질 수 있다.

---

# 32. Spring Security 적용

Spring Security를 적용해 API 접근 제어를 시작했습니다.

처음에는 모든 API를 막는 것이 아니라, 로그인/회원가입/Swagger/Health Check 등은 열어두고 보호가 필요한 API부터 잠그는 방식으로 적용했습니다.

기본 설정 예시:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/actuator/health/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/h2-console/**",
        "/auth/login",
        "/members"
    ).permitAll()
    .requestMatchers(HttpMethod.POST, "/orders").authenticated()
    .anyRequest().permitAll()
)
```

배운 점:

- SecurityConfig는 어떤 API를 열고 잠글지를 정의한다.
- `permitAll()`은 인증 없이 접근 가능하다는 의미다.
- `authenticated()`는 로그인한 사용자만 접근 가능하다는 의미다.
- 보호할 API를 점진적으로 늘리는 방식이 학습에 적합했다.

---

# 33. BCrypt 비밀번호 암호화

처음에는 비밀번호를 평문으로 저장할 위험이 있었습니다.

이를 방지하기 위해 `PasswordEncoder`를 사용해 비밀번호를 BCrypt로 암호화했습니다.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

회원 생성 시:

```java
String encodedPassword = passwordEncoder.encode(password);
```

로그인 검증 시:

```java
passwordEncoder.matches(rawPassword, encodedPassword);
```

배운 점:

- 비밀번호는 절대 평문으로 저장하면 안 된다.
- BCrypt는 매번 다른 salt를 사용하기 때문에 같은 비밀번호도 다른 해시값이 나온다.
- 따라서 비밀번호 비교는 문자열 비교가 아니라 `matches()`로 해야 한다.

---

# 34. 로그인 API

로그인은 단순 조회가 아니라 인증 처리 요청입니다.

처리 흐름:

```text
email/password 제출
→ 서버가 회원 조회
→ 비밀번호 검증
→ 인증 성공 여부 판단
→ JWT Access Token 발급
```

GET 요청은 URL에 데이터가 노출될 수 있습니다.

```http
GET /auth/login?email=kim@test.com&password=password1234
```

이 방식은 비밀번호가 브라우저 히스토리, 서버 로그, 프록시 로그 등에 남을 수 있기 때문에 부적절합니다.

따라서 로그인은 요청 body에 자격 증명을 담을 수 있는 POST를 사용합니다.

```http
POST /auth/login
```

```json
{
  "email": "kim@test.com",
  "password": "password1234"
}
```

POST는 반드시 DB에 데이터를 저장한다는 뜻이 아니라, 서버에 어떤 처리를 요청한다는 의미로 볼 수 있습니다.

배운 점:

- 로그인은 단순 조회가 아니라 인증 처리 요청이므로 POST를 사용한다.
- 비밀번호는 URL에 노출되면 안 되므로 GET 로그인은 부적절하다.
- 로그인 실패는 401 Unauthorized로 응답하는 것이 적절하다.

---

# 35. JWT란?

JWT는 로그인 성공 후 서버가 발급하는 서명된 토큰입니다.

클라이언트는 이후 요청마다 다음 헤더에 토큰을 담아 보냅니다.

```http
Authorization: Bearer <accessToken>
```

서버는 JWT를 검증해서 요청을 보낸 사용자가 누구인지 식별합니다.

JWT는 암호화가 아니라 서명 기반입니다.

따라서 Payload는 누구나 디코딩할 수 있으므로 비밀번호나 민감정보를 넣으면 안 됩니다.

현재 프로젝트에서는 JWT에 다음 정보를 담았습니다.

```text
subject: memberId
email: 회원 이메일
role: USER 또는 ADMIN
issuedAt: 발급 시간
expiration: 만료 시간
```

배운 점:

- JWT는 서버가 발급한 서명된 인증 정보다.
- JWT를 요청마다 보내면 서버는 사용자를 식별할 수 있다.
- JWT Payload에는 민감정보를 넣으면 안 된다.
- JWT는 인증 정보를 담을 수 있지만, 토큰 탈취 위험도 고려해야 한다.

---

# 36. JwtProvider의 역할

`JwtProvider`는 JWT를 생성하고 검증하는 책임을 가집니다.

주요 역할:

```text
1. 로그인 성공 시 Access Token 생성
2. 토큰 유효성 검증
3. 토큰에서 memberId 추출
4. 토큰에서 email 추출
5. 토큰에서 role 추출
```

토큰 생성 흐름:

```java
Jwts.builder()
    .subject(String.valueOf(member.getId()))
    .claim("email", member.getEmail())
    .claim("role", member.getRole().name())
    .issuedAt(now)
    .expiration(expiration)
    .signWith(secretKey)
    .compact();
```

배운 점:

- JWT 생성/검증 책임은 Controller나 Service에 흩어두지 않고 별도 클래스로 분리하는 것이 좋다.
- role 같은 권한 정보도 JWT claim에 포함할 수 있다.
- 단, JWT payload는 디코딩 가능하므로 민감정보는 넣지 않아야 한다.

---

# 37. JwtAuthenticationFilter의 역할

`JwtAuthenticationFilter`는 요청마다 Authorization 헤더를 확인합니다.

처리 흐름:

```text
1. Authorization 헤더 확인
2. Bearer 토큰인지 확인
3. JWT 유효성 검증
4. 토큰에서 memberId/email/role 추출
5. CustomUserPrincipal 생성
6. Authentication 객체 생성
7. SecurityContextHolder에 저장
```

예시:

```java
UsernamePasswordAuthenticationToken authentication =
    new UsernamePasswordAuthenticationToken(
        principal,
        null,
        List.of(new SimpleGrantedAuthority("ROLE_" + role))
    );

SecurityContextHolder.getContext().setAuthentication(authentication);
```

즉, 필터는 토큰을 검사해서 Spring Security가 이해할 수 있는 인증 정보로 바꿔주는 역할을 합니다.

배운 점:

- JWT 필터는 Authorization 헤더의 토큰을 검증한다.
- 검증된 사용자 정보는 SecurityContext에 저장된다.
- Spring Security는 Authentication 객체를 기준으로 현재 사용자를 판단한다.
- 권한은 `GrantedAuthority` 형태로 넣어야 한다.

---

# 38. SecurityContext란?

`SecurityContextHolder`는 현재 요청의 인증 정보를 저장하는 공간입니다.

JWT 필터가 인증에 성공하면 다음과 같이 인증 객체를 저장합니다.

```java
SecurityContextHolder.getContext().setAuthentication(authentication);
```

이후 Controller에서는 현재 로그인 사용자를 꺼낼 수 있습니다.

```java
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

CustomUserPrincipal principal =
    (CustomUserPrincipal) authentication.getPrincipal();
```

배운 점:

- SecurityContext는 현재 요청의 인증 상태를 담는다.
- JWT 인증 필터가 SecurityContext에 인증 객체를 넣어주면 Controller에서 현재 사용자를 알 수 있다.
- 로그인 사용자의 id를 요청 body에서 받는 것보다 SecurityContext에서 가져오는 것이 안전하다.

---

# 39. `/auth/me` API

JWT 인증이 정상적으로 동작하는지 확인하기 위해 `/auth/me` API를 구현했습니다.

```http
GET /auth/me
Authorization: Bearer <accessToken>
```

응답 예시:

```json
{
  "data": {
    "memberId": 1,
    "email": "kim@test.com",
    "role": "USER"
  }
}
```

배운 점:

- `/auth/me`는 현재 토큰이 누구의 것인지 확인하기 좋은 API다.
- JWT 필터가 SecurityContext에 인증 정보를 제대로 넣는지 검증할 수 있다.
- 인증 테스트 초반에 매우 유용했다.

---

# 40. 주문 생성과 인증 사용자

기존에는 주문 생성 요청 body에 `memberId`를 포함했습니다.

```json
{
  "memberId": 1,
  "bookId": 1,
  "quantity": 2
}
```

하지만 이 방식은 클라이언트가 다른 회원의 `memberId`를 임의로 넣을 수 있다는 문제가 있습니다.

따라서 주문 생성 요청에서는 `memberId`를 제거하고, JWT 인증 필터가 SecurityContext에 저장한 현재 로그인 사용자 정보를 사용하도록 변경했습니다.

```json
{
  "bookId": 1,
  "quantity": 2
}
```

Controller는 `SecurityContextHolder`에서 `CustomUserPrincipal`을 꺼내 `memberId`를 얻고, 그 값을 `OrderService`에 전달합니다.

배운 점:

- 인증된 사용자 정보는 클라이언트 요청 body에서 받으면 안 된다.
- 주문 생성자는 요청 body가 아니라 JWT 인증 정보로 결정되어야 한다.
- 이렇게 해야 다른 사람 이름으로 주문하는 문제를 막을 수 있다.

---

# 41. 인증 사용자 기반 주문 API

현재 로그인한 사용자의 주문만 조회하는 API를 추가했습니다.

```http
GET /orders/my
Authorization: Bearer <accessToken>
```

또한 주문 단건 조회도 본인의 주문만 가능하도록 변경했습니다.

```http
GET /orders/{id}
Authorization: Bearer <accessToken>
```

배운 점:

- `/orders/my`는 현재 로그인 사용자의 리소스만 조회하는 API다.
- 단순히 로그인 여부만 확인하는 것으로는 부족하다.
- 리소스가 누구의 것인지 확인하는 인가 로직이 필요하다.

---

# 42. 주문 취소 소유권 검증

기존에는 로그인한 사용자라면 `orderId`만 알고 있을 때 다른 사용자의 주문을 취소할 위험이 있었습니다.

이를 방지하기 위해 주문 취소 시 JWT 인증 정보에서 추출한 `memberId`와 주문의 `memberId`를 비교했습니다.

```java
if (!order.getMemberId().equals(memberId)) {
    throw new ForbiddenException("본인의 주문만 취소할 수 있습니다.");
}
```

조회도 같은 방식으로 소유권을 검증했습니다.

```java
if (!order.getMemberId().equals(memberId)) {
    throw new ForbiddenException("본인의 주문만 조회할 수 있습니다.");
}
```

배운 점:

- 인증과 인가는 다르다.
- 로그인한 사용자라도 모든 리소스에 접근할 수 있는 것은 아니다.
- 본인의 주문인지 확인하는 소유권 기반 인가가 필요하다.
- 다른 사용자의 리소스 접근은 403 Forbidden이 적절하다.

---

# 43. Role 기반 인가

기존에는 JWT를 통해 사용자의 `memberId`만 식별했습니다.

이번 단계에서는 `Member`에 `Role`을 추가하고 JWT claim에 role을 포함했습니다.

```java
public enum Role {
    USER,
    ADMIN
}
```

JWT 인증 필터는 토큰에서 role을 꺼내 `ROLE_USER`, `ROLE_ADMIN` 형태의 권한으로 변환합니다.

```java
List.of(new SimpleGrantedAuthority("ROLE_" + role))
```

이를 통해 SecurityConfig에서 다음과 같은 권한 규칙을 적용할 수 있습니다.

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

테스트에서는 다음 세 가지를 검증했습니다.

```text
토큰 없음 → 401
USER 토큰 → 403
ADMIN 토큰 → 200
```

배운 점:

- Role은 사용자 유형에 따른 권한 분리에 사용된다.
- Spring Security의 `hasRole("ADMIN")`은 내부적으로 `ROLE_ADMIN` 권한을 확인한다.
- JWT에 role을 포함하고 필터에서 GrantedAuthority로 변환해야 SecurityConfig에서 사용할 수 있다.
- 인증은 “너 누구야?”이고, 인가는 “너 이거 해도 돼?”이다.

---

# 44. 관리자 API 구현

관리자 API는 `/admin/**` 경로로 분리했습니다.

구현한 API:

```text
GET   /admin/members
GET   /admin/orders
GET   /admin/orders/search
PATCH /admin/orders/{id}/cancel
```

권한 규칙:

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

관리자 회원 조회:

```http
GET /admin/members?page=0&size=10
Authorization: Bearer <adminAccessToken>
```

관리자 주문 전체 조회:

```http
GET /admin/orders?page=0&size=10
Authorization: Bearer <adminAccessToken>
```

관리자 주문 검색:

```http
GET /admin/orders/search?memberId=1&status=ORDERED&page=0&size=10
Authorization: Bearer <adminAccessToken>
```

관리자 주문 강제 취소:

```http
PATCH /admin/orders/1/cancel
Authorization: Bearer <adminAccessToken>
```

배운 점:

- 관리자 기능은 일반 사용자 API와 경로를 분리하는 것이 좋다.
- `/orders/{id}/cancel`은 본인 주문 취소 API다.
- `/admin/orders/{id}/cancel`은 관리자 강제 취소 API다.
- 같은 “취소”라도 사용자 권한에 따라 API를 분리하면 책임이 명확해진다.

---

# 45. 관리자 계정 생성 방식

일반 회원가입 API에서 role을 받지 않도록 했습니다.

이유:

```text
클라이언트가 회원가입 요청 body에 role=ADMIN을 넣으면 관리자 계정을 만들 수 있는 위험이 생긴다.
```

따라서 일반 회원 생성은 항상 `USER`로 만들고, 관리자 계정은 내부 메서드로만 생성하도록 했습니다.

```java
@Transactional
public Member createMember(String name, String email, String password, int age) {
    return createMemberWithRole(name, email, password, age, Role.USER);
}

@Transactional
public Member createAdminMember(String name, String email, String password, int age) {
    return createMemberWithRole(name, email, password, age, Role.ADMIN);
}
```

배운 점:

- 권한 상승이 가능한 값을 클라이언트 요청으로 받으면 위험하다.
- 일반 회원가입은 무조건 USER로 생성하는 것이 안전하다.
- ADMIN 생성은 테스트, 초기 데이터, 운영자용 내부 절차로 제한해야 한다.

---

# 46. 일반 사용자 API와 관리자 API 분리

일반 사용자 주문 취소 API:

```http
PATCH /orders/{id}/cancel
```

특징:

```text
주문 소유자만 취소 가능
```

관리자 주문 강제 취소 API:

```http
PATCH /admin/orders/{id}/cancel
```

특징:

```text
ADMIN은 모든 주문 취소 가능
```

처음 테스트에서 관리자 토큰으로 `/orders/{id}/cancel`을 호출했을 때 403이 발생했습니다.

원인:

```text
/orders/{id}/cancel은 관리자 API가 아니라 일반 사용자용 본인 주문 취소 API였기 때문
```

해결:

관리자용 강제 취소 API를 별도로 만들었습니다.

배운 점:

- API 경로는 권한과 책임을 드러내야 한다.
- 같은 기능처럼 보여도 사용자 관점과 관리자 관점은 다르다.
- 테스트 실패가 오히려 설계를 명확히 해주는 계기가 되었다.

---

# 47. Security Integration Test

Spring Security와 JWT 인증/인가 흐름은 단위 테스트보다 통합 테스트가 적합했습니다.

검증한 내용:

```text
로그인 성공 시 JWT 발급
잘못된 로그인 요청 시 401 Unauthorized
JWT로 현재 사용자 조회
토큰 없이 주문 생성 시 401 Unauthorized
토큰이 있으면 주문 생성 성공
주문 생성 시 요청 body의 memberId가 아니라 JWT의 memberId 사용
현재 로그인 사용자의 주문 목록 조회
다른 사용자의 주문 조회 시 403 Forbidden
다른 사용자의 주문 취소 시 403 Forbidden
USER가 관리자 API 접근 시 403 Forbidden
ADMIN이 관리자 API 접근 시 200 OK
ADMIN의 전체 회원 조회
ADMIN의 전체 주문 조회
ADMIN의 주문 검색
ADMIN의 주문 강제 취소
```

테스트에서 확인한 권한 흐름:

```text
토큰 없음 → 401 Unauthorized
USER 토큰 → 403 Forbidden
ADMIN 토큰 → 200 OK
```

배운 점:

- 보안 설정은 Controller 단위 테스트보다 통합 테스트로 검증하는 것이 좋다.
- MockMvc로 로그인 → 토큰 추출 → 보호 API 호출 흐름을 검증할 수 있다.
- 테스트 코드의 경로 오타도 보안 규칙 때문에 통과할 수 있으므로 실제 API 경로를 주의해야 한다.
- 테스트가 성공해도 테스트 내용이 올바른지 검토해야 한다.

---

# 48. 현재까지의 인증/인가 구조 정리

현재 프로젝트의 인증/인가 구조는 다음과 같습니다.

```text
1. 회원가입
   POST /members
   → 비밀번호 BCrypt 암호화
   → 기본 Role.USER 저장

2. 로그인
   POST /auth/login
   → email/password 검증
   → JWT Access Token 발급

3. 인증 요청
   Authorization: Bearer <accessToken>
   → JwtAuthenticationFilter에서 토큰 검증
   → SecurityContext에 CustomUserPrincipal 저장

4. 일반 사용자 API
   POST /orders
   GET /orders/my
   GET /orders/{id}
   PATCH /orders/{id}/cancel
   → JWT 기반 사용자 식별
   → 주문 소유권 검증

5. 관리자 API
   GET /admin/members
   GET /admin/orders
   GET /admin/orders/search
   PATCH /admin/orders/{id}/cancel
   → Role.ADMIN 권한 필요
```

---

# 49. 현재까지의 한 줄 요약

```text
Spring Boot 기반 CRUD API를 구현하고, JPA/MySQL/Redis/Docker Compose/GitHub Actions/Actuator/Nginx/GHCR/EC2 배포를 적용한 뒤, Spring Security와 JWT를 통해 인증/인가 및 Role 기반 관리자 API까지 구현했다.
```

---

# 50. 다음 학습 후보

우선순위가 높은 순서:

```text
1. Swagger JWT Authorize 설정
2. 배포 환경용 관리자 계정 생성 방식 정리
3. Admin 통계 API
4. Refresh Token / Access Token 재발급 기능
5. 로그아웃 API
6. 테스트 코드 Fixture 분리
7. Redis TTL 전략 정리
8. 캐시 대상 확장
9. 운영 환경용 DB 설정 고도화
10. HTTPS 적용
11. 주문 구조를 OrderItem 기반으로 확장
12. API 에러 응답 구조 추가 개선
13. Kubernetes 맛보기
```

현재 단계에서는 기능을 계속 무작정 추가하기보다, 인증/인가 구조를 문서화하고 Swagger에서 테스트하기 쉽게 만든 뒤, 관리자 계정 생성 방식과 통계 API를 추가하는 것이 좋다.