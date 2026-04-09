package com.ensnif.lanime.domain.animation.repository

import com.ensnif.lanime.domain.animation.entity.AnimationTranslation
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface AnimationTranslationRepository : ReactiveCrudRepository<AnimationTranslation, UUID> {

    @Query("SELECT * FROM animation_translation WHERE animation_id = :animationId AND locale = :locale")
    fun findByAnimationIdAndLocale(animationId: UUID, locale: String): Mono<AnimationTranslation>

    @Query("""
        INSERT INTO animation_translation (animation_id, locale, title, description)
        VALUES (:animationId, :locale, :title, :description)
        ON CONFLICT (animation_id, locale) DO UPDATE
        SET title = EXCLUDED.title, description = EXCLUDED.description
    """)
    fun upsert(animationId: UUID, locale: String, title: String, description: String?): Mono<Void>
}
