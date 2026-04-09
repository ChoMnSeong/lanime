# Lanime API 명세서

> **Base URL:** `/api/v1`  
> 모든 응답은 `ApiResponse<T>` 형태로 반환됩니다.

---

## 공통 사항

### 응답 형식

```json
{
  "success": true,
  "message": "메시지 (선택)",
  "data": {}
}
```

### 인증 헤더

| 헤더 | 설명 |
|------|------|
| `Authorization: Bearer {accessToken}` | 사용자/관리자 토큰 |
| `X-Profile-Token: {profileToken}` | 프로필 토큰 (프로필 선택 후 발급) |

표기 규칙:
- **유저** = `Authorization` 필요
- **유저+프로필** = `Authorization` + `X-Profile-Token` 필요
- **관리자** = 관리자 `Authorization` 필요
- **선택** = 있으면 추가 정보 포함, 없어도 동작

### 다국어 (i18n)

`title`, `description` 등 콘텐츠 텍스트를 다국어로 받으려면 `Accept-Language` 헤더를 전송합니다.

```
Accept-Language: ko
Accept-Language: en
Accept-Language: ja
```

헤더가 없거나 번역이 없으면 `ja` → 원본 순으로 자동 fallback합니다.  
i18n 헤더를 지원하는 엔드포인트에는 🌐 표시가 있습니다.

### 에러 코드

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
| AD101 | 404 | 해당 광고 배너를 찾을 수 없습니다. |

---

## 1. 인증 (Auth)

### 1-1. 이메일 중복 체크 / 가입 여부 확인

**POST** `/auth/check-email`

```json
{ "email": "user@example.com" }
```

```json
{
  "success": true,
  "data": {
    "email": "user@example.com",
    "isRegistered": true
  }
}
```

> `isRegistered: true` → 로그인 유도 / `false` → 회원가입(인증) 유도

---

### 1-2. 인증 메일 발송

**POST** `/auth/send-verification`

```json
{ "email": "user@example.com" }
```

```json
{ "success": true, "message": "인증 번호가 이메일로 발송되었습니다." }
```

---

### 1-3. 인증 코드 검증

**POST** `/auth/verify-code`

```json
{ "email": "user@example.com", "code": "123456" }
```

```json
{ "success": true, "data": true }
```

---

### 1-4. 회원가입

**POST** `/auth/signup`

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

```json
{ "success": true, "message": "회원가입에 성공했습니다." }
```

---

### 1-5. 로그인

**POST** `/auth/signin`

```json
{ "email": "user@example.com", "password": "Password1!" }
```

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

### 1-6. Access Token 재발급

**POST** `/auth/refresh`

```json
{ "refreshToken": "eyJ..." }
```

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

> Refresh Token이 만료되었거나 유효하지 않으면 **401** 반환.

---

### 1-7. 비밀번호 재설정 코드 발송

**POST** `/auth/forgot-password`

```json
{ "email": "user@example.com" }
```

```json
{ "success": true, "message": "비밀번호 재설정 코드가 이메일로 발송되었습니다." }
```

---

### 1-8. 비밀번호 재설정

**POST** `/auth/reset-password`

```json
{
  "email": "user@example.com",
  "token": "재설정토큰",
  "newPassword": "NewPassword1!"
}
```

```json
{ "success": true, "message": "비밀번호가 변경되었습니다." }
```

---

### 1-9. 계정 삭제

**DELETE** `/auth/account` · **인증:** 유저

```json
{ "success": true, "message": "계정이 삭제되었습니다." }
```

---

### 1-10. 이메일 변경 인증 발송

**POST** `/auth/account/email/send-verification` · **인증:** 유저

```json
{ "email": "newemail@example.com" }
```

```json
{ "success": true, "message": "인증 번호가 이메일로 발송되었습니다." }
```

---

### 1-11. 이메일 변경

**PATCH** `/auth/account/email` · **인증:** 유저

```json
{ "newEmail": "newemail@example.com", "verificationCode": "123456" }
```

```json
{ "success": true, "message": "이메일이 변경되었습니다." }
```

---

### 1-12. 비밀번호 변경

**PATCH** `/auth/account/password` · **인증:** 유저

```json
{ "currentPassword": "OldPassword1!", "newPassword": "NewPassword1!" }
```

```json
{ "success": true, "message": "비밀번호가 변경되었습니다." }
```

---

## 2. 프로필 (Profile)

### 2-1. 프로필 목록 조회

