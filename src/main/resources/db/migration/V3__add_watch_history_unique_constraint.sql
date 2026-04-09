ALTER TABLE user_watch_history
    ADD CONSTRAINT uq_watch_history_profile_episode UNIQUE (profile_id, episode_id);
