package com.ensnif.lanime.domain.episode.repository

import com.ensnif.lanime.domain.episode.entity.EpisodeTranslation
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface EpisodeTranslationRepository : ReactiveCrudRepository<EpisodeTranslation, UUID> {

    @Query("SELECT * FROM episode_translation WHERE episode_id = :episodeId AND locale = :locale")
    fun findByEpisodeIdAndLocale(episodeId: UUID, locale: String): Mono<EpisodeTranslation>

    @Query("""
        INSERT INTO episode_translation (episode_id, locale, title, description)
        VALUES (:episodeId, :locale, :title, :description)
        ON CONFLICT (episode_id, locale) DO UPDATE
        SET title = EXCLUDED.title, description = EXCLUDED.description
    """)
    fun upsert(episodeId: UUID, locale: String, title: String, description: String?): Mono<Void>
}
