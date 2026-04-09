package com.ensnif.lanime.domain.animation.repository

import com.ensnif.lanime.domain.animation.entity.Genre
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface GenreRepository : ReactiveCrudRepository<Genre, UUID> {

    fun findByName(name: String): Mono<Genre>

    @Query("""
        INSERT INTO genre (genre_id, name)
        VALUES (gen_random_uuid(), :name)
        ON CONFLICT (name) DO NOTHING
        RETURNING *
    """)
    fun insertIfAbsent(name: String): Mono<Genre>

    @Query("""
        SELECT g.genre_id, g.name, g.created_at, g.updated_at
        FROM genre g
        JOIN animation_genre ag ON g.genre_id = ag.genre_id
        WHERE ag.animation_id = :animationId
    """)
    fun findAllByAnimationId(animationId: UUID): Flux<Genre>
}