**GET** `/profiles` · **인증:** 유저

```json
{
  "success": true,
  "data": [
    {
      "profileId": "uuid",
      "name": "프로필명",
      "avatarUrl": "https://...",
      "age": 25,
      "isOwner": true
    }
  ]
}
```

---

### 2-2. 현재 프로필 정보 조회

**GET** `/profiles/self` · **인증:** 유저+프로필

```json
{
  "success": true,
  "data": {
    "profileId": "uuid",
    "name": "프로필명",
    "avatarUrl": "https://...",
    "age": 25,
    "isOwner": false
  }
}
```

---

### 2-3. 프로필 선택 시도 (PIN 필요 여부 확인)

**POST** `/profiles/{profileId}/access` · **인증:** 유저

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

**POST** `/profiles/{profileId}/verify` · **인증:** 유저

```json
{ "pin": "1234" }
```

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

**POST** `/profiles` · **인증:** 유저

```json
{ "nickname": "닉네임", "avatarUrl": "https://...", "age": 25, "pin": "1234" }
```

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| nickname | String | O | 2~10자 |
| avatarUrl | String | O | 이미지 URL |
| age | Integer | X | 1~150 |
| pin | String | X | 4~6자리 숫자 |

```json
{ "success": true, "message": "프로필이 추가되었습니다." }
```

---

### 2-6. 프로필 수정

**PATCH** `/profiles/self` · **인증:** 유저+프로필

```json
{ "name": "새닉네임", "avatarUrl": "https://...", "age": 25, "pin": "5678" }
```

> 모든 필드 선택 사항. 변경할 필드만 전송.

| 필드 | 타입 | 필수 | 제약사항 |
|------|------|------|---------|
| name | String | X | 2~10자 |
| avatarUrl | String | X | 이미지 URL |
| age | Integer | X | 1~150 |
| pin | String | X | 4~6자리 숫자 |

```json
{ "success": true, "message": "프로필이 수정되었습니다." }
```

---

### 2-7. 프로필 PIN 초기화

**DELETE** `/profiles/{profileId}/pin` · **인증:** 유저

계정 비밀번호로 본인 인증 후 PIN을 제거합니다.

```json
{ "password": "UserPassword1!" }
```

```json
{ "success": true, "message": "PIN이 초기화되었습니다." }
```

---

### 2-8. 프로필 삭제

**DELETE** `/profiles/{profileId}` · **인증:** 유저

```json
{ "success": true, "message": "프로필이 삭제되었습니다." }
```

> 대표 프로필(`isOwner: true`)은 삭제 불가.

---

## 3. 애니메이션 (Animation)

### 3-1. 애니메이션 목록 조회 / 검색 🌐

**GET** `/animations`

파라미터 없이 호출하면 전체 목록, 파라미터 포함 시 검색/필터 결과를 반환합니다.

**Query Parameters**

| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| `query` | String | - | 제목 키워드 (부분 일치, 대소문자 무시) |
| `typeIds` | List<UUID> | - | 애니메이션 타입 ID 목록 (예: `typeIds=uuid1&typeIds=uuid2`) |
| `status` | String | - | `UPCOMING` \| `ONGOING` \| `FINISHED` |
| `genreIds` | List<UUID> | - | 장르 ID 목록 (예: `genreIds=uuid1&genreIds=uuid2`) |
| `startYear` | Integer | - | 출시 연도 시작 (포함) |
| `endYear` | Integer | - | 출시 연도 끝 (포함) |
| `userAge` | Integer | - | 사용자 나이 (연령 등급별 필터링) |
| `page` | Integer | 0 | 페이지 번호 (0부터 시작) |
| `limit` | Integer | 20 | 페이지당 아이템 수 (고정값: 20) |

**페이지네이션 동작**

- 결과가 20개 미만이면 마지막 페이지입니다.
- 결과가 정확히 20개이면 다음 페이지가 존재합니다.
- 존재하지 않는 페이지를 요청하면 빈 배열을 반환합니다.

**요청 예시**

