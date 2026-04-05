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
| E001 | 404 | 해당 에피소드를 찾을 수 없습니다. |
| E002 | 409 | 이미 인코딩이 진행 중입니다. |
| E003 | 404 | 스트리밍 가능한 영상이 없습니다. |
| E005 | 500 | 영상 업로드에 실패했습니다. |
| E007 | 403 | 관리자만 접근 가능합니다. |

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

### 3-2. 요일별 방영 애니메이션 전체 조회

**GET** `/animations/weekly`

모든 요일의 방영 애니메이션을 요일별로 그룹핑하여 한번에 반환합니다.

**Response**
```json
{
  "success": true,
  "data": {
    "MON": [{ "id": "uuid", "title": "...", "thumbnailUrl": "...", "type": "TV", "ageRating": "ALL" }],
    "TUE": [],
    "WED": [{ "id": "uuid", "title": "...", "thumbnailUrl": "...", "type": "TV", "ageRating": "15" }],
    "THU": [],
    "FRI": [],
    "SAT": [],
    "SUN": []
  }
}
```

> 방영 애니메이션이 없는 요일은 빈 배열(`[]`)로 반환됩니다. 요일 순서는 항상 `MON → SUN` 순으로 고정됩니다.

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
      "logoImageURL": "https://..."
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

## 7. 관리자 API (Admin)

> 모든 관리자 API는 `Authorization: Bearer {adminAccessToken}` 헤더 필요  
> 관리자 Access Token은 `/admin/auth/signin`으로 발급 (type 클레임 = "admin")

### 7-1. 관리자 로그인

**POST** `/admin/auth/signin`

