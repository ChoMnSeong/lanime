# 어드민 플로우 가이드

어드민 프론트엔드 개발자를 위한 API 호출 순서 및 i18n 처리 가이드입니다.

---

## 1. i18n 전략 개요

lanime은 두 가지 i18n 방식을 혼합하여 사용합니다.

| 대상 | 방식 | 저장 위치 |
|------|------|-----------|
| 애니메이션 제목 / 설명 | **번역 테이블** | `animation_translation` |
| 에피소드 제목 / 설명 | **번역 테이블** | `episode_translation` |
| 장르 이름 | **키 기반** | 클라이언트 i18n 파일 (서버는 영문 대문자 키 저장) |
| 애니메이션 타입 이름 | **키 기반** | 클라이언트 i18n 파일 |
| 상태(status), 방영 요일(airDay) | **키 기반** | 클라이언트 i18n 파일 |

### 번역 테이블 폴백 규칙
클라이언트가 `Accept-Language: ko`를 보내면 서버는 아래 순서로 폴백합니다.

```
ko 번역 → ja 번역 → 원본 컬럼
```

번역이 없는 언어를 요청해도 항상 값이 반환됩니다.

---

## 2. 장르 / 타입 관리

### 등록 규칙
- 이름은 **영문 대문자** + 언더스코어 조합으로 저장됩니다.
- 서버가 자동 정규화(`uppercase().trim()`)하므로 소문자로 전송해도 됩니다.
- 예: `"action"` → DB에 `"ACTION"` 저장

### 장르 등록
```
POST /api/v1/admin/animations/genres
{ "name": "ACTION" }
```

### 타입 등록
```
POST /api/v1/admin/animations/types
{ "name": "TVA" }
```

### 클라이언트 i18n 파일 추가 절차
새 장르/타입을 등록할 때 클라이언트 측 번역 파일에도 키를 추가해야 합니다.

```json
// ko.json 예시
{
  "genre": {
    "ACTION": "액션",
    "ROMANCE": "로맨스",
    "FANTASY": "판타지"
  },
  "type": {
    "TVA": "TV 애니메이션",
    "MOVIE": "극장판",
    "OVA": "OVA",
    "ONA": "ONA",
    "SPECIAL": "스페셜"
  },
  "status": {
    "ONGOING": "방영 중",
    "FINISHED": "완결",
    "UPCOMING": "방영 예정"
  },
  "airDay": {
    "MONDAY": "월요일",
    "TUESDAY": "화요일",
    "WEDNESDAY": "수요일",
    "THURSDAY": "목요일",
    "FRIDAY": "금요일",
    "SATURDAY": "토요일",
    "SUNDAY": "일요일"
  }
}
```

---

## 3. 애니메이션 등록 플로우

### 방법 A — MAL 임포트 (권장)
MyAnimeList ID를 알면 AniList + Jikan에서 데이터를 자동으로 가져옵니다.

```
POST /api/v1/admin/animations/import
{ "malId": 21 }
```

- 제목(ja/en), 설명, 장르, 에피소드, 방영 정보가 한 번에 저장됩니다.
- 이미 임포트된 MAL ID를 재전송하면 에러가 반환됩니다.
- 에피소드 개수가 많으면 Jikan 레이트 리밋으로 인해 수 초 소요될 수 있습니다.

### 방법 B — 수동 등록

**① 애니메이션 생성**
```
POST /api/v1/admin/animations
{
  "typeId": "uuid",
  "title": "진격의 거인",          // ja 원제
  "description": "설명 텍스트",
  "thumbnailUrl": "https://...",
  "rating": "15",
  "status": "FINISHED",
  "airDay": "SUNDAY",
  "releasedAt": "2013-04-07",
  "genreIds": ["uuid1", "uuid2"]
}
```
- 저장 시 `title`과 `description`이 자동으로 `ja` 번역 테이블에도 저장됩니다.

