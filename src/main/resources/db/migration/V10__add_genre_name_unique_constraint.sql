-- genre.name 중복 방지를 위한 UNIQUE 제약 추가
-- 기존 중복 데이터 제거 (같은 name 중 가장 오래된 것만 남김)
DELETE FROM genre
WHERE genre_id NOT IN (
    SELECT DISTINCT ON (name) genre_id
    FROM genre
    ORDER BY name, created_at
);

ALTER TABLE genre ADD CONSTRAINT genre_name_unique UNIQUE (name);