```
GET /api/v1/animations?query=강철&typeIds=uuid1&typeIds=uuid2&genreIds=uuid3&status=ONGOING&startYear=2020&endYear=2024&userAge=25&page=0&limit=20
```

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "title": "귀멸의 칼날",
      "description": "...",
      "thumbnailUrl": "https://...",
      "type": "TVA",
      "genres": ["ACTION", "FANTASY"],
      "ageRating": "15",
      "status": "FINISHED",
      "airDay": "SUNDAY",
      "releasedAt": "2019-04-06"
    },
    // ... 최대 20개 아이템
  ]
}
```

---

### 3-2. 애니메이션 타입 목록 조회

**GET** `/animations/types`

```json
{
  "success": true,
  "data": [
    { "typeId": "uuid", "name": "TVA" },
    { "typeId": "uuid", "name": "OVA" }
  ]
}
```

---

### 3-3. 장르 목록 조회

**GET** `/animations/genres`

```json
{
  "success": true,
  "data": [
    { "genreId": "uuid", "name": "ACTION" },
    { "genreId": "uuid", "name": "YURI" }
  ]
}
```

> 반환값은 i18next 키입니다. 클라이언트에서 `t('genre.ACTION')` 형태로 변환하세요. ([i18n 가이드](./i18n.md) 참고)

---

### 3-4. 애니메이션 랭킹 조회 🌐

**GET** `/animations/rankings?type={rankingType}&userAge={userAge}`

| Query | 타입 | 필수 | 값 |
|-------|------|------|----|
| `type` | RankingType | O | `REALTIME` \| `Q1` \| `Q2` \| `Q3` \| `Q4` \| `LAST_YEAR` \| `ALL` |
| `userAge` | Integer | X | 사용자 나이 (연령 등급별 필터링) |

```json
{
  "success": true,
  "data": [
    {
      "rank": 1,
      "id": "uuid",
      "title": "귀멸의 칼날",
      "thumbnailUrl": "https://...",
      "type": "TVA",
      "ageRating": "15",
      "averageScore": 4.5,
      "reviewCount": 128,
      "watchCount": 3042
    }
  ]
}
```

---

### 3-5. 요일별 방영 애니메이션 조회 🌐

**GET** `/animations/weekly`  
**GET** `/animations/weekly?airDay={airDay}&userAge={userAge}`

| Query | 설명 |
|-------|------|
| `airDay` 없음 | 전체 요일 그룹 반환 |
| `airDay=FRIDAY` | 해당 요일 목록만 반환 |
| `userAge` | 사용자 나이 (연령 등급별 필터링) |

airDay 값: `MONDAY` \| `TUESDAY` \| `WEDNESDAY` \| `THURSDAY` \| `FRIDAY` \| `SATURDAY` \| `SUNDAY`

**전체 요일 응답 (airDay 없음)**
```json
{
  "success": true,
  "data": {
    "MONDAY": [{ "id": "uuid", "title": "...", "thumbnailUrl": "...", "type": "TVA", "ageRating": "ALL", "status": "ONGOING" }],
    "TUESDAY": [],
    "WEDNESDAY": [],
    "THURSDAY": [],
    "FRIDAY": [{ "id": "uuid", "title": "...", "thumbnailUrl": "...", "type": "TVA", "ageRating": "15", "status": "ONGOING" }],
    "SATURDAY": [],
    "SUNDAY": []
  }
}
```

> 방영 애니메이션이 없는 요일은 빈 배열 반환. 요일 순서는 항상 `MONDAY → SUNDAY` 고정.

---

### 3-6. 애니메이션 상세 조회 🌐

**GET** `/animations/{animationId}`  
**인증:** 선택 (유저+프로필 포함 시 `isFavorite` 정확히 반환)

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "title": "귀멸의 칼날",
    "description": "대마살상부대...",
    "thumbnailUrl": "https://...",
    "type": "TVA",
    "genres": ["ACTION", "FANTASY"],
    "ageRating": "15",
    "status": "FINISHED",
    "isFavorite": false
  }
}
```

---

### 3-7. 에피소드 목록 조회 🌐

**GET** `/animations/{animationId}/episodes`  
**인증:** 선택 (유저+프로필 포함 시 시청 기록 포함)

```json
{
  "success": true,
  "data": [
    {
      "episodeId": "uuid",
      "episodeNumber": 1,
      "title": "잔혹",
      "thumbnailUrl": "https://...",
      "description": "에피소드 설명",
      "videoUrl": null,
      "duration": 1440,
      "hlsPath": "videos/uuid/uuid/hls/index.m3u8",
      "encodingStatus": "COMPLETED",
      "lastWatchedSecond": 300,
      "isFinished": false
    }
  ]
}
```

---

### 3-8. 리뷰/평점 목록 조회

