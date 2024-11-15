# 🎠놀이공원 티켓 구매 사이트 프로젝트
## 프로젝트 소개

이 프로젝트는 놀이공원 내에 위치한 인기 동물원 판다월드의 입장권 구매 요청이 **특정 시간대에 폭증하는 상황**을 가정하여 설계된 개인 프로젝트입니다. **한정된 재고를 선착순으로 판매하는 시스템**을 구현하여, 대규모 트래픽 상황에서도 안정적이고 정확하게 작동할 수 있도록 개발되었습니다.

초기에는 모놀리식 구조로 설계되었으나, 이후 **마이크로서비스 아키텍처(MSA)로 전환**하여 유저 관리, 티켓 관리, 주문 관리 서비스를 독립적으로 운영할 수 있도록 설계하였습니다.

<br>

## 📅 일정
**기간** : 2024.10.16 ~ 2024.11.16 (4주)

<br>

## 🛠 개발 환경

- Back-end : Java 21, Spring Boot 3.3.5, Spring Data JPA, Spring Security
- Database : MySQL, Redis
- infra : Docker, Docker compose, Apache Kafka
- tool : Notion, GitHub

<br>

## ⚙ 실행
```
docker compose up -d
```

<br>




## 🎯 주요 기능

**유저 관리 (User Service)**
- 이메일 인증 기반 회원가입 및 개인정보 암호화 저장
- JWT를 활용한 로그인/로그아웃 및 모든 기기에서 로그아웃 기능

**티켓 관리 (Ticket Service)**

- 티켓 리스트 조회 및 상세 정보 제공

**주문 관리 (Order Service)**

- 티켓 주문 및 결제 처리
- 마이페이지에서 주문 내역 조회 및 주문 취소
- 관람일 기준 환불 정책 적용
- 장바구니 기능

<br>



