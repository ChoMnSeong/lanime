# Lanime API 명세서

> Base URL: `/api/v1`
> 모든 응답은 `ApiResponse<T>` 형태로 반환됩니다.

## 공통 응답 형식

```json
{
  "success": true,
  "message": "메시지 (선택)",
  "data": { }
}
```

### 공통 에러 코드

| 코드 | HTTP | 설명 |
|------|------|------|
| C001 | 400 | 입력값이 올바르지 않습니다. |
| C002 | 500 | 서버 내부 오류가 발생했습니다. |
| C003 | 403 | 해당 리소스에 대한 권한이 없습니다. |
| C004 | 401 | 인증 정보가 없거나 유효하지 않습니다. |
| U001 | 404 | 해당 사용자를 찾을 수 없습니다. |
| U002 | 400 | 이미 존재하는 이메일입니다. |
| U003 | 403 | 이메일 인증이 완료되지 않았습니다. |
| A001 | 404 | 해당 애니메이션 정보를 찾을 수 없습니다. |
| R001 | 409 | 이미 리뷰를 작성하셨습니다. |
| R002 | 404 | 작성된 리뷰를 찾을 수 없습니다. |
| C101 | 404 | 댓글을 찾을 수 없습니다. |

### 인증 헤더

| 헤더 | 설명 |
|------|------|
| `Authorization: Bearer {accessToken}` | 사용자 토큰 (로그인 후 발급) |
| `X-Profile-Token: {profileToken}` | 프로필 토큰 (프로필 선택 후 발급) |

---

## 1. 인증 (Auth)

### 1-1. 이메일 중복 체크

**POST** `/auth/check-email`

**Request Body**
```json
{
  "email": "user@example.com"
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "isDuplicate": true
  }
}
```

---

### 1-2. 인증 메일 발송

**POST** `/auth/send-verification`

**Request Body**
```json
{
  "email": "user@example.com"
}
```

**Response**
```json
{
  "success": true,
  "message": "인증 번호가 이메일로 발송되었습니다."
}
```

---

### 1-3. 인증 코드 검증

**POST** `/auth/verify-code`

**Request Body**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response**
```json
{
  "success": true,
  "data": true
}
```

---

### 1-4. 회원가입

