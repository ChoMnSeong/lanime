package com.ensnif.lanime.domain.episode.repository

import com.ensnif.lanime.domain.episode.dto.EpisodeResponse
import com.ensnif.lanime.domain.episode.entity.Episode
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

interface EpisodeRepository : ReactiveCrudRepository<Episode, UUID> {

    @Query("""
        SELECT e.episode_id, e.episode_number, e.title, e.thumbnail_url, e.description,
               e.video_url, e.duration, e.hls_path, e.encoding_status,
               0 AS last_watched_second, false AS is_finished
        FROM episode e
        WHERE e.animation_id = :animationId
        ORDER BY e.episode_number ASC
    """)
    fun findAllByAnimationId(animationId: UUID): Flux<EpisodeResponse>

    @Query("""
        SELECT e.episode_id, e.episode_number, e.title, e.thumbnail_url, e.description,
               e.video_url, e.duration, e.hls_path, e.encoding_status,
               COALESCE(wh.last_watched_second, 0) AS last_watched_second,
               COALESCE(wh.is_finished, false) AS is_finished
        FROM episode e
        LEFT JOIN user_watch_history wh
            ON wh.episode_id = e.episode_id AND wh.profile_id = :profileId
        WHERE e.animation_id = :animationId
        ORDER BY e.episode_number ASC
    """)
    fun findAllByAnimationIdWithWatchHistory(animationId: UUID, profileId: UUID): Flux<EpisodeResponse>
}
