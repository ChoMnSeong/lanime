ALTER TABLE user_animation_favorite
    ADD CONSTRAINT uq_favorite_profile_animation UNIQUE (profile_id, animation_id);
