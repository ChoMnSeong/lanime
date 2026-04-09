package com.ensnif.lanime.domain.animation.repository

import com.ensnif.lanime.domain.animation.entity.AnimationGenre
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

interface AnimationGenreRepository : ReactiveCrudRepository<AnimationGenre, UUID> {
    fun findAllByAnimationId(animationId: UUID): Flux<AnimationGenre>
    fun deleteAllByAnimationId(animationId: UUID): reactor.core.publisher.Mono<Void>

    @org.springframework.data.r2dbc.repository.Query("""
        INSERT INTO animation_genre (animation_id, genre_id)
        VALUES (:animationId, :genreId)
        ON CONFLICT (animation_id, genre_id) DO NOTHING
    """)
    fun insertIfAbsent(animationId: UUID, genreId: UUID): reactor.core.publisher.Mono<Void>

    @org.springframework.data.r2dbc.repository.Query("""
        DELETE FROM animation_genre WHERE animation_id = :animationId AND genre_id = :genreId
    """)
    fun deleteByAnimationIdAndGenreId(animationId: UUID, genreId: UUID): reactor.core.publisher.Mono<Void>
}
