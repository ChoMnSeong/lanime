package com.ensnif.lanime.domain.episode.repository

import com.ensnif.lanime.domain.episode.dto.EpisodeResponse
import com.ensnif.lanime.domain.episode.entity.Episode
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

interface EpisodeRepository : ReactiveCrudRepository<Episode, UUID> {

    fun findAllByAnimationId(animationId: UUID): Flux<Episode>

    @Query("""
        SELECT e.episode_id, e.episode_number,
               COALESCE(t.title, t_ja.title, e.title) AS title,
               e.thumbnail_url,
               COALESCE(t.description, t_ja.description, e.description) AS description,
               e.video_url, e.duration, e.hls_path, e.encoding_status,
               0 AS last_watched_second, false AS is_finished
        FROM episode e
        LEFT JOIN episode_translation t ON t.episode_id = e.episode_id AND t.locale = :locale
        LEFT JOIN episode_translation t_ja ON t_ja.episode_id = e.episode_id AND t_ja.locale = 'ja'
        WHERE e.animation_id = :animationId
        ORDER BY e.episode_number ASC
    """)
    fun findAllByAnimationId(animationId: UUID, locale: String): Flux<EpisodeResponse>

    @Query("""
        SELECT e.episode_id, e.episode_number,
               COALESCE(t.title, t_ja.title, e.title) AS title,
               e.thumbnail_url,
               COALESCE(t.description, t_ja.description, e.description) AS description,
               e.video_url, e.duration, e.hls_path, e.encoding_status,
               COALESCE(wh.last_watched_second, 0) AS last_watched_second,
               COALESCE(wh.is_finished, false) AS is_finished
        FROM episode e
        LEFT JOIN user_watch_history wh
            ON wh.episode_id = e.episode_id AND wh.profile_id = :profileId
        LEFT JOIN episode_translation t ON t.episode_id = e.episode_id AND t.locale = :locale
        LEFT JOIN episode_translation t_ja ON t_ja.episode_id = e.episode_id AND t_ja.locale = 'ja'
        WHERE e.animation_id = :animationId
        ORDER BY e.episode_number ASC
    """)
    fun findAllByAnimationIdWithWatchHistory(animationId: UUID, profileId: UUID, locale: String): Flux<EpisodeResponse>

    @Query("""
        SELECT * FROM episode WHERE animation_id = :animationId AND episode_number = :episodeNumber
    """)
    fun findByAnimationIdAndEpisodeNumber(animationId: UUID, episodeNumber: Int): reactor.core.publisher.Mono<Episode>

    @Query("""
        INSERT INTO episode (animation_id, episode_number, title, thumbnail_url, description, video_url, duration)
        VALUES (:animationId, :episodeNumber, :title, :thumbnailUrl, :description, :videoUrl, :duration)
        ON CONFLICT (animation_id, episode_number) DO UPDATE
        SET title             = EXCLUDED.title,
            thumbnail_url     = EXCLUDED.thumbnail_url,
            duration          = COALESCE(EXCLUDED.duration, episode.duration)
    """)
    fun upsertRaw(
        animationId: UUID, episodeNumber: Int, title: String,
        thumbnailUrl: String?, description: String?, videoUrl: String?, duration: Int?
    ): reactor.core.publisher.Mono<Void>
}
