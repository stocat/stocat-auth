# Auth API 명세서

## 1. 공통 응답 포맷
모든 API 응답은 아래와 같은 JSON 구조를 가집니다.

```json
{
  "code": 1000,
  "message": "성공",
  "data": { ... }
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `code` | Integer | 응답 코드 (성공 시 `1000`, 실패 시 에러 코드) |
| `message` | String | 응답 메시지 (성공 시 "성공", 실패 시 에러 사유) |
| `data` | Object | 실제 데이터 (없을 경우 `null`) |

---

## 2. API 목록

### 2.1. 회원 가입
로컬 회원을 생성합니다. 이메일과 닉네임 중복 검사를 수행합니다.

- **URL**: `/auth/signup`
- **Method**: `POST`
- **Content-Type**: `application/json`

**Request Body**

```json
{
  "email": "test@example.com",
  "nickname": "홍길동",
  "password": "P@ssw0rd!"
}
```

| 필드 | 필수 | 설명 |
| --- | --- | --- |
| `email` | Y | 이메일 주소 |
| `nickname` | Y | 닉네임 |
| `password` | Y | 비밀번호 (평문) |

**Response (성공)**

```json
{
  "code": 1000,
  "message": "성공",
  "data": null
}
```

**Response (실패 예시 - 중복 이메일)**

```json
{
  "code": 10002,
  "message": "이미 사용 중인 이메일입니다.",
  "data": null
}
```

---

### 2.2. 로그인
이메일과 비밀번호를 검증하고 액세스/리프레시 토큰을 발급합니다.

- **URL**: `/auth/login`
- **Method**: `POST`
- **Content-Type**: `application/json`

**Request Body**

```json
{
  "email": "test@example.com",
  "password": "P@ssw0rd!"
}
```

**Response (성공)**

```json
{
  "code": 1000,
  "message": "성공",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Response (실패 예시 - 자격 증명 오류)**

```json
{
  "code": 10004,
  "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "data": null
}
```

---

### 2.3. 내 정보 요약 조회
현재 로그인한 사용자의 간단한 정보를 조회합니다. 헤더에 액세스 토큰이 필요합니다.

- **URL**: `/auth/summary`
- **Method**: `GET`
- **Header**: `Authorization: Bearer {accessToken}`

**Response (성공)**

```json
{
  "code": 1000,
  "message": "성공",
  "data": {
    "id": 1,
    "email": "test@example.com",
    "nickname": "홍길동"
  }
}
```

---

### 2.4. 에러 코드 목록 조회
서버에서 정의된 인증 관련 에러 코드 목록을 반환합니다.

- **URL**: `/auth/errors`
- **Method**: `GET`

**Response (성공)**

```json
{
  "code": 1000,
  "message": "성공",
  "data": [
    {
      "code": 10000,
      "message": "서버 에러가 발생했습니다."
    },
    {
      "code": 10001,
      "message": "요청값이 올바르지 않습니다."
    },
    {
      "code": 10002,
      "message": "이미 사용 중인 이메일입니다."
    },
    ...
  ]
}
```

---

## 3. 주요 에러 코드 (AuthErrorCode)

| 코드 | 메시지 | 설명 |
| --- | --- | --- |
| `10000` | 서버 에러가 발생했습니다. | 내부 서버 오류 |
| `10001` | 요청값이 올바르지 않습니다. | 파라미터 유효성 검증 실패 |
| `10002` | 이미 사용 중인 이메일입니다. | 회원가입 시 이메일 중복 |
| `10003` | 이미 사용 중인 닉네임입니다. | 회원가입 시 닉네임 중복 |
| `10004` | 이메일 또는 비밀번호가 올바르지 않습니다. | 로그인 실패 |
| `10005` | 회원 정보를 찾을 수 없습니다. | 존재하지 않는 회원 조회 시 |
