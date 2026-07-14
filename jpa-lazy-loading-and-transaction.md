# JPA Lazy Loading과 Transaction 학습 정리

## 1. 이번에 발생한 예외

`open-in-view: flase`를 적용한 뒤 `GET /orders`를 호출했을 때 아래 예외가 발생했다. 

```text
org.hibernate.LazyInitializationException:
Could not initialize proxy [Member#1] - no session
```

발생 흐름은 대략 다음과 같다.
```text
OrderController.findOrders()
-> orderService.findOrders(pageable)
-> Page<Order>.map(OrderResponse::new)
-> OrderResponse 생성자
-> order.getMember().getName()
-> LazyInitializationException 발생
```

즉, `OrderResponse`를 만드는 과정에서 `Order`가 가지고 있는 `Member`프록시 객체의 `name`필드에 접근하려 했고, 이때 이미 영속성 컨텍스트가 닫혀 있어서 예외가 발생했다.

---

# 2. 트랜잭션이란?
트랜잭션은 DB 작업을 하나의 작업 단위로 묶는 것이다.

예를 들어 주문 취소 기능은 다음 작업을 하나로 묶어야 한다.

```text
1. 주문 조회
2. 주문 상태 변경
3. DB 반영
```

Spring에서는 보통 Service 계층에 `@Transactional`을 붙여 트랜잭션을 관리한다.

```java
@Transactional
public Order cancelOrder(Long id) {
    Order order = findOrder(id);
    order.cancel();
    return order;
}
```
이 메서드가 실행되는 동안의 흐름은 다음과 같다.

```text
트랜잭션 시작
-> 영속성 컨텍스트 열림
-> DB에서 Order 조회
-> Order 상태 변경
-> 트랜잭션 커밋
-> 변경 감지로 update SQL 실행
-> 영속성 컨텍스트 닫힘
```

---
# 3. 영속성 컨텍스트 / 세션이란?
JPA에서 엔티티를 관리하는 공간을 **영속성 컨텍스트**라고 한다.

Hibernate에서는 이것을 **Session**이라고도 부른다.

지금 단계에서는 이렇게 이해하면 된다.

```text
영속성 컨텍스트 / 세션
= JPA가 Entity를 기억하고 관리하는 작업 공간
```
영속성 컨텍스트 안에 있는 엔티티는 JPA가 관리한다.

이때 가능한 기능은 다음과 같다.

```text
1차 캐시
변경 감지
동일성 보장 
LAZY 로딩
```

반대로 영속성 컨텍스트가 닫힌 뒤에는 JPA가 더 이상 해당 엔티티를 관리하지 못한다.

이 상태에서 LAZY 연관 객체를 조회하려고 하면 문제가 발생한다.

---
# 4. LAZY로딩이란? 
현재 `Order`는 `Member`, `Book`과 다대일 관계를 가진다.

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "member_id")
private Member member;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "book_id")
private Book book;
```
`LAZY`는 지연 로딩을 의미한다.

뜻은 다음과 같다.
```text
Order를 조회할 때 Member와 Book을 당장 같이 조회하지 않는다.
일단 가짜 객체인 프록시를 넣어두고, 실제로 필요할 때 그때 DB에서 조회한다. 
```

예를 들어 `GET /orders`를 호출하면 처음 SQL은 `orders`테이블만 조회된다. 

```sql
select
    o.id,
    o.member_id,
    o.book_id,
    o.quantity,
    o.order_price,
    o.status
from orders o 
limit ?, ? 
```

이 시점에는 `member`테이블과 `book`테이블을 join하지 않는다.

대신 `Order`안의 `member`,`book`필드에는 실제 객체가 아니라 프록시가 들어간다.

```text
Order
 ├── member: Member 프록시
 └── book: Book 프록시
```

---
# 5. 프록시란?
프록시는 실제 객체처럼 보이는 가짜 객체다.

JPA는 LAZY 연관관계를 사용할 때 실제 객체 대신 프록시 객체를 넣어둔다.

예를 들어 `Order`가 `Member#1`을 참조하고 있다면, 처음에는 실제 `Member`를 조회하지 않고 다음과 같은 가짜 객체를 둔다.

```text
Member#1 프록시
```
프록시는 ID 정도는 알고 있다.

(ID가 FK이므로...)