## 🗂 API 문서
[놀이공원 티켓 구매 사이트 API 명세 (POSTMAN)](https://documenter.getpostman.com/view/32408353/2sAY55ZHJX)   

<br>


## 🔥 성능 최적화
- [Redis 캐싱을 통한 결제 처리 속도 00% 향상]()   
`Pending Order` 데이터를 Redis에 저장하여 데이터베이스 요청을 줄이고 결제 프로세스의 처리 속도를 00% 개선했습니다.

- [Lua 스크립트로 초당 300건 동시 처리 구현]()  
Redis에서 Lua 스크립트를 사용해 재고 감소 연산을 원자적으로 처리, 초당 최대 300건의 동시 주문을 안정적으로 처리할 수 있도록 구현했습니다.  

- [MSA 도입으로 확장성과 안정성 확보]()  
Eureka 서버로 서비스 디스커버리를 구현하고 Ribbon으로 클라이언트 사이드 로드 밸런싱을 처리해 트래픽 분산을 최적화했습니다. Resilience4j 서킷 브레이커를 통해 장애 확산을 방지하며 시스템 안정성을 강화했습니다.

<br>  

## 🔫 트러블 슈팅
- [동시성 문제 해결 및 결제 프로세스 최적화]()  
Pending 상태에서 재고를 임시 예약하고, 결제 완료 시 확정하는 로직을 도입하여 재고 충돌 문제를 해결했습니다.
Kafka로 재고 감소 이벤트를 비동기로 처리하고 Lua 스크립트를 활용한 원자적 연산으로 동시성 문제를 해결, 결제 성공률을 100%로 향상시켰습니다.

- [API Gateway 통신 문제 해결]()  
JWT 검증이 필요 없는 호출은 검증 필터를 우회하도록 설정하고, 내부 호출은 `/internal` 경로로 명시적으로 처리하여 인증 없이 접근 가능하도록 설정했습니다.

<br>  

## 💭 기술적 의사결정

- [Redis 캐싱 전략으로 성능 최적화]()   
`Pending Order`와 장바구니 데이터를 Redis에 캐싱하여 데이터베이스 요청을 줄이고 성능을 향상했습니다.

- [MSA 환경에서 서비스 간 동기 호출(OpenFeign)과 비동기 호출(Kafka)의 활용]()    
OpenFeign으로 데이터 검증 요청을 처리하고, Kafka를 사용해 재고 감소 이벤트를 비동기로 처리하여 효율성을 높였습니다.

- [Refresh Token을 활용한 중복 로그인 방지 및 로그아웃 구현]()  
JWT의 `sessionId`로 기기를 구분해 중복 로그인을 방지하고, Refresh Token 블랙리스트를 활용해 모든 기기에서 로그아웃을 구현했습니다.

- [Custom Argument Resolver로 유저 정보 처리]()  
JWT 검증을 API Gateway에서만 처리하고, 각 서비스에서는 Security 설정 없이 요청을 처리하기 위해 도입했습니다. Gateway에서 유저 정보를 HTTP 헤더로 전달하며, 이를 `Custom Argument Resolver`를 통해 처리하도록 설계했습니다.

<br>

## 🏗 프로젝트 아키텍처
![아키텍처](https://github.com/user-attachments/assets/837cc05b-5983-4909-ae08-f4fcd9bee7b2)

<br>

## 🏷 ERD
![ERD](https://github.com/user-attachments/assets/424132dc-e611-485a-8480-0feaa8b5c4d0)

<br>

## 📂 폴더 구조

```
📦eureka-server
 ┣ 📂src
 ┃ ┣ 📂main
 ┃ ┃ ┣ 📂java.com.sparta
 ┃ ┃ ┃ ┗📂eurekaserver
 ┃ ┃ ┃ ┃ ┗📂security
 ┃ ┃ ┗ 📂resources
 ┗ ┗ 📂test

📦gateway
 ┣ 📂src
 ┃ ┣ 📂main
 ┃ ┃ ┣ 📂java.com.sparta
 ┃ ┃ ┃ ┗ 📂gateway
 ┃ ┃ ┃ ┃ ┗ 📂security
 ┃ ┃ ┗ 📂resources
 ┗ ┗ 📂test

📦order-service
 ┣ 📂src
 ┃ ┣ 📂main
 ┃ ┃ ┣ 📂java.com.sparta
 ┃ ┃ ┃ ┗ 📂orderservice
 ┃ ┃ ┃ ┃ ┣ 📂client
 ┃ ┃ ┃ ┃ ┣ 📂config
 ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┣ 📂event
 ┃ ┃ ┃ ┃ ┣ 📂exception
 ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┣ 📂service
 ┃ ┃ ┃ ┃ ┗ 📂toss
 ┃ ┃ ┗ 📂resources
 ┗ ┗ 📂test

📦ticket-service
 ┣ 📂src
 ┃ ┣ 📂main
 ┃ ┃ ┣ 📂java.com.sparta
 ┃ ┃ ┃ ┗ 📂ticketservice
 ┃ ┃ ┃ ┃ ┣ 📂config
 ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┣ 📂event
 ┃ ┃ ┃ ┃ ┣ 📂exception
 ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┗ 📂service
 ┃ ┃ ┗ 📂resources
 ┗ ┗ 📂test

📦user-service
 ┣ 📂src
 ┃ ┣ 📂main
 ┃ ┃ ┣ 📂java.com.sparta
 ┃ ┃ ┃ ┗ 📂userservice
 ┃ ┃ ┃ ┃ ┣ 📂config
 ┃ ┃ ┃ ┃ ┣ 📂controller
 ┃ ┃ ┃ ┃ ┣ 📂dto
 ┃ ┃ ┃ ┃ ┣ 📂entity
 ┃ ┃ ┃ ┃ ┣ 📂exception
 ┃ ┃ ┃ ┃ ┣ 📂jwt
 ┃ ┃ ┃ ┃ ┣ 📂repository
 ┃ ┃ ┃ ┃ ┗ 📂service
 ┃ ┃ ┗ 📂resources
 ┗ ┗ 📂test


```
