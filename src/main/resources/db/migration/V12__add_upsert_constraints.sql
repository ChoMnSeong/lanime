-- animation_genre: (animation_id, genre_id) 중복 방지로 upsert 가능
ALTER TABLE animation_genre
    ADD CONSTRAINT animation_genre_unique UNIQUE (animation_id, genre_id);

-- episode: (animation_id, episode_number) 중복 방지로 upsert 가능
ALTER TABLE episode
    ADD CONSTRAINT episode_animation_number_unique UNIQUE (animation_id, episode_number);
