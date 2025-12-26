# stocat-auth

`stocat-auth` 백엔드 서비스는 stocat 서비스에서 인증 기능을 제공하는 Spring Boot 기반 REST API 서버입니다.

## 주요 기능
- JWT 인증/인가 (로그인, 회원가입, 토큰 검증)
- RESTful 설계 및 예외 처리

## 기술 스택
- Java 21
- Spring Boot 3.5.x
- Spring Security + JWT (io.jsonwebtoken)
- Spring Data JPA
- H2 (개발용 인메모리) → PostgreSQL/MySQL 전환 가능
- Gradle 8.x (Wrapper 포함)
- Lombok (선택)

## 프로젝트 구조
```
src/
└─ main/
   ├─ java/com/example/stocat/
   │  ├─ config/
   │  ├─ controller/
   │  ├─ domain/
   │  ├─ repository/
   │  ├─ service/
   │  └─ StocatApplication.java
   └─ resources/
      └─ application.yml
```

## 빠른 시작
```bash
# 프로젝트 클론
git clone https://github.com/your-username/stocat-backend.git
cd stocat-auth

# 환경 설정: src/main/resources/application.yml 수정
# JWT 시크릿, DB URL 등

# 실행 (Unix/macOS)
./gradlew bootRun

# 실행 (Windows)
gradlew.bat bootRun
```

## 주요 엔드포인트
```
POST   /api/auth/register   회원가입
POST   /api/auth/login      로그인 (JWT 발급)
```