그래서 아래 코드는 추가 조회 없이 동작할 수 있다. 

```java
order.getMember().getId();
```
하지만 아래 코드는 다르다.
```java
order.getMember().getName();
```

`name`은 `orders`테이블에 없고, `member`테이블에 있다.

따라서 이 순간 JPA는 실제 `Member` 데이터를 조회해야 한다.

이때 영속성 컨텍스트가 열려 있으면 DB 조회가 가능하다. 

하지만 영속성 컨텍스트가 닫혀 있으면 조회할 수 없고 예외가 발생한다.

---
# 6. 왜 memberId는 됐고 memberName은 터졌나?
기존 `OrderResponse`는 다음 정도만 응답했다.

```java
this.memberId = order.getMemberId();
this.bookId = order.getBookId();
```

그리고 `Order`의 `getMemberId()`는 다음처럼 구현되어 있었다. 

```java
public Long getMemberId() {
    return member.getId();
}
```

`member.getId()`는 프록시가 이미 알고 있는 값이라서 실제 `Member`를 DB에서 조회하지 않아도 가능한 경우가 많다.

그래서 문제가 발생하지 않았다. 

하지만 `memberName`을 추가하면서 다음 코드가 들어갔다.

```text
member.getName()
-> member테이블 조회 필요
-> 그런데 세션이 이미 닫힘
-> LazyInitializationException 발생
```

---
# 7. 현재 코드 흐름에서 왜 예외가 발생했나? 
현재 Controller 구조는 다음과 같았다.
```java
@GetMapping
public ApiResponse<PageResponse<OrderResponse>> findOrders(Pageable pageabl){
    Page<OrderResponse> orders = orderService.findOrders(pageable)
        .map(OrderResponse::new);
    
    return ApiResponse.of(PageResponse.from(orders));
}
```

이 흐름을 순서대로 보면 다음과 같다. 

```text
1. Controller가 요청을 받음
2. orderService.findOrders(pageable) 호출
3. Service에서 트랜잭션 시작
4. DB에서 Order 목록 조회
5. Service메서드 종료
6. 트랜잭션 종료
7. 영속성 컨텍스트 닫힘
8. Controller로 Page<Order> 변환
9. Controller에서 .map(OrderResponse::new) 실행
10. OrderResponse 생성자에서 order.getMember().getName() 호출 
11. LAZY 로딩 시도
12. 영속성 컨텍스트가 이미 닫혀 있음
13. LazyInitializationException 발생
```

핵심은 이것이다. 
```text
Entity를 조회한 뒤,
트랜잭션 밖인 Controller에서 
LAZY 연관 객체를 건드려서 예외가 터졌다.
```

---
# 8. open-in-view란?
Spring Boot는 기본적으로 `open-in-view`가 켜져 있다.

```yaml
spring:
  jpa:
    open-in-view: true
```
이 설정이 켜져 있으면 Service 트랜잭션이 끝난 뒤에도 Controller 응답이 끝난 때 까지 영속성 컨텍스트를 열어둔다.

그래서 Controller에서 다음 코드를 실행해도 예외가 안 날 수 있다.
```java
new OrderResponse(order)
```
왜냐하면 아직 세션이 열려 있기 때문이다.

하지만 단점이 있다. 

```text
Controller에서 DB 쿼리가 몰래 나갈 수 있다.
성능 예측이 어려워진다.
N+1 문제가 숨어버릴 수 있다.
Service 계층의 책임이 흐려진다.
```
그래서 실무에서는 보통 다음처럼 끄는 방향을 많이 사용한다.
```yaml
spring:
  jpa:
    open-in-view: false
```
그러면 Service 트랜잭션이 끝난 뒤에는 영속성 컨텍스트도 닫힌다.

따라서 필요한 데이터는 Service 트랜잭션 안에서 명확하게 조회하고 DTO로 변환해야 한다.

---
# 9. LazyInitializationException 해결 방법
## 방법 1. DTO 변환을 Service 안에서 수행
가장 먼저 적용하기 좋은 방법이다.

Controller에서 Entity를 DTO로 바꾸지 않고, Service 트랜잭션 안에서 DTO로 변환한다.