**POST** `/auth/signup`

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "Password1!",
  "nickname": "닉네임"
}
```

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| email | String | O | 이메일 형식 |
| password | String | O | 8~20자, 영문+숫자+특수문자 포함 |
| nickname | String | O | 2~10자 |

**Response**
```json
{
  "success": true,
  "message": "회원가입에 성공했습니다."
}
```

---

### 1-5. 토큰 재발급

**POST** `/auth/refresh`

만료된 Access Token을 Refresh Token으로 재발급합니다.

**Request Body**
```json
{
  "refreshToken": "eyJ..."
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "expiresIn": 21600,
    "tokenType": "Bearer"
  }
}
```

> Refresh Token이 만료되었거나 유효하지 않으면 **401** 반환. Access Token을 Refresh Token으로 사용하면 **401** 반환.

---

### 1-6. 로그인

**POST** `/auth/signin`

**Request Body**
```json
{
  "email": "user@example.com",
  "password": "Password1!"
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "expiresIn": 3600,
    "tokenType": "Bearer"
  }
}
```

---

### 1-6. 비밀번호 재설정 코드 발송

**POST** `/auth/forgot-password`

**Request Body**
```json
{
  "email": "user@example.com"
}
```

**Response**
```json
{
  "success": true,
  "message": "비밀번호 재설정 코드가 이메일로 발송되었습니다."
}
```

---

### 1-7. 비밀번호 재설정

**POST** `/auth/reset-password`

**Request Body**
```json
{
  "email": "user@example.com",
  "token": "재설정토큰",
  "newPassword": "NewPassword1!"
}
```

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| email | String | O | 이메일 형식 |
| token | String | O | 재설정 코드 |
| newPassword | String | O | 8~20자, 영문+숫자+특수문자 포함 |

**Response**
```json
{
  "success": true,
  "message": "비밀번호가 변경되었습니다."
}
```

---

### 1-8. 계정 삭제

**DELETE** `/auth/account`
**인증 필요:** `Authorization`

**Response**
```json
{
  "success": true,
  "message": "계정이 삭제되었습니다."
}
```

---

### 1-9. 이메일 변경 인증 발송

**POST** `/auth/account/email/send-verification`
**인증 필요:** `Authorization`

**Request Body**
```json
{
  "email": "newemail@example.com"
}
```

**Response**
```json
{
  "success": true,
  "message": "인증 번호가 이메일로 발송되었습니다."
}
```

---

### 1-10. 이메일 변경

**PATCH** `/auth/account/email`
**인증 필요:** `Authorization`

**Request Body**
```json
{
  "newEmail": "newemail@example.com",
  "verificationCode": "123456"
}
```

**Response**
```json
{
  "success": true,
  "message": "이메일이 변경되었습니다."
}
```

---

### 1-11. 비밀번호 변경 (로그인 상태)

**PATCH** `/auth/account/password`
**인증 필요:** `Authorization`

**Request Body**
```json
{
  "currentPassword": "OldPassword1!",
  "newPassword": "NewPassword1!"
}
```

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| currentPassword | String | O | 현재 비밀번호 |
| newPassword | String | O | 8~20자, 영문+숫자+특수문자 포함 |

**Response**
```json
{
  "success": true,
  "message": "비밀번호가 변경되었습니다."
}
```

---

## 2. 프로필 (Profile)

### 2-1. 프로필 목록 조회

**GET** `/profiles`
**인증 필요:** `Authorization`

**Response**
```json
{
  "success": true,
  "data": [
    {
      "profileId": "uuid",
      "name": "프로필명",
      "avatarUrl": "https://...",
      "isAdmin": true,
      "pin": null
    }
  ]
}
```

---

### 2-2. 현재 프로필 정보 조회

**GET** `/profiles/self`
**인증 필요:** `Authorization` + `X-Profile-Token`

**Response**
```json
{
  "success": true,
  "data": {
    "profileId": "uuid",
    "name": "프로필명",
    "avatarUrl": "https://...",
    "isAdmin": false
  }
}
```

---

### 2-3. 프로필 선택 시도 (PIN 필요 여부 확인)

**POST** `/profiles/{profileId}/access`
**인증 필요:** `Authorization`

**Response**
```json
{
  "success": true,
  "data": {
    "isPasswordRequired": false,
    "profileToken": "eyJ..."
  }
}
```

> `isPasswordRequired: true`이면 PIN 입력 필요. `profileToken`은 `isPasswordRequired: false`일 때만 포함됩니다.

---

### 2-4. 프로필 PIN 검증

**POST** `/profiles/{profileId}/verify`
**인증 필요:** `Authorization`

**Request Body**
```json
{
  "pin": "1234"
}
```

**Response**
```json
{
  "success": true,
  "data": {
    "isPasswordRequired": false,
    "profileToken": "eyJ..."
  }
}
```

---

### 2-5. 프로필 생성

**POST** `/profiles`
**인증 필요:** `Authorization`

**Request Body**
```json
{
  "avatarUrl": "https://...",
  "pin": "1234",
  "nickname": "닉네임"
}
```

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| avatarUrl | String | O | 이미지 URL |
| pin | String | X | 4~6자리 숫자 |
| nickname | String | O | 2~10자 |

**Response**
```json
{
  "success": true,
  "message": "프로필이 추가되었습니다."
}
```

---

### 2-6. 프로필 수정

**PATCH** `/profiles/self`
**인증 필요:** `Authorization` + `X-Profile-Token`

**Request Body**
```json
{
  "name": "새닉네임",
  "avatarUrl": "https://...",
  "pin": "5678"
}
```

> 모든 필드 선택 사항. 변경할 필드만 전송.

**Response**
```json
{
  "success": true,
  "message": "프로필이 수정되었습니다."
}
```

---

### 2-7. 프로필 PIN 초기화

**DELETE** `/profiles/{profileId}/pin`
**인증 필요:** `Authorization`

PIN을 잊어버렸을 때 계정 비밀번호로 본인 인증 후 PIN을 제거합니다. 초기화 후 `/profiles/{profileId}/access` 호출 시 PIN 없이 바로 입장할 수 있으며, 이후 프로필 수정에서 새 PIN을 설정할 수 있습니다.

**Request Body**
```json
{
  "password": "UserPassword1!"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| password | String | O | 계정 비밀번호 (틀리면 400 반환) |

**Response**
```json
{
  "success": true,
  "message": "PIN이 초기화되었습니다."
}
```

---

### 2-8. 프로필 삭제

**DELETE** `/profiles/{profileId}`
**인증 필요:** `Authorization`

**Response**
```json
{
  "success": true,
  "message": "프로필이 삭제되었습니다."
}
```

> 관리자 프로필은 삭제 불가.

---

## 3. 애니메이션 (Animation)

### 3-1. 애니메이션 랭킹 조회

**GET** `/animations/rankings?type={rankingType}`

| Query | 타입 | 필수 | 설명 |
|-------|------|------|------|
| type | RankingType | O | `REALTIME`, `Q1`, `Q2`, `Q3`, `Q4`, `LAST_YEAR`, `ALL` |

**Response**
```json
{
  "success": true,
  "data": [
    {
      "rank": 1,
      "id": "uuid",
      "title": "애니메이션 제목",
      "thumbnailUrl": "https://...",
      "type": "TV",
      "ageRating": "15",
      "averageScore": 4.5,
      "reviewCount": 128
    }
  ]
}
```

---

### 3-2. 요일별 방영 애니메이션 조회

**GET** `/animations/weekly?airDay={airDay}`

| Query | 타입 | 필수 | 설명 |
|-------|------|------|------|
| airDay | String | X | 요일 (예: `MON`, `TUE`, ...) 미입력 시 전체 |

**Response**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "title": "애니메이션 제목",
      "thumbnailUrl": "https://...",
      "type": "TV",
      "ageRating": "ALL"
    }
  ]
}
```

---

### 3-3. 애니메이션 상세 조회

**GET** `/animations/{animationId}`

**Response**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "title": "애니메이션 제목",
    "description": "줄거리",
    "thumbnailUrl": "https://...",
    "type": "TV",
    "genres": ["액션", "판타지"],
    "ageRating": "15",
    "status": "ONGOING"
  }
}
```