**② 추가 언어 번역 등록** (선택)
```
PUT /api/v1/admin/animations/{animationId}/translations/ko
{
  "title": "진격의 거인",
  "description": "한국어 설명"
}
```
```
PUT /api/v1/admin/animations/{animationId}/translations/en
{
  "title": "Attack on Titan",
  "description": "English description"
}
```

---

## 4. 에피소드 등록 플로우

MAL 임포트를 사용하면 에피소드도 자동으로 저장됩니다. 수동 등록이 필요한 경우:

**① 에피소드 생성**
```
POST /api/v1/admin/animations/{animationId}/episodes
{
  "episodeNumber": 1,
  "title": "二千年後の君へ",         // ja 원제
  "thumbnailUrl": "https://...",
  "description": "에피소드 설명",
  "duration": 1440
}
```
- `duration`은 초 단위입니다.
- 저장 시 `title`과 `description`이 자동으로 `ja` 번역 테이블에도 저장됩니다.

**② 추가 언어 번역 등록** (선택)
```
PUT /api/v1/admin/episodes/{episodeId}/translations/ko
{
  "title": "2000년 후의 너에게",
  "description": "한국어 설명"
}
```

---

## 5. MAL 시즌 임포트

현재 시즌 또는 특정 시즌의 애니메이션을 일괄 임포트합니다.

```
POST /api/v1/admin/animations/import/season
```

특정 시즌을 지정하려면 쿼리 파라미터를 사용합니다.

```
POST /api/v1/admin/animations/import/season?season=WINTER&year=2025
```

- `season`: `WINTER` / `SPRING` / `SUMMER` / `FALL`
- 이미 임포트된 애니메이션은 자동으로 스킵됩니다.
- MAL ID가 없는 항목도 스킵됩니다.
- 스케줄러가 매 분기 첫날 오전 3시에 자동 실행되므로 수동 호출은 보통 불필요합니다.

---

## 6. 번역 수정 플로우

### 애니메이션 제목/설명 수정
`PATCH /api/v1/admin/animations/{animationId}`에서 `title` 또는 `description`을 포함하면 `ja` 번역도 자동 갱신됩니다.

특정 언어만 수정하려면 번역 API를 직접 호출합니다.
```
PUT /api/v1/admin/animations/{animationId}/translations/{locale}
{ "title": "수정된 제목", "description": "수정된 설명" }
```

### 에피소드 제목/설명 수정
`PATCH /api/v1/admin/episodes/{episodeId}`에서 `title` 또는 `description`을 포함하면 `ja` 번역도 자동 갱신됩니다.

특정 언어만 수정하려면:
```
PUT /api/v1/admin/episodes/{episodeId}/translations/{locale}
{ "title": "수정된 에피소드 제목", "description": "수정된 설명" }
```

### 번역 Upsert 동작
- 해당 `(id, locale)` 조합이 없으면 **INSERT**
- 이미 존재하면 **UPDATE**
- `description`은 `null`을 전송하면 기존 값이 `null`로 덮어쓰입니다. 변경하지 않으려면 필드를 아예 포함하지 마세요.

---

## 7. 자주 묻는 질문

**Q. 어떤 locale 값을 사용해야 하나요?**  
ISO 639-1 소문자 2자리 코드를 사용합니다. 예: `ja`, `ko`, `en`

**Q. `ja` 번역이 없으면 어떻게 되나요?**  
서버는 원본 컬럼(`animation.title`)을 폴백으로 사용합니다. 항상 값이 반환됩니다.

**Q. 장르 이름에 한국어를 저장할 수 있나요?**  
서버에는 영문 대문자 키만 저장합니다. 한국어 번역은 클라이언트 i18n 파일에서 처리합니다.

**Q. 에피소드 비디오 URL은 어떻게 등록하나요?**  
현재 에피소드 생성/수정 API의 `videoUrl` 필드는 대용량 업로드 파이프라인과 연동됩니다. 직접 URL을 지정하는 경우 `PATCH /api/v1/admin/episodes/{episodeId}`의 `videoUrl` 필드를 사용하세요.
