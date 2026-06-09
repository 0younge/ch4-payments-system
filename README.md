# CH4 Payments System

커머스 주문, 결제, 환불 흐름을 구현한 Spring Boot 기반 데모 프로젝트입니다.

상품 조회, 장바구니, 주문 생성, PortOne 결제 확정, 포인트 적립/사용, 부분 환불과 전액 환불을 하나의 서비스 흐름으로 다룹니다.

## 설계 기준

최우선 설계 기준은 [commerce_design_final_reviewed.md](commerce_design_final_reviewed.md)입니다.

구현과 문서가 충돌할 경우 설계 문서를 우선합니다.

## 기술 스택

| 구분 | 내용 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Web | Spring Web MVC |
| Persistence | Spring Data JPA |
| Database | MySQL |
| Security | Spring Security, JWT |
| Validation | Spring Validation |
| Payment | PortOne KG이니시스 |
| Build | Gradle |

## 주요 기능

- 이메일 기반 회원가입과 로그인
- JWT 기반 인증
- 상품 목록/상세 조회
- 장바구니 상품 추가, 수량 변경, 삭제, 전체 비우기
- 장바구니 전체 또는 선택 상품 주문
- 주문 생성 시 재고 선차감과 결제 레코드 동시 생성
- PortOne 결제 확정 및 웹훅 처리
- 결제 완료 시 포인트 사용 확정, PG 결제 금액 1% 적립, 장바구니 비우기
- 결제 실패 또는 결제대기 주문 취소 시 재고와 사용 포인트 복구
- 부분 환불과 전액 환불
- 포인트 원장 조회

## 프로젝트 구조

```text
src/main/java/com/example/ch4paymentssystem
├── common
├── config
├── domain
│   ├── auth
│   ├── cart
│   ├── order
│   ├── payment
│   ├── point
│   ├── product
│   ├── refund
│   └── user
├── global
│   ├── exception
│   ├── response
│   └── security
└── infra
    └── portone
```

## 실행 전 준비

`application.yaml`은 `.env` 파일을 읽어 설정합니다.

`.env`는 Git 관리 대상이 아니며, 실제 비밀번호와 PortOne 시크릿은 외부에 공유하지 않습니다.

```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ch4_payments
DB_USERNAME=ch4_user
DB_PASSWORD=change_me
DB_SERVER_TIMEZONE=Asia/Seoul
DB_CHARACTER_ENCODING=UTF-8
DB_USE_SSL=false
DB_ALLOW_PUBLIC_KEY_RETRIEVAL=true

DDL_AUTO=create
SQL_INIT_MODE=always

JWT_SECRET_KEY=change_me_to_long_random_secret
JWT_EXPIRATION_TIME=3600000

PORTONE_BASE_URL=https://api.portone.io
PORTONE_API_SECRET=change_me
PORTONE_STORE_ID=change_me
PORTONE_CHANNEL_KEY=change_me
PORTONE_WEBHOOK_SECRET=change_me

MYSQL_IMAGE=mysql:8.4
MYSQL_CONTAINER_NAME=ch4-payments-mysql
MYSQL_ROOT_PASSWORD=change_me
MYSQL_TIMEZONE=Asia/Seoul
```

## 실행 방법

MySQL 컨테이너를 사용하는 경우:

```bash
docker compose up -d mysql
```

애플리케이션 실행:

```bash
./gradlew bootRun
```

테스트 실행:

```bash
./gradlew test
```

정적 데모 화면은 애플리케이션 실행 후 아래 주소에서 확인할 수 있습니다.

```text
http://localhost:8080
```

## 테스트 데이터

`SQL_INIT_MODE=always`로 `data.sql`을 실행하면 아래 테스트 데이터가 생성됩니다.

| 이메일 | 비밀번호 | 보유 포인트 |
| --- | --- | --- |
| test1@test.com | Password1234! | 10000 |
| test2@test.com | Password1234! | 5000 |
| test3@test.com | Password1234! | 20000 |