---

### 3-4. 애니메이션 리뷰/평점 목록 조회

**GET** `/animations/{animationId}/ratings?page={page}&limit={limit}`

| Query | 타입 | 기본값 | 설명 |
|-------|------|--------|------|
| page | Int | 0 | 페이지 번호 |
| limit | Int | 20 | 페이지 크기 |

**인증 선택:** `Authorization` + `X-Profile-Token` (있으면 내 리뷰 포함)

**Response**
```json
{
  "success": true,
  "data": {
    "averageRating": 4.3,
    "ratingCounts": [
      { "rating": 5.0, "count": 50 },
      { "rating": 4.0, "count": 30 }
    ],
    "reviews": [
      {
        "reviewId": "uuid",
        "profileId": "uuid",
        "rating": 4.5,
        "comment": "재밌어요!",
        "createdAt": "2026-01-01T00:00:00",
        "updateAt": "2026-01-01T00:00:00",
        "profileName": "닉네임",
        "avatarURL": "https://..."
      }
    ],
    "totalCount": 128
  }
}
```

---

### 3-5. 에피소드 목록 조회

**GET** `/animations/{animationId}/episodes`
**인증 선택:** `Authorization` + `X-Profile-Token` (있으면 시청 기록 포함)

**Response**
```json
{
  "success": true,
  "data": [
    {
      "episodeId": "uuid",
      "episodeNumber": 1,
      "title": "1화 제목",
      "thumbnailUrl": "https://...",
      "description": "에피소드 설명",
      "videoUrl": "https://...",
      "duration": 1440,
      "lastWatchedSecond": 300,
      "isFinished": false
    }
  ]
}
```