**Request Body**
```json
{
  "email": "admin@example.com",
  "password": "password123!"
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

---

### 7-2. 관리자 목록 조회

**GET** `/admin/accounts`

**Response**
```json
{
  "success": true,
  "data": [
    { "adminId": "uuid", "email": "admin@example.com", "createdAt": "2024-01-01T00:00:00" }
  ]
}
```

---

### 7-3. 관리자 계정 생성

**POST** `/admin/accounts`

**Request Body**
```json
{ "email": "new-admin@example.com", "password": "password123!" }
```

---

### 7-4. 관리자 계정 삭제

**DELETE** `/admin/accounts/{adminId}`

> 자기 자신은 삭제 불가

---

### 7-5. 애니메이션 생성

**POST** `/admin/animations`

**Request Body**
```json
{
  "typeId": "uuid",
  "title": "애니메이션 제목",
  "description": "설명",
  "thumbnailUrl": "https://...",
  "rating": "ALL",
  "status": "ONGOING",
  "airDay": "Monday",
  "releasedAt": "2024-01-01",
  "genreIds": ["uuid1", "uuid2"]
}
```

---

### 7-6. 애니메이션 수정

**PATCH** `/admin/animations/{animationId}`

> 모든 필드 optional. 포함된 필드만 수정. `genreIds` 포함 시 기존 장르 전체 교체.

---

### 7-7. 애니메이션 삭제

**DELETE** `/admin/animations/{animationId}`

---

### 7-8. 에피소드 생성

**POST** `/admin/animations/{animationId}/episodes`

**Request Body**
```json
{
  "episodeNumber": 1,
  "title": "1화 제목",
  "thumbnailUrl": "https://...",
  "description": "에피소드 설명",
  "duration": 1440
}
```

---

### 7-9. 에피소드 수정

**PATCH** `/admin/episodes/{episodeId}`

---

### 7-10. 에피소드 삭제

**DELETE** `/admin/episodes/{episodeId}`

---

### 7-11. 광고 배너 목록 조회 (전체)

**GET** `/admin/banners`

활성/비활성 포함 전체 배너 목록을 반환합니다.

**Response**
```json
{
  "success": true,
  "data": [
    {
      "adBannerId": "uuid",
      "title": "배너 제목",
      "imageUrl": "https://...",
      "logoImageUrl": "https://...",
      "startAt": "2026-01-01T00:00:00",
      "endAt": "2026-12-31T23:59:59",
      "isActive": true
    }
  ]
}
```

---

### 7-12. 광고 배너 생성

**POST** `/admin/banners`

**Request Body**
```json
{
  "title": "배너 제목",
  "imageUrl": "https://...",
  "logoImageUrl": "https://...",
  "startAt": "2026-01-01T00:00:00",
  "endAt": "2026-12-31T23:59:59",
  "isActive": true
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| title | String | O | 배너 제목 |
| imageUrl | String | O | 배너 이미지 URL |
| logoImageUrl | String | X | 로고 이미지 URL |
| startAt | LocalDateTime | X | 노출 시작 일시 |
| endAt | LocalDateTime | X | 노출 종료 일시 |
| isActive | Boolean | X | 활성 여부 (기본값: true) |

---

### 7-13. 광고 배너 수정

**PATCH** `/admin/banners/{bannerId}`

> 모든 필드 optional. 포함된 필드만 수정.

**Request Body**
```json
{
  "title": "수정된 제목",
  "isActive": false
}
```

**에러 코드**

| 코드 | HTTP | 설명 |
|------|------|------|
| AD101 | 404 | 해당 광고 배너를 찾을 수 없습니다. |

---

### 7-14. 광고 배너 삭제

**DELETE** `/admin/banners/{bannerId}`

**Response**
```json
{
  "success": true,
  "message": "광고 배너가 삭제되었습니다."
}
```

**에러 코드**

| 코드 | HTTP | 설명 |
|------|------|------|
| AD101 | 404 | 해당 광고 배너를 찾을 수 없습니다. |

---

## 8. 영상 업로드 및 스트리밍 (Video / Stream)

### 8-1. 에피소드 영상 업로드 (관리자 전용)

**POST** `/admin/episodes/{episodeId}/video`
**Content-Type:** `multipart/form-data`
**인증:** `Authorization: Bearer {adminAccessToken}`

| Form | 타입 | 설명 |
|------|------|------|
| file | File | 업로드할 영상 파일 (mp4 권장) |

업로드 완료 즉시 응답을 반환하고, 서버 백그라운드에서 HLS 인코딩을 비동기로 진행합니다.

**Response**
```json
{
  "success": true,
  "data": {
    "episodeId": "uuid",
    "jobId": "uuid",
    "encodingStatus": "PENDING",
    "message": "영상 업로드가 완료되었습니다. 백그라운드에서 HLS 인코딩을 시작합니다."
  }
}
```

**에러 코드**

| 코드 | HTTP | 설명 |
|------|------|------|
| E001 | 404 | 해당 에피소드를 찾을 수 없습니다. |
| E002 | 409 | 이미 인코딩이 진행 중입니다. |
| E007 | 403 | 관리자만 접근 가능합니다. |

---

### 7-2. 인코딩 상태 조회

**GET** `/episodes/{episodeId}/encoding-status`
**인증:** `Authorization: Bearer {accessToken}`

**Response**
```json
{
  "success": true,
  "data": {
    "episodeId": "uuid",
    "jobId": "uuid",
    "status": "ENCODING",
    "errorMessage": null,
    "startedAt": "2024-01-01T12:00:00",
    "completedAt": null
  }
}
```

인코딩 상태값: `PENDING` | `ENCODING` | `COMPLETED` | `FAILED`

---

### 7-3. HLS 플레이리스트 조회 (스트리밍)

**GET** `/stream/{episodeId}/index.m3u8`
**인증:** 불필요
**Content-Type 응답:** `application/vnd.apple.mpegurl`

HLS 호환 비디오 플레이어(hls.js, Video.js 등)의 소스 URL로 사용합니다.

**에러 코드**

| 코드 | HTTP | 설명 |
|------|------|------|
| E001 | 404 | 해당 에피소드를 찾을 수 없습니다. |
| E003 | 404 | 스트리밍 가능한 영상이 없습니다. (인코딩 미완료 또는 파일 없음) |

---

### 7-4. HLS 세그먼트 파일 조회

**GET** `/stream/{episodeId}/{segment}.ts`
**인증:** 불필요
**Content-Type 응답:** `video/mp2t`

플레이어가 자동으로 요청합니다. (직접 호출 불필요)

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
| Admin | POST | `/admin/auth/signin` | - | 관리자 로그인 |
| Admin | GET | `/admin/accounts` | 관리자 | 관리자 목록 조회 |
| Admin | POST | `/admin/accounts` | 관리자 | 관리자 생성 |
| Admin | DELETE | `/admin/accounts/{id}` | 관리자 | 관리자 삭제 |
| Admin | POST | `/admin/animations` | 관리자 | 애니메이션 생성 |
| Admin | PATCH | `/admin/animations/{id}` | 관리자 | 애니메이션 수정 |
| Admin | DELETE | `/admin/animations/{id}` | 관리자 | 애니메이션 삭제 |
| Admin | POST | `/admin/animations/{id}/episodes` | 관리자 | 에피소드 생성 |
| Admin | PATCH | `/admin/episodes/{id}` | 관리자 | 에피소드 수정 |
| Admin | DELETE | `/admin/episodes/{id}` | 관리자 | 에피소드 삭제 |
| Admin | GET | `/admin/banners` | 관리자 | 광고 배너 전체 목록 조회 |
| Admin | POST | `/admin/banners` | 관리자 | 광고 배너 생성 |
| Admin | PATCH | `/admin/banners/{id}` | 관리자 | 광고 배너 수정 |
| Admin | DELETE | `/admin/banners/{id}` | 관리자 | 광고 배너 삭제 |
| Video | POST | `/admin/episodes/{id}/video` | 관리자 | 에피소드 영상 업로드 |
| Video | GET | `/admin/episodes/{id}/encoding-status` | 관리자 | 인코딩 상태 조회 |
| Stream | GET | `/stream/{id}/index.m3u8` | - | HLS 플레이리스트 조회 |
| Stream | GET | `/stream/{id}/{segment}.ts` | - | HLS 세그먼트 조회 |
| Image | POST | `/images/upload` | - | 이미지 업로드 |
