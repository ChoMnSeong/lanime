package com.ensnif.lanime.domain.social.repository

import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

@Repository
class ReviewQueryRepository(private val databaseClient: DatabaseClient) {

    fun findAverageScore(animationId: UUID): Mono<Double> {
        return databaseClient.sql(
            "SELECT COALESCE(AVG(score::float8), 0.0) FROM animation_review WHERE animation_id = :animationId"
        )
            .bind("animationId", animationId)
            .map { row -> row.get(0, Double::class.java) ?: 0.0 }
            .one()
            .defaultIfEmpty(0.0)
    }

    fun countByRating(animationId: UUID): Flux<RatingCountRow> {
        return databaseClient.sql("""
            SELECT score AS rating, COUNT(*) AS count
            FROM animation_review
            WHERE animation_id = :animationId
            GROUP BY score
            ORDER BY score
        """)
            .bind("animationId", animationId)
            .map { row ->
                RatingCountRow(
                    rating = row.get("rating", Integer::class.java)!!.toInt(),
                    count = row.get("count", Long::class.java)!!
                )
            }
            .all()
    }

    fun findReviewsWithProfile(animationId: UUID, limit: Int, offset: Long): Flux<ReviewRow> {
        return databaseClient.sql("""
            SELECT r.review_id, r.profile_id, r.score, r.content, r.created_at, r.updated_at,
                   p.name AS profile_name, p.avatar_url
            FROM animation_review r
            JOIN user_profile p ON r.profile_id = p.profile_id
            WHERE r.animation_id = :animationId
            ORDER BY r.created_at DESC
            LIMIT :limit OFFSET :offset
        """)
            .bind("animationId", animationId)
            .bind("limit", limit)
            .bind("offset", offset)
            .map { row ->
                ReviewRow(
                    reviewId = row.get("review_id", UUID::class.java)!!,
                    profileId = row.get("profile_id", UUID::class.java)!!,
                    score = row.get("score", Integer::class.java)!!.toInt(),
                    content = row.get("content", String::class.java),
                    createdAt = row.get("created_at", LocalDateTime::class.java),
                    updatedAt = row.get("updated_at", LocalDateTime::class.java),
                    profileName = row.get("profile_name", String::class.java)!!,
                    avatarUrl = row.get("avatar_url", String::class.java)
                )
            }
            .all()
    }
}

data class RatingCountRow(
    val rating: Int,
    val count: Long
)

data class ReviewRow(
    val reviewId: UUID,
    val profileId: UUID,
    val score: Int,
    val content: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val profileName: String,
    val avatarUrl: String?
)