---

### 3-6. 리뷰 작성

**POST** `/animations/{animationId}/ratings`
**인증 필요:** `Authorization` + `X-Profile-Token`

**Request Body**
```json
{
  "rating": 4.5,
  "comment": "재밌어요!"
}
```

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| rating | Double | O | 0.5 ~ 5.0 |
| comment | String | X | 선택 |

**Response**
```json
{
  "success": true,
  "message": "리뷰가 등록되었습니다."
}
```

---

### 3-7. 리뷰 수정

**PATCH** `/animations/{animationId}/ratings`
**인증 필요:** `Authorization` + `X-Profile-Token`

**Request Body**
```json
{
  "rating": 3.5,
  "comment": "다시 봤는데 그냥 그래요."
}
```

**Response**
```json
{
  "success": true,
  "message": "리뷰가 수정되었습니다."
}
```

---

### 3-8. 리뷰 삭제

**DELETE** `/animations/{animationId}/ratings`
**인증 필요:** `Authorization` + `X-Profile-Token`

**Response**
```json
{
  "success": true,
  "message": "리뷰가 삭제되었습니다."
}
```

---

## 4. 에피소드 댓글 (Episode Comment)

### 4-1. 댓글 작성

**POST** `/episodes/{episodeId}/comments`
**인증 필요:** `Authorization` + `X-Profile-Token`

**Request Body**
```json
{
  "content": "댓글 내용",
  "parentCommentId": null
}
```

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| content | String | O | 최대 500자 |
| parentCommentId | UUID | X | 답글 대상 댓글 ID |

**Response**
```json
{
  "success": true,
  "message": "댓글이 등록되었습니다."
}
```

---

### 4-2. 댓글 목록 조회

**GET** `/episodes/{episodeId}/comments?page={page}&limit={limit}`

| Query | 타입 | 기본값 | 설명 |
|-------|------|--------|------|
| page | Int | 0 | 페이지 번호 |
| limit | Int | 20 | 페이지 크기 |

**Response**
```json
{
  "success": true,
  "data": {
    "comments": [
      {
        "commentId": "uuid",
        "profileId": "uuid",
        "profileName": "닉네임",
        "avatarUrl": "https://...",
        "content": "댓글 내용",
        "parentCommentId": null,
        "replyCount": 3,
        "createdAt": "2026-01-01T00:00:00",
        "updatedAt": "2026-01-01T00:00:00"
      }
    ],
    "totalCount": 42
  }
}
```

---

### 4-3. 답글 목록 조회

**GET** `/episodes/{episodeId}/comments/{commentId}/replies`

**Response**
```json
{
  "success": true,
  "data": [
    {
      "commentId": "uuid",
      "profileId": "uuid",
      "profileName": "닉네임",
      "avatarUrl": "https://...",
      "content": "답글 내용",
      "parentCommentId": "uuid",
      "replyCount": 0,
      "createdAt": "2026-01-01T00:00:00",
      "updatedAt": "2026-01-01T00:00:00"
    }
  ]
}
```

---

### 4-4. 댓글 수정

**PATCH** `/episodes/{episodeId}/comments/{commentId}`
**인증 필요:** `Authorization` + `X-Profile-Token`

**Request Body**
```json
{
  "content": "수정된 댓글 내용"
}
```

**Response**
```json
{
  "success": true,
  "message": "댓글이 수정되었습니다."
}
```

---

### 4-5. 댓글 삭제

**DELETE** `/episodes/{episodeId}/comments/{commentId}`
**인증 필요:** `Authorization` + `X-Profile-Token`

**Response**
```json
{
  "success": true,
  "message": "댓글이 삭제되었습니다."
}
```

---

## 5. 광고 (Ad)

### 5-1. 활성 광고 목록 조회

**GET** `/ad`