**GET** `/animations/{animationId}/ratings?page={page}&limit={limit}`  
**인증:** 선택

| Query | 기본값 | 설명 |
|-------|--------|------|
| `page` | 0 | 페이지 번호 |
| `limit` | 20 | 페이지 크기 |

```json
{
  "success": true,
  "data": {
    "averageRating": 4.3,
    "totalCount": 128,
    "ratingCounts": [
      { "rating": 5.0, "count": 50 },
      { "rating": 4.5, "count": 30 }
    ],
    "reviews": [
      {
        "reviewId": "uuid",
        "profileId": "uuid",
        "profileName": "닉네임",
        "avatarURL": "https://...",
        "rating": 4.5,
        "comment": "재밌어요!",
        "createdAt": "2026-01-01T00:00:00",
        "updateAt": "2026-01-01T00:00:00"
      }
    ]
  }
}
```

---

### 3-9. 리뷰 작성

**POST** `/animations/{animationId}/ratings` · **인증:** 유저+프로필

```json
{ "rating": 4.5, "comment": "재밌어요!" }
```

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| rating | Double | O | 0.5 ~ 5.0 (0.5 단위) |
| comment | String | X | 선택 |

```json
{ "success": true, "message": "리뷰가 등록되었습니다." }
```

---

### 3-10. 리뷰 수정

**PATCH** `/animations/{animationId}/ratings` · **인증:** 유저+프로필

```json
{ "rating": 3.5, "comment": "다시 봤는데 그냥 그래요." }
```

```json
{ "success": true, "message": "리뷰가 수정되었습니다." }
```

---

### 3-11. 리뷰 삭제

**DELETE** `/animations/{animationId}/ratings` · **인증:** 유저+프로필

```json
{ "success": true, "message": "리뷰가 삭제되었습니다." }
```

---

### 3-12. 비슷한 애니메이션 조회 🌐

**GET** `/animations/{animationId}/similar?userAge={userAge}&matchPercentage={matchPercentage}&page={page}&limit={limit}`

