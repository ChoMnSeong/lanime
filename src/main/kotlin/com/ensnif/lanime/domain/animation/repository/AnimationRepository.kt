package com.ensnif.lanime.domain.animation.repository

import com.ensnif.lanime.domain.animation.dto.response.AnimationListItemResponse
import com.ensnif.lanime.domain.animation.dto.response.AnimationListResponse
import com.ensnif.lanime.domain.animation.dto.response.AnimationRankingItemResponse
import com.ensnif.lanime.domain.animation.entity.AirDay
import com.ensnif.lanime.domain.animation.entity.Animation
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.UUID

@Repository
interface AnimationRepository : ReactiveCrudRepository<Animation, UUID> {
    fun findAllByAirDay(airDay: AirDay): Flux<Animation>
    fun findAllByAirDayIsNotNull(): Flux<Animation>

    @Query("""
        SELECT a.animation_id, a.title, a.description, a.thumbnail_url, a.rating AS age_rating,
               a.status, a.air_day, a.released_at,
               at.name AS type
        FROM animation a
        JOIN animation_type at ON a.type_id = at.type_id
        ORDER BY a.created_at DESC
    """)
    fun findAllDetailedAnimations(): Flux<AnimationListItemResponse>

    @Query("""
        SELECT a.animation_id, a.title, a.thumbnail_url, a.rating AS age_rating,
               at.name AS type,
               COALESCE(AVG(r.score), 0.0) AS average_score,
               COUNT(r.review_id) AS review_count
        FROM animation a
        JOIN animation_type at ON a.type_id = at.type_id
        LEFT JOIN animation_review r ON a.animation_id = r.animation_id
        GROUP BY a.animation_id, a.title, a.thumbnail_url, a.rating, at.name
        ORDER BY COUNT(CASE WHEN r.created_at >= NOW() - INTERVAL '7 days' THEN 1 END) DESC,
                 COALESCE(AVG(r.score), 0.0) DESC
        LIMIT 10
    """)
    fun findRealTimeRankings(): Flux<AnimationRankingItemResponse>

    @Query("""
        SELECT a.animation_id, a.title, a.thumbnail_url, a.rating AS age_rating,
               at.name AS type,
               COALESCE(AVG(r.score), 0.0) AS average_score,
               COUNT(r.review_id) AS review_count
        FROM animation a
        JOIN animation_type at ON a.type_id = at.type_id
        LEFT JOIN animation_review r ON a.animation_id = r.animation_id
        WHERE EXTRACT(YEAR FROM a.released_at) = :year
          AND EXTRACT(QUARTER FROM a.released_at) = :quarter
        GROUP BY a.animation_id, a.title, a.thumbnail_url, a.rating, at.name
        ORDER BY COALESCE(AVG(r.score), 0.0) DESC, COUNT(r.review_id) DESC
        LIMIT 10
    """)
    fun findRankingsByYearAndQuarter(year: Int, quarter: Int): Flux<AnimationRankingItemResponse>

    @Query("""
        SELECT a.animation_id, a.title, a.thumbnail_url, a.rating AS age_rating,
               at.name AS type,
               COALESCE(AVG(r.score), 0.0) AS average_score,
               COUNT(r.review_id) AS review_count
        FROM animation a
        JOIN animation_type at ON a.type_id = at.type_id
        LEFT JOIN animation_review r ON a.animation_id = r.animation_id
        WHERE EXTRACT(YEAR FROM a.released_at) = :year
        GROUP BY a.animation_id, a.title, a.thumbnail_url, a.rating, at.name
        ORDER BY COALESCE(AVG(r.score), 0.0) DESC, COUNT(r.review_id) DESC
        LIMIT 10
    """)
    fun findRankingsByYear(year: Int): Flux<AnimationRankingItemResponse>

    @Query("""
        SELECT a.animation_id, a.title, a.thumbnail_url, a.rating AS age_rating,
               at.name AS type,
               COALESCE(AVG(r.score), 0.0) AS average_score,
               COUNT(r.review_id) AS review_count
        FROM animation a
        JOIN animation_type at ON a.type_id = at.type_id
        LEFT JOIN animation_review r ON a.animation_id = r.animation_id
        GROUP BY a.animation_id, a.title, a.thumbnail_url, a.rating, at.name
        ORDER BY COALESCE(AVG(r.score), 0.0) DESC, COUNT(r.review_id) DESC
        LIMIT 10
    """)
    fun findAllTimeRankings(): Flux<AnimationRankingItemResponse>
}
