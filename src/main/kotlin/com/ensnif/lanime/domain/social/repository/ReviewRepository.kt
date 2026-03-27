package com.ensnif.lanime.domain.social.repository

import com.ensnif.lanime.domain.social.dto.RatingCountResponse
import com.ensnif.lanime.domain.social.dto.ReviewResponse
import com.ensnif.lanime.domain.social.entity.AnimationReview
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface ReviewRepository : ReactiveCrudRepository<AnimationReview, UUID> {

    fun countByAnimationId(animationId: UUID): Mono<Long>

    fun existsByAnimationIdAndProfileId(animationId: UUID, profileId: UUID): Mono<Boolean>

    fun findByAnimationIdAndProfileId(animationId: UUID, profileId: UUID): Mono<AnimationReview>

    @Query("SELECT COALESCE(AVG(score), 0.0) FROM animation_review WHERE animation_id = :animationId")
    fun findAverageScore(animationId: UUID): Mono<Double>

    @Query("""
        SELECT score AS rating, COUNT(*) AS count
        FROM animation_review
        WHERE animation_id = :animationId
        GROUP BY score
        ORDER BY score
    """)
    fun countScoreByGroup(animationId: UUID): Flux<RatingCountResponse>

    @Query("""
        SELECT r.review_id, r.profile_id, r.score, r.content, r.created_at, r.updated_at,
               p.name AS profile_name, p.avatar_url
        FROM animation_review r
        JOIN user_profile p ON r.profile_id = p.profile_id
        WHERE r.animation_id = :animationId
        ORDER BY (r.profile_id = :profileId) DESC, r.created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    fun findReviewsWithProfileFirst(animationId: UUID, profileId: UUID, limit: Int, offset: Long): Flux<ReviewResponse>

    @Query("""
        SELECT r.review_id, r.profile_id, r.score, r.content, r.created_at, r.updated_at,
               p.name AS profile_name, p.avatar_url
        FROM animation_review r
        JOIN user_profile p ON r.profile_id = p.profile_id
        WHERE r.animation_id = :animationId
        ORDER BY r.created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    fun findReviews(animationId: UUID, limit: Int, offset: Long): Flux<ReviewResponse>
}