```java
@Transactional(readOnly = true)
public Page<OrderResponse> findOrderResponse(Pageable pageable){
    return orderRepository.findAll(pageable)
        .map(OrderResponse::new);
}
```
흐름은 다음과 같다.
```text
트랜잭션 시작
-> Order 조회
-> OrderResponse 생성
-> order.getMember().getName() 접근
-> 세션이 열려 있으므로 LAZY 로딩 가능
-> DTO 완성
-> 트랜잭션 종료
-> Controller로 DTO 반환
```
장점:
```text
이해하기 쉽다.
현재 프로젝트에 바로 적용하기 좋다.
open-in-view=false 환경에서도 동작한다.
```

## 방법 2. Fetch Join 사용
처음부터 `Order`, `Member`, `Book`을 함께 조회하는 방법이다
```java
@Query("""
    select o from Order o
    join fetch o.member
    join fetch o.book
    """)
List<Order> findAllWithMemberAndBook();
```
그러면 `Order`를 조회할 때 `Member`, `Book`도 실제 객체로 함께 조회된다.

장점:
```text
LAZY 로딩 추가 쿼리를 줄일 수 있다.
N+1 문제 해결에 자주 사용된다.
```
주의:
```text
페이징과 fetch join을 같이 사용할 때는 조심해야 한다.
특히 컬렉션 fetch join + 페이징은 문제가 크다.
```
현재 프로젝트의 `Order -> Member`, `Order -> Book`은 `ManyToOne`이라 비교적 안전하지만, fetch join은 다음 단계에서 따로 학습하는 것이 좋다.

## 방법 3. DTO 직접 조회
처음부터 Entity가 아니라 DTO를 바로 조회하는 방법이다.
```java
select new ...OrderResponse(...)
from Order o
join o.member m
join o.book b
```
장점:
```text
필요한 데이터만 조회할 수 있다.
조회 성능 최적화에 유리하다.
```
단점:
```text
초반에는 코드가 복잡하게 느껴질 수 있다.
Entity 중심 CRUD 학습 이후에 적용하는 것이 좋다.
```

---
# 10. 지금 프로젝트에서 우선 적용할 해결책
현재 프로젝트에서는 먼저 다음 방식을 적용한다. 
```text
DTO 변환을 Controller에서 하지 않고 Service 안에서 수행한다.
```
현재 문제 코드:
```java
@GetMapping
public ApiResponse<PageResponse<OrderResponse>> findOrders(Pageable pageable){
    Page<OrderResponse> orders = orderService.findOrders(pageable)
        .map(OrderResponse::new);

    return ApiResponse.of(PageResponse.from(orders));
}
```
개선 방향:
```java
@GetMapping
public ApiResponse<PageResponse<OrderResponse>> findOrders(Pageable pageable){
    Page<OrderResponse> orders = orderService.findOrderResponse(pageable);

    return ApiResponse.of(PageResponse.from(orders));
}
```

Service:
```java
@Transactional(readOnly = true)
public Page<OrderResponse> findOrderResponse(Pageable pageable){
    return orderReposiotry.findAll(pageable)
        .map(OrderResponse::new);
}
```
이렇게 하면 DTO 생성이 트랜잭션 안에서 일어나므로 LAZY 로딩이 가능해진다.

# 11. 핵심 요약
```text
트랜잭션 = DB 작업을 하나로 묶는 단위

영속성 컨텍스트 = JPA가 Entity를 관리하는 공간

LAZY 로딩 = 연관 객체를 실제로 사용할 때 조회하는 방식

프록시 = 실제 객체 대신 들어있는 가짜 객체

open-in-view=false = Controller 응답 시점에는 세션이 닫혀 있음

LazyInitializationException = 세션이 닫힌 뒤 LAZY 프록시를 초기화하려 해서 발생하는 예외 
```

이번 예외의 핵심:
```text
Controller에서 OrderResponse를 만들면서 
order.getMember().getName()을 호출했다.

하지만 Service 트랜잭션은 이미 끝났고,
영속성 컨텍스트도 닫혀 있었다. 

그래서 Member 프록시를 초기화할 수 없어 예외가 발생했다.
```

# 12. 지금 기억해야할 한 문장
```text
open-in-view=false 환경에서는
LAZY 연관 객체를 사용하는 DTO 변환을 
Controller가 아니라 Service 트랜잭션 안에서 처리해야 한다.
```