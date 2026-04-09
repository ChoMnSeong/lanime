package com.ensnif.lanime.domain.interaction.repository

import com.ensnif.lanime.domain.interaction.dto.WatchedEpisodeResponse
import com.ensnif.lanime.domain.interaction.entity.UserWatchHistory
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface WatchHistoryRepository : ReactiveCrudRepository<UserWatchHistory, UUID> {

    @Query("""
        SELECT e.episode_id, e.episode_number,
               COALESCE(et.title, et_ja.title, e.title) AS title,
               e.thumbnail_url, e.duration, e.animation_id,
               COALESCE(atrans.title, atrans_ja.title, a.title) AS animation_title,
               wh.last_watched_second, wh.is_finished, wh.updated_at AS watched_at
        FROM user_watch_history wh
        JOIN episode e ON e.episode_id = wh.episode_id
        JOIN animation a ON a.animation_id = e.animation_id
        LEFT JOIN episode_translation et ON et.episode_id = e.episode_id AND et.locale = :locale
        LEFT JOIN episode_translation et_ja ON et_ja.episode_id = e.episode_id AND et_ja.locale = 'ja'
        LEFT JOIN animation_translation atrans ON atrans.animation_id = a.animation_id AND atrans.locale = :locale
        LEFT JOIN animation_translation atrans_ja ON atrans_ja.animation_id = a.animation_id AND atrans_ja.locale = 'ja'
        WHERE wh.profile_id = :profileId
        ORDER BY wh.updated_at DESC
        LIMIT :limit OFFSET :offset
    """)
    fun findWatchedEpisodes(profileId: UUID, limit: Int, offset: Long, locale: String): Flux<WatchedEpisodeResponse>

    @Query("SELECT COUNT(*) FROM user_watch_history WHERE profile_id = :profileId")
    fun countByProfileId(profileId: UUID): Mono<Long>

    @Query("""
        INSERT INTO user_watch_history (history_id, profile_id, episode_id, last_watched_second, is_finished, created_at, updated_at)
        SELECT gen_random_uuid(), :profileId, :episodeId, :lastWatchedSecond,
               CASE WHEN e.duration IS NOT NULL AND e.duration > 0
                    THEN :lastWatchedSecond >= e.duration * 0.9
                    ELSE false END,
               NOW(), NOW()
        FROM episode e
        WHERE e.episode_id = :episodeId
        ON CONFLICT (profile_id, episode_id)
        DO UPDATE SET
            last_watched_second = EXCLUDED.last_watched_second,
            is_finished = EXCLUDED.is_finished,
            updated_at = NOW()
    """)
    fun upsertWatchProgress(profileId: UUID, episodeId: UUID, lastWatchedSecond: Int): Mono<Void>
}
