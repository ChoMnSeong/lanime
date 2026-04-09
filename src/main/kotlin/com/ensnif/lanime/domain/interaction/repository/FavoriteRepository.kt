package com.ensnif.lanime.domain.interaction.repository

import com.ensnif.lanime.domain.interaction.dto.FavoriteAnimationResponse
import com.ensnif.lanime.domain.interaction.entity.Favorite
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface FavoriteRepository : ReactiveCrudRepository<Favorite, UUID> {

    @Query("""
        SELECT a.animation_id, a.title, a.thumbnail_url,
               at.name AS type, a.status,
               f.created_at AS favorited_at
        FROM user_animation_favorite f
        JOIN animation a ON a.animation_id = f.animation_id
        JOIN animation_type at ON at.type_id = a.type_id
        WHERE f.profile_id = :profileId
        ORDER BY f.created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    fun findFavoritesByProfileId(profileId: UUID, limit: Int, offset: Long): Flux<FavoriteAnimationResponse>

    @Query("SELECT COUNT(*) FROM user_animation_favorite WHERE profile_id = :profileId")
    fun countByProfileId(profileId: UUID): Mono<Long>

    @Query("SELECT EXISTS(SELECT 1 FROM user_animation_favorite WHERE profile_id = :profileId AND animation_id = :animationId)")
    fun existsByProfileIdAndAnimationId(profileId: UUID, animationId: UUID): Mono<Boolean>

    @Query("DELETE FROM user_animation_favorite WHERE profile_id = :profileId AND animation_id = :animationId")
    fun deleteByProfileIdAndAnimationId(profileId: UUID, animationId: UUID): Mono<Void>
}
