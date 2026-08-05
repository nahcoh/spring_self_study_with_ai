# Spring MVC CRUD Practice

![Java CI](https://github.com/nahcoh/spring_self_study_with_ai/actions/workflows/ci.yml/badge.svg)

Spring Boot 기반의 Book / Member / Order CRUD REST API 프로젝트입니다.

단순 CRUD API에서 시작해 JPA, MySQL, Redis Cache, Docker Compose, Swagger, GitHub Actions CI/CD, Actuator Health Check, AWS EC2 배포, Spring Security + JWT 인증/인가까지 확장하며 백엔드 애플리케이션의 기본 구조와 인프라 연결 흐름을 학습했습니다.

---

## 프로젝트 요약

이 프로젝트는 Spring 백엔드 애플리케이션의 기본 흐름을 직접 구현하며 체화하기 위한 학습 프로젝트입니다.

처음에는 `MemoryRepository` 기반으로 시작했고, 이후 Spring Data JPA와 실제 DB 기반 구조로 전환했습니다.

현재는 Book, Member, Order 도메인에 대해 CRUD, 검색, 페이징, 정렬, 예외 처리, Validation, 테스트, Redis 캐시, Docker Compose 실행 환경, CI/CD, EC2 배포, JWT 인증/인가, Role 기반 관리자 API까지 적용되어 있습니다.

---

## 주요 구현 내용

- Book / Member / Order CRUD REST API
- Controller / Service / Repository 계층 구조
- Request DTO / Response DTO 분리
- 공통 응답 구조 `ApiResponse<T>`
- 페이징 응답 구조 `PageResponse<T>`
- Bean Validation
- Global Exception Handling
- Spring Data JPA 기반 Repository 전환
- H2 / MySQL 프로필 분리
- JPA Auditing 기반 생성일 / 수정일 자동 관리
- `@ManyToOne(fetch = LAZY)` 연관관계 적용
- Lazy Loading과 OSIV 문제 해결
- N+1 문제 확인 및 `@EntityGraph` 최적화
- Pageable 기반 페이징 / 정렬
- 정렬 가능한 필드 제한
- Redis Cache 적용
- Redis JSON 직렬화
- Docker Compose 기반 App + MySQL + Redis 실행 환경
- Swagger / OpenAPI 문서화
- Spring Boot Actuator Health Check
- GitHub Actions CI 테스트 자동화
- GHCR 기반 Docker 이미지 빌드 및 배포
- AWS EC2 배포
- Nginx Reverse Proxy 적용
- Spring Security 적용
- BCrypt 비밀번호 암호화
- 로그인 API 구현
- JWT Access Token 발급
- JWT 인증 필터 구현
- SecurityContext 기반 현재 사용자 식별
- 주문 소유권 기반 인가
- Role 기반 USER / ADMIN 권한 분리
- 관리자 회원 조회 API
- 관리자 주문 조회 / 검색 / 강제 취소 API

---

## 기술 스택

### Backend

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Spring Cache
- Spring Boot Actuator
- Bean Validation
- Lombok
- JJWT

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
- GitHub Container Registry
- AWS EC2
- Nginx

---

## 전체 구조

기본 요청 흐름은 다음과 같습니다.

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

JWT 인증이 필요한 API의 흐름은 다음과 같습니다.

```text
Client
  ↓
Authorization: Bearer <accessToken>
  ↓
JwtAuthenticationFilter
  ↓
JWT 검증
  ↓
SecurityContext 저장
  ↓
Controller
  ↓
Service
  ↓
Repository
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

Docker Compose 실행 구조는 다음과 같습니다.

```text
Docker Compose
 ├── app   : Spring Boot API Server
 ├── mysql : MySQL Database
 └── redis : Redis Cache Server
```

배포 구조는 다음과 같습니다.

```text
Client
  ↓
Nginx :80
  ↓
Spring Boot App :8080
  ↓
MySQL / Redis
```

---

## 패키지 구조

```text
com.example.mvccrud
 ├── admin
 │   └── AdminController
 │
 ├── auth
 │   ├── AuthController
 │   ├── AuthService
 │   ├── LoginRequest
 │   ├── LoginResponse
 │   └── LoginFailedException
 │
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
 │   ├── Role
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
 │   ├── ForbiddenException
 │   ├── RedisCacheConfig
 │   ├── SecurityConfig
 │   └── security
 │       ├── JwtProvider
 │       ├── JwtAuthenticationFilter
 │       └── CustomUserPrincipal
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

---

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

---

### mysql 프로필 실행

MySQL과 Redis를 Docker로 실행한 뒤 Spring Boot를 로컬에서 실행합니다.

```bash
docker compose up -d mysql redis
```

```bash
SPRING_PROFILES_ACTIVE=mysql ./gradlew bootRun
```

---

### docker 프로필 실행

Spring Boot App, MySQL, Redis를 모두 Docker Compose로 실행합니다.

```bash
docker compose up -d --build
```

상태 확인:

```bash
docker ps
```

로그 확인:

```bash
docker logs -f mvc-crud-app
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

## 환경변수 설정

Docker 환경에서는 DB와 Redis 설정을 환경변수로 분리했습니다.

예시:

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

`application-docker.yml`에서는 환경변수를 사용합니다.

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

## API 목록

### Auth API

```text
POST /auth/login
GET  /auth/me
```

로그인 요청 예시:

```http
POST /auth/login
```

```json
{
  "email": "kim@test.com",
  "password": "password1234"
}
```

로그인 응답 예시:

```json
{
  "data": {
    "memberId": 1,
    "email": "kim@test.com",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

인증 요청 예시:

```http
Authorization: Bearer <accessToken>
```

---

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
GET    /orders/my
GET    /orders/{id}
PATCH  /orders/{id}/cancel
```

주문 생성 요청 예시:

```http
POST /orders
Authorization: Bearer <accessToken>
```

```json
{
  "bookId": 1,
  "quantity": 2
}
```

현재 로그인 사용자의 주문 목록 조회:

```http
GET /orders/my?page=0&size=10
Authorization: Bearer <accessToken>
```

내 주문 단건 조회:

```http
GET /orders/1
Authorization: Bearer <accessToken>
```

내 주문 취소:

```http
PATCH /orders/1/cancel
Authorization: Bearer <accessToken>
```

일반 사용자는 본인의 주문만 조회하거나 취소할 수 있습니다.

---

### Admin API

```text
GET   /admin/members
GET   /admin/orders
GET   /admin/orders/search
PATCH /admin/orders/{id}/cancel
```

관리자 회원 전체 조회:

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

관리자 API는 `ADMIN` 권한을 가진 사용자만 접근할 수 있습니다.

---

## 인증 / 인가

Spring Security와 JWT를 사용해 REST API 기반 인증 구조를 구현했습니다.

### 주요 기능

- 회원 비밀번호 BCrypt 암호화 저장
- 로그인 API 구현
- 로그인 성공 시 JWT Access Token 발급
- JWT 인증 필터 구현
- Authorization 헤더의 Bearer Token 검증
- SecurityContext 기반 현재 사용자 식별
- JWT claim에 memberId, email, role 포함
- Role 기반 USER / ADMIN 권한 분리
- 인증 실패 시 401 Unauthorized 응답 처리
- 권한 없는 접근 시 403 Forbidden 응답 처리

---

### 인증 흐름

```text
1. 회원가입
   POST /members
   → 비밀번호를 BCrypt로 암호화해 저장

2. 로그인
   POST /auth/login
   → email/password 검증
   → JWT Access Token 발급

3. 인증 요청
   Authorization: Bearer <accessToken>
   → JwtAuthenticationFilter에서 토큰 검증
   → SecurityContext에 인증 정보 저장

4. 보호된 API 접근
   → SecurityContext에서 현재 사용자 정보 사용
```

---

### 인가 방식

이 프로젝트에서는 두 가지 인가 방식을 구현했습니다.

```text
1. 소유권 기반 인가
2. 역할 기반 인가
```

#### 소유권 기반 인가

일반 사용자는 자신의 주문만 조회하거나 취소할 수 있습니다.

```text
GET   /orders/{id}
PATCH /orders/{id}/cancel
```

다른 사용자의 주문에 접근하면 403 Forbidden을 반환합니다.

```json
{
  "status": 403,
  "message": "본인의 주문만 조회할 수 있습니다.",
  "errors": null
}
```

```json
{
  "status": 403,
  "message": "본인의 주문만 취소할 수 있습니다.",
  "errors": null
}
```

#### 역할 기반 인가

회원은 `USER`, `ADMIN` 역할을 가집니다.

- `USER`: 자신의 주문 생성, 조회, 취소 가능
- `ADMIN`: 전체 회원 조회, 전체 주문 조회, 주문 검색, 주문 강제 취소 가능

관리자 API는 다음 Security 설정으로 보호합니다.

```java
.requestMatchers("/admin/**").hasRole("ADMIN")
```

JWT 인증 필터에서는 JWT의 role claim을 Spring Security 권한으로 변환합니다.

```java
new SimpleGrantedAuthority("ROLE_" + role)
```

Spring Security의 `hasRole("ADMIN")`은 내부적으로 `ROLE_ADMIN` 권한을 확인합니다.

---

### 권한 실패 응답

| 상황 | HTTP Status |
|---|---|
| 토큰 없이 보호된 API 접근 | `401 Unauthorized` |
| USER가 관리자 API 접근 | `403 Forbidden` |
| 다른 사용자의 주문 조회 | `403 Forbidden` |
| 다른 사용자의 주문 취소 | `403 Forbidden` |
| 로그인 실패 | `401 Unauthorized` |

---

## 주요 기능 설명

### 공통 응답 구조

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

### 예외 처리

공통 예외 처리를 위해 `GlobalExceptionHandler`를 사용했습니다.

처리한 주요 예외:

- `BookNotFoundException`
- `MemberNotFoundException`
- `OrderNotFoundException`
- `DuplicateEmailException`
- `InvalidSortException`
- `ForbiddenException`
- `LoginFailedException`
- `MethodArgumentNotValidException`
- `IllegalArgumentException`
- `IllegalStateException`
- `DataIntegrityViolationException`

---

### Validation

Request DTO에 Bean Validation을 적용했습니다.

예시:

```java
@NotBlank
private String title;

@Min(1)
private int price;
```

Validation 실패 시 400 Bad Request를 반환합니다.

---

### 정렬 가능한 필드 제한

허용되지 않은 필드로 정렬을 요청하면 `400 Bad Request`를 반환합니다.

```http
GET /books?sort=unknownField,asc
```

응답 예시:

```json
{
  "status": 400,
  "message": "정렬할 수 없는 필드입니다: unknownField"
}
```

---

### Redis 캐시

책 단건 조회 API에 Redis 캐시를 적용했습니다.

적용 대상:

```http
GET /books/{id}
```

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

책 정보가 수정되거나 삭제되면 기존 캐시를 삭제합니다.

```text
PUT /books/{id}    → book::{id} 캐시 삭제
PATCH /books/{id}  → book::{id} 캐시 삭제
DELETE /books/{id} → 삭제 성공 시 book::{id} 캐시 삭제
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

API 그룹:

```text
Book API
Member API
Order API
Auth API
Admin API
```

---

## Actuator Health Check

Spring Boot Actuator를 추가하여 애플리케이션 상태 확인 API를 제공합니다.

Health Check:

```http
GET /actuator/health
```

응답 예시:

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

Liveness / Readiness:

```http
GET /actuator/health/liveness
GET /actuator/health/readiness
```

이 엔드포인트는 Docker, 클라우드 배포, Kubernetes 환경에서 애플리케이션 상태를 확인하는 데 사용할 수 있습니다.

---

## 테스트

### Service Test

대상:

```text
BookServiceTest
MemberServiceTest
OrderServiceTest
```

검증 내용:

- 등록
- 단건 조회
- 전체 조회
- 수정
- 부분 수정
- 삭제
- 검색
- 예외 상황 검증
- 비밀번호 암호화 검증
- 관리자 회원 생성 검증

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

---

### Security Integration Test

Spring Security와 JWT 인증/인가 흐름을 통합 테스트로 검증했습니다.

검증 내용:

- 로그인 성공 시 JWT 발급
- 잘못된 로그인 요청 시 401 Unauthorized
- JWT로 현재 사용자 조회
- 토큰 없이 주문 생성 시 401 Unauthorized
- 토큰이 있으면 주문 생성 성공
- 주문 생성 시 요청 body의 memberId가 아니라 JWT의 memberId 사용
- 현재 로그인 사용자의 주문 목록 조회
- 다른 사용자의 주문 조회 시 403 Forbidden
- 다른 사용자의 주문 취소 시 403 Forbidden
- USER가 관리자 API 접근 시 403 Forbidden
- ADMIN이 관리자 API 접근 시 200 OK
- ADMIN의 전체 회원 조회
- ADMIN의 전체 주문 조회
- ADMIN의 주문 검색
- ADMIN의 주문 강제 취소

---

### JPA Integration Test

대상:

```text
BookJpaIntegrationTest
MemberJpaIntegrationTest
OrderJpaIntegrationTest
```

검증 내용:

- DB 저장
- DB 조회
- 변경 감지
- 삭제
- 검색
- 주문 취소
- 예외 상황
- 연관관계 조회
- EntityGraph 기반 조회 최적화

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
Actuator  : http://localhost:8080/actuator/health
MySQL     : localhost:3306
Redis     : localhost:6379
```

Docker 환경에서는 `docker` 프로필을 사용합니다.

```yaml
SPRING_PROFILES_ACTIVE: docker
```

---

## CI/CD

GitHub Actions를 사용해 테스트 자동화와 Docker 이미지 빌드, EC2 자동 배포를 구성했습니다.

main 브랜치에 push되면 다음 순서로 동작합니다.

```text
1. GitHub Actions에서 테스트 실행
2. 테스트 성공 시 Docker 이미지 빌드
3. GitHub Container Registry에 이미지 Push
4. EC2 서버에 SSH 접속
5. EC2에서 최신 Docker 이미지 Pull
6. Docker Compose로 애플리케이션 재실행
```

GitHub Actions에서 실행하는 주요 명령:

```bash
./gradlew clean test
```

EC2 배포 시 실행하는 주요 명령:

```bash
docker compose pull app
docker compose up -d
docker image prune -f
```

---

## GHCR 기반 Docker 이미지 배포

기존에는 EC2 서버에서 직접 소스코드를 pull한 뒤 Docker 이미지를 빌드했습니다.

```text
EC2
→ git pull
→ docker compose up -d --build
```

현재는 GitHub Actions에서 Docker 이미지를 빌드하고, GitHub Container Registry에 push한 뒤 EC2 서버에서는 이미지를 Pull하여 실행합니다.

```text
GitHub Actions
→ Test
→ Docker Image Build
→ GHCR Push
→ EC2 Deploy
→ docker compose pull app
→ docker compose up -d
```

이미지:

```text
ghcr.io/nahcoh/spring_self_study_with_ai:latest
```

이를 통해 EC2 서버의 빌드 부담을 줄이고, 배포 결과물을 Docker 이미지로 관리할 수 있게 되었습니다.

---

## AWS EC2 배포

AWS EC2 Ubuntu 서버에 Docker Compose 기반으로 애플리케이션을 배포했습니다.

### 배포 구성

- EC2 Ubuntu 24.04
- Docker
- Docker Compose
- Spring Boot App
- MySQL
- Redis
- Nginx

### Nginx Reverse Proxy

외부 사용자가 Spring Boot의 8080 포트에 직접 접근하지 않도록 Nginx Reverse Proxy를 적용했습니다.

기존 접근 방식:

```text
http://<EC2_PUBLIC_IP>:8080
```

변경 후 접근 방식:

```text
http://<EC2_PUBLIC_IP>
```

구조:

```text
Client
  ↓
Nginx :80
  ↓
Spring Boot App :8080
```

Nginx 설정 파일:

```text
/etc/nginx/sites-available/mvc-crud
```

주요 설정:

```nginx
server {
    listen 80;
    server_name _;

    location / {
        proxy_pass http://localhost:8080;

        proxy_http_version 1.1;

        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

설정 적용:

```bash
sudo ln -s /etc/nginx/sites-available/mvc-crud /etc/nginx/sites-enabled/mvc-crud
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx
```

Nginx 적용 후 보안 그룹에서 8080 포트를 닫고, 80 포트만 외부에 공개했습니다.

---

## Docker Restart Policy

EC2 재부팅 이후에도 컨테이너가 자동으로 다시 실행되도록 Docker Compose 서비스에 restart policy를 적용했습니다.

```yaml
restart: unless-stopped
```

적용 대상:

- app
- mysql
- redis

재부팅 테스트 결과, EC2 재시작 후 세 컨테이너가 모두 자동으로 복구되는 것을 확인했습니다.

```text
mvc-crud-app    Up
mvc-crud-mysql  Up healthy
mvc-crud-redis  Up healthy
```

이를 통해 EC2 재부팅이나 Docker daemon 재시작 상황에서도 애플리케이션이 자동 복구될 수 있도록 기본 안정성을 확보했습니다.

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
- 환경변수 기반 설정 분리
- Spring Boot Actuator Health Check 적용
- AWS EC2 배포
- Nginx Reverse Proxy 적용
- Docker Restart Policy 적용
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
- Security 통합 테스트 작성
- GitHub Actions CI 구성
- GHCR 기반 Docker 이미지 배포 구성
- GitHub push 시 자동 테스트 / 이미지 빌드 / EC2 배포
- Spring Security 적용
- BCrypt 비밀번호 암호화
- 로그인 API 구현
- JWT Access Token 발급
- JWT 인증 필터 구현
- `/auth/me` API 구현
- JWT 기반 주문 생성 API 보호
- JWT 기반 내 주문 조회 API 구현
- JWT 기반 내 주문 단건 조회 API 구현
- JWT 기반 내 주문 취소 API 구현
- 주문 소유권 기반 인가 구현
- Role 기반 USER / ADMIN 권한 분리
- 관리자 회원 전체 조회 API 구현
- 관리자 주문 전체 조회 API 구현
- 관리자 주문 검색 API 구현
- 관리자 주문 강제 취소 API 구현

---

## 다음 개선 예정

- Swagger JWT Authorize 설정
- 배포 환경용 관리자 계정 생성 방식 정리
- Refresh Token / Access Token 재발급 기능
- 로그아웃 API
- Admin 통계 API
- 테스트 코드 Fixture 분리
- 운영 환경용 DB 설정 고도화
- HTTPS 적용
- Redis TTL 전략 정리
- 캐시 대상 확장
- 주문 구조를 `OrderItem` 기반으로 확장
- API 에러 응답 구조 추가 개선
- Kubernetes 맛보기