**Response**
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "webImageURL": "https://...",
      "logoImageURL": "https://...",
      "linkURL": "https://..."
    }
  ]
}
```

---

## 6. 이미지 (Image)

### 6-1. 이미지 업로드

**POST** `/images/upload`
**Content-Type:** `multipart/form-data`

| Form | 타입 | 설명 |
|------|------|------|
| file | File | 업로드할 이미지 파일 |

**Response**
```json
{
  "success": true,
  "data": "https://업로드된-이미지-URL"
}
```

---

## API 엔드포인트 요약

| 도메인 | 메서드 | 경로 | 인증 | 설명 |
|--------|--------|------|------|------|
| Auth | POST | `/auth/check-email` | - | 이메일 중복 체크 |
| Auth | POST | `/auth/send-verification` | - | 인증 메일 발송 |
| Auth | POST | `/auth/verify-code` | - | 인증 코드 검증 |
| Auth | POST | `/auth/signup` | - | 회원가입 |
| Auth | POST | `/auth/refresh` | - | Access Token 재발급 |
| Auth | POST | `/auth/signin` | - | 로그인 |
| Auth | POST | `/auth/forgot-password` | - | 비밀번호 재설정 코드 발송 |
| Auth | POST | `/auth/reset-password` | - | 비밀번호 재설정 |
| Auth | DELETE | `/auth/account` | 유저 | 계정 삭제 |
| Auth | POST | `/auth/account/email/send-verification` | 유저 | 이메일 변경 인증 발송 |
| Auth | PATCH | `/auth/account/email` | 유저 | 이메일 변경 |
| Auth | PATCH | `/auth/account/password` | 유저 | 비밀번호 변경 |
| Profile | GET | `/profiles` | 유저 | 프로필 목록 조회 |
| Profile | GET | `/profiles/self` | 유저+프로필 | 현재 프로필 정보 조회 |
| Profile | POST | `/profiles/{profileId}/access` | 유저 | 프로필 선택 시도 |
| Profile | POST | `/profiles/{profileId}/verify` | 유저 | PIN 검증 및 토큰 발급 |
| Profile | POST | `/profiles` | 유저 | 프로필 생성 |
| Profile | PATCH | `/profiles/self` | 유저+프로필 | 프로필 수정 |
| Profile | DELETE | `/profiles/{profileId}/pin` | 유저 | 프로필 PIN 초기화 |
| Profile | DELETE | `/profiles/{profileId}` | 유저 | 프로필 삭제 |
| Animation | GET | `/animations/rankings` | - | 애니메이션 랭킹 조회 |
| Animation | GET | `/animations/weekly` | - | 요일별 방영 목록 조회 |
| Animation | GET | `/animations/{id}` | - | 애니메이션 상세 조회 |
| Animation | GET | `/animations/{id}/ratings` | 선택 | 리뷰/평점 목록 조회 |
| Animation | GET | `/animations/{id}/episodes` | 선택 | 에피소드 목록 조회 |
| Animation | POST | `/animations/{id}/ratings` | 유저+프로필 | 리뷰 작성 |
| Animation | PATCH | `/animations/{id}/ratings` | 유저+프로필 | 리뷰 수정 |
| Animation | DELETE | `/animations/{id}/ratings` | 유저+프로필 | 리뷰 삭제 |
| Comment | POST | `/episodes/{id}/comments` | 유저+프로필 | 댓글 작성 |
| Comment | GET | `/episodes/{id}/comments` | - | 댓글 목록 조회 |
| Comment | GET | `/episodes/{id}/comments/{cid}/replies` | - | 답글 목록 조회 |
| Comment | PATCH | `/episodes/{id}/comments/{cid}` | 유저+프로필 | 댓글 수정 |
| Comment | DELETE | `/episodes/{id}/comments/{cid}` | 유저+프로필 | 댓글 삭제 |
| Ad | GET | `/ad` | - | 활성 광고 목록 조회 |
| Image | POST | `/images/upload` | - | 이미지 업로드 |
