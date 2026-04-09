---
-- 필수 확장 모듈 설치
---
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

---
-- 기초 공통 테이블
---
-- 서버 관리자 (user와 완전히 독립된 별도 엔티티)
CREATE TABLE IF NOT EXISTS admin (
    admin_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email      VARCHAR(100) UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 애니메이션 타입 (TVA, 극장판, OVA 등)
CREATE TABLE IF NOT EXISTS animation_type (
    type_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 장르 (액션, 로맨스, 판타지 등)
CREATE TABLE IF NOT EXISTS genre (
    genre_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

---
-- 사용자 관련 테이블
---
-- 계정 정보 ("user"는 예약어라 큰따옴표 필수)
CREATE TABLE IF NOT EXISTS "user" (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 기기 정보 (FCM 토큰 등)
CREATE TABLE IF NOT EXISTS user_device (
    device_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    device_token TEXT,
    device_type VARCHAR(20) NOT NULL, -- WEB, IOS, ANDROID
    last_logged_in_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 프로필 (닉네임, 아바타 등)
CREATE TABLE IF NOT EXISTS user_profile (
    profile_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    pin VARCHAR(255),
    avatar_url TEXT,
    is_owner BOOLEAN DEFAULT false, -- 대표 프로필 여부 (삭제 불가)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

---
-- 애니메이션 콘텐츠 테이블
---
-- 애니메이션 메인 정보
CREATE TABLE IF NOT EXISTS animation (
    animation_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type_id UUID NOT NULL REFERENCES animation_type(type_id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    thumbnail_url TEXT,
    rating VARCHAR(20), -- ALL, 15, 19
    status VARCHAR(20), -- ONGOING, FINISHED
    air_day VARCHAR(15), -- SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY
    released_at DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 애니메이션-장르 매핑 (다대다 관계 해소)
CREATE TABLE IF NOT EXISTS animation_genre (
    animation_genre_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animation_id UUID NOT NULL REFERENCES animation(animation_id) ON DELETE CASCADE,
    genre_id UUID NOT NULL REFERENCES genre(genre_id) ON DELETE CASCADE
);

-- 개별 에피소드 정보
CREATE TABLE IF NOT EXISTS episode (
    episode_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animation_id UUID NOT NULL REFERENCES animation(animation_id) ON DELETE CASCADE,
    episode_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    thumbnail_url TEXT,
    description TEXT,
    video_url TEXT,
    duration INT, -- 재생 시간(초)
    hls_path TEXT, -- HLS 플레이리스트 상대 경로 (videos/{animId}/{epId}/hls/index.m3u8)
    encoding_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, ENCODING, COMPLETED, FAILED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 영상 인코딩 작업 이력
CREATE TABLE IF NOT EXISTS video_encoding_job (
    job_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    episode_id UUID NOT NULL REFERENCES episode(episode_id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, ENCODING, COMPLETED, FAILED
    input_path TEXT,
    output_path TEXT,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 에피소드당 동시에 ENCODING 상태인 job은 1개만 허용
CREATE UNIQUE INDEX IF NOT EXISTS idx_encoding_job_episode_active
ON video_encoding_job (episode_id) WHERE status = 'ENCODING';

---
-- 사용자 활동 기록 테이블
---
-- 즐겨찾기
CREATE TABLE IF NOT EXISTS user_animation_favorite (
    favorite_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES user_profile(profile_id) ON DELETE CASCADE,
    animation_id UUID NOT NULL REFERENCES animation(animation_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 시청 기록
CREATE TABLE IF NOT EXISTS user_watch_history (
    history_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES user_profile(profile_id) ON DELETE CASCADE,
    episode_id UUID NOT NULL REFERENCES episode(episode_id) ON DELETE CASCADE,
    last_watched_second INT DEFAULT 0,
    is_finished BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 에피소드 댓글 (대댓글 지원)
CREATE TABLE IF NOT EXISTS episode_comment (
    comment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    episode_id UUID NOT NULL REFERENCES episode(episode_id) ON DELETE CASCADE,
    profile_id UUID NOT NULL REFERENCES user_profile(profile_id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    parent_comment_id UUID REFERENCES episode_comment(comment_id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

---
-- 기타 테이블
---
-- 광고 배너
CREATE TABLE IF NOT EXISTS ad_banner (
    ad_banner_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    image_url TEXT NOT NULL,
    logo_image_url TEXT,
    link_url TEXT,
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 애니메이션 통합 리뷰/평점
CREATE TABLE IF NOT EXISTS animation_review (
    review_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    animation_id UUID NOT NULL REFERENCES animation(animation_id) ON DELETE CASCADE,
    profile_id UUID NOT NULL REFERENCES user_profile(profile_id) ON DELETE CASCADE,
    score DECIMAL(3,1) NOT NULL CHECK (score >= 0.5 AND score <= 5.0 AND score * 2 = FLOOR(score * 2)),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
