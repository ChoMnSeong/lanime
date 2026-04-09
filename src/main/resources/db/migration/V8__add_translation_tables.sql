-- animation 번역 테이블
CREATE TABLE animation_translation (
    animation_id UUID NOT NULL REFERENCES animation(animation_id) ON DELETE CASCADE,
    locale       VARCHAR(10) NOT NULL,
    title        TEXT NOT NULL,
    description  TEXT,
    PRIMARY KEY (animation_id, locale)
);

-- episode 번역 테이블
CREATE TABLE episode_translation (
    episode_id UUID NOT NULL REFERENCES episode(episode_id) ON DELETE CASCADE,
    locale     VARCHAR(10) NOT NULL,
    title      TEXT NOT NULL,
    description TEXT,
    PRIMARY KEY (episode_id, locale)
);

-- 기존 animation 데이터를 'ja'로 이관
INSERT INTO animation_translation (animation_id, locale, title, description)
SELECT animation_id, 'ja', title, description
FROM animation
WHERE title IS NOT NULL;

-- 기존 episode 데이터를 'ja'로 이관
INSERT INTO episode_translation (episode_id, locale, title, description)
SELECT episode_id, 'ja', title, description
FROM episode
WHERE title IS NOT NULL;