| Query | 타입 | 기본값 | 설명 |
|-------|------|--------|------|
| `userAge` | Integer | - | 사용자 나이 (연령 등급별 필터링) |
| `matchPercentage` | Integer | 50 | 장르 일치도 (1~100) |
| `page` | Integer | 0 | 페이지 번호 |
| `limit` | Integer | 10 | 페이지당 아이템 수 |

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "title": "유사 애니메이션",
      "description": "...",
      "thumbnailUrl": "https://...",
      "type": "TVA",
      "genres": ["ACTION", "FANTASY"],
      "ageRating": "15",
      "status": "ONGOING"
    }
  ]
}
```

> 장르 교집합 비율로 유사도 계산. AWARD_WINNING 장르는 제외.

---

## 4. 에피소드 댓글 (Episode Comment)

### 4-1. 댓글 작성

**POST** `/episodes/{episodeId}/comments` · **인증:** 유저+프로필

```json
{ "content": "댓글 내용", "parentCommentId": null }
```

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| content | String | O | 최대 500자 |
| parentCommentId | UUID | X | 답글 대상 댓글 ID |

```json
{ "success": true, "message": "댓글이 등록되었습니다." }
```

---

### 4-2. 댓글 목록 조회

**GET** `/episodes/{episodeId}/comments?page={page}&limit={limit}`

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

**PATCH** `/episodes/{episodeId}/comments/{commentId}` · **인증:** 유저+프로필

```json
{ "content": "수정된 댓글 내용" }
```

```json
{ "success": true, "message": "댓글이 수정되었습니다." }
```

---

### 4-5. 댓글 삭제

**DELETE** `/episodes/{episodeId}/comments/{commentId}` · **인증:** 유저+프로필

```json
{ "success": true, "message": "댓글이 삭제되었습니다." }
```

---

## 5. 좋아요 (Favorite)

### 5-1. 좋아요 등록

**POST** `/animations/{animationId}/favorite` · **인증:** 유저+프로필

이미 좋아요한 경우 무시합니다.

```json
{ "success": true, "message": "좋아요가 등록되었습니다." }
```

---

### 5-2. 좋아요 취소

**DELETE** `/animations/{animationId}/favorite` · **인증:** 유저+프로필

좋아요하지 않은 경우 무시합니다.

```json
{ "success": true, "message": "좋아요가 취소되었습니다." }
```

---

### 5-3. 좋아요한 애니메이션 목록 조회

**GET** `/favorites` · **인증:** 유저+프로필

| Query | 기본값 | 설명 |
|-------|--------|------|
| `page` | 0 | 페이지 번호 |
| `limit` | 20 | 페이지당 항목 수 |

```json
{
  "success": true,
  "data": {
    "animations": [
      {
        "animationId": "uuid",
        "title": "귀멸의 칼날",
        "thumbnailUrl": "https://...",
        "type": "TVA",
        "status": "FINISHED",
        "favoritedAt": "2024-01-15T20:30:00"
      }
    ],
    "total": 10,
    "page": 0,
    "limit": 20,
    "totalPages": 1
  }
}
```

---

## 6. 시청 기록 (Watch History)

### 6-1. 시청 진행도 저장

**PUT** `/watch-history/{episodeId}` · **인증:** 유저+프로필

재생 중 주기적으로 호출하여 진행도를 저장합니다. 기록이 없으면 생성, 있으면 갱신합니다.

```json
{ "lastWatchedSecond": 720 }
```

> `isFinished`는 클라이언트가 보내지 않습니다. 서버에서 `lastWatchedSecond >= duration × 0.9` 기준으로 자동 판단합니다.

```json
{ "success": true, "message": "시청 기록이 저장되었습니다." }
```

---

### 6-2. 시청한 에피소드 목록 조회 🌐

**GET** `/watch-history` · **인증:** 유저+프로필

| Query | 기본값 | 설명 |
|-------|--------|------|
| `page` | 0 | 페이지 번호 |
| `limit` | 20 | 페이지당 항목 수 |

```json
{
  "success": true,
  "data": {
    "episodes": [
      {
        "episodeId": "uuid",
        "episodeNumber": 3,
        "title": "잔혹",
        "thumbnailUrl": "https://...",
        "duration": 1440,
        "animationId": "uuid",
        "animationTitle": "귀멸의 칼날",
        "lastWatchedSecond": 720,
        "isFinished": false,
        "watchedAt": "2024-01-15T20:30:00"
      }
    ],
    "total": 42,
    "page": 0,
    "limit": 20,
    "totalPages": 3
  }
}
```

---

## 7. 광고 (Ad)

### 7-1. 활성 광고 목록 조회

**GET** `/ad`

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

## 8. 이미지 (Image)

### 8-1. 이미지 업로드

**POST** `/images/upload` · **Content-Type:** `multipart/form-data`

| Form | 타입 | 설명 |
|------|------|------|
| file | File | 업로드할 이미지 파일 |

```json
{ "success": true, "data": "https://업로드된-이미지-URL" }
```

---

## 9. 영상 업로드 및 스트리밍 (Video / Stream)

### 9-1. 에피소드 영상 업로드

**POST** `/admin/episodes/{episodeId}/video` · **인증:** 관리자  
**Content-Type:** `multipart/form-data`

| Form | 타입 | 설명 |
|------|------|------|
| file | File | 업로드할 영상 파일 (mp4 권장) |

업로드 완료 즉시 응답하고, 서버 백그라운드에서 HLS 인코딩을 비동기로 진행합니다.

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

---

### 9-2. 인코딩 상태 조회

**GET** `/episodes/{episodeId}/encoding-status` · **인증:** 유저

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

인코딩 상태값: `PENDING` \| `ENCODING` \| `COMPLETED` \| `FAILED`

---

### 9-3. HLS 플레이리스트 조회 (스트리밍)

**GET** `/stream/{episodeId}/index.m3u8` · **인증:** 불필요  
**응답 Content-Type:** `application/vnd.apple.mpegurl`

HLS 호환 플레이어(hls.js, Video.js 등)의 소스 URL로 사용합니다.

---

### 9-4. HLS 세그먼트 파일 조회

**GET** `/stream/{episodeId}/{segment}.ts` · **인증:** 불필요  
**응답 Content-Type:** `video/mp2t`

플레이어가 자동으로 요청합니다. (직접 호출 불필요)

---

## 10. 관리자 API (Admin)

> 모든 관리자 API는 `Authorization: Bearer {adminAccessToken}` 헤더 필요  
> 관리자 Access Token은 `POST /admin/auth/signin`으로 발급

### 10-1. 관리자 로그인

**POST** `/admin/auth/signin`

```json
{ "email": "admin@example.com", "password": "password123!" }
```

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

### 10-2. 관리자 목록 조회

**GET** `/admin/accounts`

```json
{
  "success": true,
  "data": [
    { "adminId": "uuid", "email": "admin@example.com", "createdAt": "2024-01-01T00:00:00" }
  ]
}
```

---

### 10-3. 관리자 계정 생성

**POST** `/admin/accounts`

```json
{ "email": "new-admin@example.com", "password": "password123!" }
```

---

### 10-4. 관리자 계정 삭제

**DELETE** `/admin/accounts/{adminId}`

> 자기 자신은 삭제 불가.

---

### 10-5. 전체 초기화 후 벌크 임포트

**POST** `/admin/animations/import/bulk-reset?stopYear={year}`

모든 애니메이션 데이터를 삭제하고 현재 시즌부터 `stopYear`까지 역순으로 AniList + Jikan 데이터를 임포트합니다.  
작업은 백그라운드에서 실행되며 즉시 응답을 반환합니다. 진행 상황은 서버 로그에서 확인하세요.

| Query | 타입 | 필수 | 기본값 | 설명 |
|-------|------|------|--------|------|
| `stopYear` | Int | X | `2000` | 임포트를 멈출 연도 |

```json
{
  "success": true,
  "data": {
    "message": "벌크 임포트가 백그라운드에서 시작되었습니다. 서버 로그를 확인하세요.",
    "stopYear": 2000
  }
}
```

---

### 10-6. 시즌 임포트

**POST** `/admin/animations/import/season?season={season}&year={year}`

특정 시즌의 애니메이션을 AniList에서 일괄 임포트합니다. 이미 임포트된 항목은 자동 스킵.

| Query | 타입 | 필수 | 설명 |
|-------|------|------|------|
| `season` | String | X | `WINTER` \| `SPRING` \| `SUMMER` \| `FALL` (미입력 시 현재 시즌) |
| `year` | Int | X | 연도 (미입력 시 현재 연도) |

---

### 10-7. 애니메이션 생성

**POST** `/admin/animations`

```json
{
  "typeId": "uuid",
  "title": "鬼滅の刃",
  "description": "원본(ja) 설명",
  "thumbnailUrl": "https://...",
  "rating": "15",
  "status": "FINISHED",
  "airDay": "SUNDAY",
  "releasedAt": "2019-04-06",
  "genreIds": ["uuid1", "uuid2"]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| typeId | UUID | O | 애니메이션 타입 ID |
| title | String | O | 원본 제목 (자동으로 `ja` 번역으로 저장) |
| description | String | X | 원본 설명 |
| thumbnailUrl | String | X | 썸네일 URL |
| rating | String | X | `ALL` \| `12` \| `15` \| `19` |
| status | String | O | `UPCOMING` \| `ONGOING` \| `FINISHED` |
| airDay | String | X | `MONDAY` ~ `SUNDAY` |
| releasedAt | Date | X | 방영 시작일 |
| genreIds | UUID[] | X | 장르 ID 목록 |

---

### 10-8. 애니메이션 수정

**PATCH** `/admin/animations/{animationId}`

모든 필드 optional. 포함된 필드만 수정됩니다.  
`genreIds` 포함 시 기존 장르 전체 교체, 빈 배열이면 장르 전체 제거.

---

### 10-9. 애니메이션 삭제

**DELETE** `/admin/animations/{animationId}`

```json
{ "success": true, "message": "애니메이션이 삭제되었습니다." }
```

---

### 10-10. 애니메이션 번역 등록/수정

**PUT** `/admin/animations/{animationId}/translations/{locale}`

locale 예시: `ko`, `en`, `ja`, `zh`

```json
{ "title": "귀멸의 칼날", "description": "대마살상부대 소속 탄지로의 이야기..." }
```

| 필드 | 타입 | 필수 |
|------|------|------|
| title | String | O |
| description | String | X |

```json
{ "success": true, "message": "번역이 저장되었습니다." }
```

> 이미 해당 locale 번역이 있으면 덮어씁니다 (upsert).

---

### 10-11. 애니메이션 타입 생성

**POST** `/admin/animations/types`

```json
{ "name": "TVA" }
```

> name은 영문 대문자 키로 등록 (i18n 키로 사용됨)

---

### 10-10. 장르 생성

**POST** `/admin/animations/genres`

```json
{ "name": "ACTION" }
```

> name은 영문 대문자 키로 등록. 등록 후 클라이언트 i18n 파일에 번역 추가 필요. ([i18n 가이드](./i18n.md) 참고)

---

### 10-11. 에피소드 생성

**POST** `/admin/animations/{animationId}/episodes`

```json
{
  "episodeNumber": 1,
  "title": "残酷",
  "thumbnailUrl": "https://...",
  "description": "에피소드 설명",
  "duration": 1440
}
```

> title은 자동으로 `ja` 번역으로 저장됩니다.

---

### 10-12. 에피소드 수정

**PATCH** `/admin/episodes/{episodeId}`

모든 필드 optional.

---

### 10-13. 에피소드 삭제

**DELETE** `/admin/episodes/{episodeId}`

```json
{ "success": true, "message": "에피소드가 삭제되었습니다." }
```

---

### 10-14. 에피소드 번역 등록/수정

**PUT** `/admin/episodes/{episodeId}/translations/{locale}`

```json
{ "title": "잔혹", "description": "..." }
```

```json
{ "success": true, "message": "번역이 저장되었습니다." }
```

---

### 10-15. 에피소드 썸네일 Jikan 동기화

**PATCH** `/admin/animations/{animationId}/episodes/thumbnails`

특정 애니메이션의 모든 에피소드 썸네일을 Jikan API에서 자동으로 조회 및 업데이트합니다.

**조건**
- 애니메이션에 MAL ID가 있어야 함 (임포트된 애니메이션)
- Jikan API 속도 제한 적용 (1.5req/s)

**응답**

```json
{
  "success": true,
  "message": "에피소드 썸네일 24개를 업데이트했습니다."
}
```

---

### 10-15. 광고 배너 목록 조회 (전체)

**GET** `/admin/banners`

활성/비활성 포함 전체 배너를 반환합니다.

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

### 10-16. 광고 배너 생성

**POST** `/admin/banners`

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

| 필드 | 타입 | 필수 |
|------|------|------|
| title | String | O |
| imageUrl | String | O |
| logoImageUrl | String | X |
| startAt | DateTime | X |
| endAt | DateTime | X |
| isActive | Boolean | X (기본: true) |

---

### 10-17. 광고 배너 수정

**PATCH** `/admin/banners/{bannerId}`

모든 필드 optional. 포함된 필드만 수정됩니다.

---

### 10-18. 광고 배너 삭제

**DELETE** `/admin/banners/{bannerId}`

```json
{ "success": true, "message": "광고 배너가 삭제되었습니다." }
```

---

## 엔드포인트 요약

| 도메인 | 메서드 | 경로 | 인증 | 설명 |
|--------|--------|------|------|------|
| Auth | POST | `/auth/check-email` | - | 이메일 가입 여부 확인 |
| Auth | POST | `/auth/send-verification` | - | 인증 메일 발송 |
| Auth | POST | `/auth/verify-code` | - | 인증 코드 검증 |
| Auth | POST | `/auth/signup` | - | 회원가입 |
| Auth | POST | `/auth/signin` | - | 로그인 |
| Auth | POST | `/auth/refresh` | - | Access Token 재발급 |
| Auth | POST | `/auth/forgot-password` | - | 비밀번호 재설정 코드 발송 |
| Auth | POST | `/auth/reset-password` | - | 비밀번호 재설정 |
| Auth | DELETE | `/auth/account` | 유저 | 계정 삭제 |
| Auth | POST | `/auth/account/email/send-verification` | 유저 | 이메일 변경 인증 발송 |
| Auth | PATCH | `/auth/account/email` | 유저 | 이메일 변경 |
| Auth | PATCH | `/auth/account/password` | 유저 | 비밀번호 변경 |
| Profile | GET | `/profiles` | 유저 | 프로필 목록 조회 |
| Profile | POST | `/profiles` | 유저 | 프로필 생성 |
| Profile | GET | `/profiles/self` | 유저+프로필 | 현재 프로필 정보 조회 |
| Profile | PATCH | `/profiles/self` | 유저+프로필 | 프로필 수정 |
| Profile | POST | `/profiles/{id}/access` | 유저 | 프로필 선택 시도 |
| Profile | POST | `/profiles/{id}/verify` | 유저 | PIN 검증 및 토큰 발급 |
| Profile | DELETE | `/profiles/{id}/pin` | 유저 | PIN 초기화 |
| Profile | DELETE | `/profiles/{id}` | 유저 | 프로필 삭제 |
| Animation | GET | `/animations` | - | 목록 조회 / 검색 🌐 |
| Animation | GET | `/animations/types` | - | 타입 목록 조회 |
| Animation | GET | `/animations/genres` | - | 장르 목록 조회 |
| Animation | GET | `/animations/rankings` | - | 랭킹 조회 🌐 |
| Animation | GET | `/animations/weekly` | - | 요일별 방영 목록 🌐 |
| Animation | GET | `/animations/{id}` | 선택 | 상세 조회 🌐 |
| Animation | GET | `/animations/{id}/episodes` | 선택 | 에피소드 목록 🌐 |
| Animation | GET | `/animations/{id}/ratings` | 선택 | 리뷰/평점 목록 |
| Animation | POST | `/animations/{id}/ratings` | 유저+프로필 | 리뷰 작성 |
| Animation | PATCH | `/animations/{id}/ratings` | 유저+프로필 | 리뷰 수정 |
| Animation | DELETE | `/animations/{id}/ratings` | 유저+프로필 | 리뷰 삭제 |
| Animation | GET | `/animations/{id}/similar` | - | 비슷한 애니메이션 🌐 |
| Comment | POST | `/episodes/{id}/comments` | 유저+프로필 | 댓글 작성 |
| Comment | GET | `/episodes/{id}/comments` | - | 댓글 목록 |
| Comment | GET | `/episodes/{id}/comments/{cid}/replies` | - | 답글 목록 |
| Comment | PATCH | `/episodes/{id}/comments/{cid}` | 유저+프로필 | 댓글 수정 |
| Comment | DELETE | `/episodes/{id}/comments/{cid}` | 유저+프로필 | 댓글 삭제 |
| Favorite | POST | `/animations/{id}/favorite` | 유저+프로필 | 좋아요 등록 |
| Favorite | DELETE | `/animations/{id}/favorite` | 유저+프로필 | 좋아요 취소 |
| Favorite | GET | `/favorites` | 유저+프로필 | 좋아요 목록 |
| Watch History | PUT | `/watch-history/{episodeId}` | 유저+프로필 | 시청 진행도 저장 |
| Watch History | GET | `/watch-history` | 유저+프로필 | 시청 목록 🌐 |
| Ad | GET | `/ad` | - | 활성 광고 목록 |
| Image | POST | `/images/upload` | - | 이미지 업로드 |
| Video | POST | `/admin/episodes/{id}/video` | 관리자 | 영상 업로드 |
| Video | GET | `/episodes/{id}/encoding-status` | 유저 | 인코딩 상태 조회 |
| Stream | GET | `/stream/{id}/index.m3u8` | - | HLS 플레이리스트 |
| Stream | GET | `/stream/{id}/{segment}.ts` | - | HLS 세그먼트 |
| Admin | POST | `/admin/auth/signin` | - | 관리자 로그인 |
| Admin | GET | `/admin/accounts` | 관리자 | 관리자 목록 |
| Admin | POST | `/admin/accounts` | 관리자 | 관리자 생성 |
| Admin | DELETE | `/admin/accounts/{id}` | 관리자 | 관리자 삭제 |
| Admin | POST | `/admin/animations` | 관리자 | 애니메이션 생성 |
| Admin | PATCH | `/admin/animations/{id}` | 관리자 | 애니메이션 수정 |
| Admin | DELETE | `/admin/animations/{id}` | 관리자 | 애니메이션 삭제 |
| Admin | PUT | `/admin/animations/{id}/translations/{locale}` | 관리자 | 애니메이션 번역 등록/수정 |
| Admin | POST | `/admin/animations/types` | 관리자 | 타입 생성 |
| Admin | POST | `/admin/animations/genres` | 관리자 | 장르 생성 |
| Admin | POST | `/admin/animations/{id}/episodes` | 관리자 | 에피소드 생성 |
| Admin | PATCH | `/admin/episodes/{id}` | 관리자 | 에피소드 수정 |
| Admin | DELETE | `/admin/episodes/{id}` | 관리자 | 에피소드 삭제 |
| Admin | PUT | `/admin/episodes/{id}/translations/{locale}` | 관리자 | 에피소드 번역 등록/수정 |
| Admin | GET | `/admin/banners` | 관리자 | 배너 전체 목록 |
| Admin | POST | `/admin/banners` | 관리자 | 배너 생성 |
| Admin | PATCH | `/admin/banners/{id}` | 관리자 | 배너 수정 |
| Admin | DELETE | `/admin/banners/{id}` | 관리자 | 배너 삭제 |
