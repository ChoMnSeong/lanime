---
-- 1. 기존 테이블 삭제 (외래키 제약 조건을 고려하여 역순 삭제)
---
DROP TABLE IF EXISTS ad_banner CASCADE;
DROP TABLE IF EXISTS animation_comment CASCADE;
DROP TABLE IF EXISTS user_watch_history CASCADE;
DROP TABLE IF EXISTS user_animation_favorite CASCADE;
DROP TABLE IF EXISTS episode CASCADE;
DROP TABLE IF EXISTS animation_genre CASCADE;
DROP TABLE IF EXISTS animation CASCADE;
DROP TABLE IF EXISTS user_profile CASCADE;
DROP TABLE IF EXISTS user_device CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;
DROP TABLE IF EXISTS genre CASCADE;
DROP TABLE IF EXISTS animation_type CASCADE;

---
-- 2. 필수 확장 모듈 설치
---
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

---
-- 3. 기초 공통 테이블
---
-- 애니메이션 타입 (TVA, 극장판, OVA 등)
CREATE TABLE animation_type (
    type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 장르 (액션, 로맨스, 판타지 등)
CREATE TABLE genre (
    genre_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

---
-- 4. 사용자 관련 테이블
---
-- 계정 정보 ("user"는 예약어라 큰따옴표 필수)
CREATE TABLE "user" (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 기기 정보 (FCM 토큰 등)
CREATE TABLE user_device (
    device_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    device_token TEXT,
    device_type VARCHAR(20) NOT NULL, -- WEB, IOS, ANDROID
    last_logged_in_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 프로필 (닉네임, 아바타 등)
CREATE TABLE user_profile (
    profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    pin VARCHAR(255),
    avatar_url TEXT,
    is_admin BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

---
-- 5. 애니메이션 콘텐츠 테이블
---
-- 애니메이션 메인 정보
CREATE TABLE animation (
    animation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_id UUID NOT NULL REFERENCES animation_type(type_id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    rating VARCHAR(20), -- ALL, 15, 19
    status VARCHAR(20), -- ONGOING, FINISHED
    released_at DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 애니메이션-장르 매핑 (다대다 관계 해소)
CREATE TABLE animation_genre (
    animation_genre_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animation_id UUID NOT NULL REFERENCES animation(animation_id) ON DELETE CASCADE,
    genre_id UUID NOT NULL REFERENCES genre(genre_id) ON DELETE CASCADE
);

-- 개별 에피소드 정보
CREATE TABLE episode (
    episode_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animation_id UUID NOT NULL REFERENCES animation(animation_id) ON DELETE CASCADE,
    episode_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    video_url TEXT NOT NULL,
    duration INT, -- 재생 시간(초)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

---
-- 6. 사용자 활동 기록 테이블
---
-- 즐겨찾기
CREATE TABLE user_animation_favorite (
    favorite_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES user_profile(profile_id) ON DELETE CASCADE,
    animation_id UUID NOT NULL REFERENCES animation(animation_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 시청 기록
CREATE TABLE user_watch_history (
    history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES user_profile(profile_id) ON DELETE CASCADE,
    episode_id UUID NOT NULL REFERENCES episode(episode_id) ON DELETE CASCADE,
    last_watched_second INT DEFAULT 0,
    is_finished BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 댓글 (대댓글 지원)
CREATE TABLE animation_comment (
    comment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    episode_id UUID NOT NULL REFERENCES episode(episode_id) ON DELETE CASCADE,
    profile_id UUID NOT NULL REFERENCES user_profile(profile_id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    parent_comment_id UUID REFERENCES animation_comment(comment_id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

---
-- 7. 기타 테이블
---
-- 광고 배너
CREATE TABLE ad_banner (
    ad_banner_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    image_url TEXT NOT NULL,
    link_url TEXT,
    position VARCHAR(50), -- MAIN_TOP, SIDEBAR 등
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);