상품 더미 데이터도 함께 생성됩니다.

## 인증

로그인 성공 시 JWT가 발급됩니다.

인증이 필요한 API는 아래 형식으로 토큰을 전달합니다.

```text
Authorization: Bearer {token}
```

회원은 본인의 장바구니, 주문, 결제, 환불, 포인트만 접근할 수 있습니다.

## 공통 응답 형식

```json
{
  "success": true,
  "message": "처리 메시지",
  "data": {}
}
```

## API 요약

### 인증

| 기능 | Method | URL | 인증 |
| --- | --- | --- | --- |
| 회원가입 | POST | `/api/auth/signup` | 불필요 |
| 로그인 | POST | `/api/auth/login` | 불필요 |

### 상품

| 기능 | Method | URL | 인증 |
| --- | --- | --- | --- |
| 상품 목록 조회 | GET | `/api/products` | 불필요 |
| 상품 단건 조회 | GET | `/api/products/{productId}` | 불필요 |

상품 목록 Query:

```text
category, minPrice, maxPrice, status, sort, page, size
```

### 장바구니

| 기능 | Method | URL | 인증 |
| --- | --- | --- | --- |
| 장바구니 조회 | GET | `/api/cart` | 필요 |
| 상품 담기 | POST | `/api/cart/items` | 필요 |
| 수량 변경 | PATCH | `/api/cart/items/{cartItemId}` | 필요 |
| 상품 개별 삭제 | DELETE | `/api/cart/items/{cartItemId}` | 필요 |
| 장바구니 전체 비우기 | DELETE | `/api/cart/items` | 필요 |

상품 담기 Request:

```json
{
  "productId": 1,
  "quantity": 2
}
```

수량 변경 Request:

```json
{
  "quantity": 3
}
```

### 주문

| 기능 | Method | URL | 인증 |
| --- | --- | --- | --- |
| 주문서 미리보기 | GET | `/api/orders/preview` | 필요 |
| 주문 생성 | POST | `/api/orders` | 필요 |
| 내 주문 내역 조회 | GET | `/api/orders` | 필요 |
| 주문 상세 조회 | GET | `/api/orders/{orderId}` | 필요 |
| 주문 취소 | POST | `/api/orders/{orderId}/cancel` | 필요 |

주문서 미리보기 Query:

```text
cartItemIds
```

주문 생성 Request:

```json
{
  "cartItemIds": [1, 2],
  "usedPointAmount": 1000
}
```

`cartItemIds`가 없으면 전체 장바구니를 기준으로 주문합니다.

주문 취소는 `PAYMENT_PENDING` 상태에서만 가능합니다. 결제 완료 주문은 환불 API로 처리합니다.

### 결제

| 기능 | Method | URL | 인증 |
| --- | --- | --- | --- |
| 결제 확정 | POST | `/api/payments/confirm` | 필요 |
| PortOne 웹훅 수신 | POST | `/api/payments/webhook` | HMAC 서명 검증 |

결제 확정 Request:

```json
{
  "orderId": 1,
  "portonePaymentId": "payment-..."
}
```

### 포인트

| 기능 | Method | URL | 인증 |
| --- | --- | --- | --- |
| 포인트 잔액 조회 | GET | `/api/points/balance` | 필요 |
| 포인트 거래 내역 조회 | GET | `/api/points/histories` | 필요 |

포인트 거래 내역 Query:

```text
page, size
```

### 환불

| 기능 | Method | URL | 인증 |
| --- | --- | --- | --- |
| 환불 요청 | POST | `/api/refunds` | 필요 |

환불 요청 Request:

```json
{
  "orderId": 1,
  "refundReason": "단순 변심",
  "refundItems": [
    {
      "orderItemId": 1,
      "quantity": 1
    }
  ]
}
```

환불 금액은 클라이언트가 전달하지 않고 서버가 주문 상품 스냅샷 가격과 환불 수량으로 계산합니다.

## 상태 값

### ProductStatus

| 값 | 의미 |
| --- | --- |
| ON_SALE | 판매중 |
| SOLD_OUT | 품절 |

### ProductSort

| 값 | 의미 |
| --- | --- |
| LATEST | 최신순 |
| PRICE_ASC | 가격 오름차순 |
| PRICE_DESC | 가격 내림차순 |

### OrderStatus

| 값 | 의미 |
| --- | --- |
| PAYMENT_PENDING | 결제 대기 |
| PAID | 주문 완료 |
| CANCELLED | 주문 취소 |

### PaymentStatus

| 값 | 의미 |
| --- | --- |
| READY | 결제 대기 |
| PAID | 결제 완료 |
| FAILED | 결제 실패 |
| PARTIAL_REFUNDED | 부분 환불 |
| REFUNDED | 전액 환불 |

### RefundStatus

| 값 | 의미 |
| --- | --- |
| REQUESTED | 환불 요청 |
| COMPLETED | 환불 완료 |
| FAILED | 환불 실패 |

### PointType

| 값 | 의미 |
| --- | --- |
| USE | 포인트 사용 |
| EARN | 포인트 적립 |
| REFUND_USED | 사용 포인트 복구 |
| CANCEL_EARNED | 적립 포인트 회수 |

## 핵심 비즈니스 규칙

- 상품 생성, 수정, 삭제 API는 구현 대상이 아닙니다.
- 동일 상품을 장바구니에 다시 담으면 기존 수량이 증가합니다.
- 주문 생성 시 재고를 즉시 선차감합니다.
- 주문 생성 시 장바구니는 비우지 않습니다.
- 결제 완료 시 주문 대상 장바구니 상품을 비웁니다.
- 포인트는 결제 시 잔액 내에서 자유롭게 사용할 수 있고 최소 사용 단위는 1원입니다.
- 결제 완료 시 PG 실결제 금액의 1%를 포인트로 적립합니다.
- 결제 실패 또는 결제대기 주문 취소 시 선차감 재고와 사용 포인트를 복구합니다.
- 결제 완료 주문 취소는 주문 취소 API가 아닌 환불 API로 처리합니다.
- 환불 수량은 주문 상품의 잔여 환불 가능 수량을 초과할 수 없습니다.
- 복합결제 환불은 포인트 환불 금액과 PG 환불 금액을 분리해 기록합니다.

## 상태 흐름

### 주문

```text
PAYMENT_PENDING ──결제 성공──────────→ PAID
PAYMENT_PENDING ──결제 실패/직접 취소──→ CANCELLED
PAID ──전액 환불────────────────────→ CANCELLED
```

### 결제 성공

```text
주문 생성
  ├─ OrderStatus = PAYMENT_PENDING
  └─ PaymentStatus = READY
      ↓
PortOne 결제 완료
      ↓
서버 결제 확정 요청 또는 웹훅 수신
      ↓
PortOne API 재조회 및 검증
      ↓
OrderStatus = PAID
PaymentStatus = PAID
      ↓
포인트 사용 확정
포인트 1% 적립
장바구니 비우기
```

### 결제 실패 또는 결제대기 취소

```text
OrderStatus = PAYMENT_PENDING
PaymentStatus = READY
      ↓
결제 실패 또는 회원 직접 취소
      ↓
OrderStatus = CANCELLED
PaymentStatus = FAILED
      ↓
재고 복구
사용 포인트 복구
장바구니 유지
```

### 환불

```text
OrderStatus = PAID
PaymentStatus = PAID 또는 PARTIAL_REFUNDED
      ↓
환불 요청
      ↓
잔여 환불 가능 수량 검증
      ↓
환불 금액 자동 산정
포인트 환불 금액 / PG 환불 금액 분리
      ↓
RefundStatus = REQUESTED
      ↓
PortOne 취소 요청
      ↓
부분 환불: PaymentStatus = PARTIAL_REFUNDED, OrderStatus = PAID 유지
전액 환불: PaymentStatus = REFUNDED, OrderStatus = CANCELLED
```

