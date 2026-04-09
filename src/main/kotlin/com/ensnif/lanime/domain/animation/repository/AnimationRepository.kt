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
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface AnimationRepository : ReactiveCrudRepository<Animation, UUID> {
    @Query("""
        SELECT * FROM animation
        WHERE air_day = :#{#airDay.name()}
          AND (status IN ('ONGOING', 'UPCOMING')
               OR (status = 'FINISHED' AND finished_at >= NOW() - INTERVAL '7 days'))
          AND (:userAge IS NULL
               OR rating = 'ALL'
               OR (rating = '15' AND :userAge >= 15)
               OR (rating = '19' AND :userAge >= 19))
    """)
    fun findAllByAirDay(airDay: AirDay, userAge: Int?): Flux<Animation>

    @Query("""
        SELECT * FROM animation
        WHERE air_day IS NOT NULL
          AND (status IN ('ONGOING', 'UPCOMING')
               OR (status = 'FINISHED' AND finished_at >= NOW() - INTERVAL '7 days'))
          AND (:userAge IS NULL
               OR rating = 'ALL'
               OR (rating = '15' AND :userAge >= 15)
               OR (rating = '19' AND :userAge >= 19))
    """)
    fun findAllByAirDayIsNotNull(userAge: Int?): Flux<Animation>
    
    fun existsByMalId(malId: Int): Mono<Boolean>
    fun findByMalId(malId: Int): Mono<Animation>

    @Query("""
        SELECT a.animation_id,
               COALESCE(t.title, t_ja.title, a.title) AS title,
               COALESCE(t.description, t_ja.description, a.description) AS description,
               a.thumbnail_url, a.rating AS age_rating,
               a.status, a.air_day, a.released_at, at.name AS type, a.created_at
        FROM animation a
        JOIN animation_type at ON a.type_id = at.type_id
        LEFT JOIN animation_translation t ON t.animation_id = a.animation_id AND t.locale = :locale
        LEFT JOIN animation_translation t_ja ON t_ja.animation_id = a.animation_id AND t_ja.locale = 'ja'
        WHERE :userAge IS NULL
           OR a.rating = 'ALL'
           OR (a.rating = '15' AND :userAge >= 15)
           OR (a.rating = '19' AND :userAge >= 19)
        ORDER BY a.created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    fun findAllDetailedAnimations(locale: String, userAge: Int?, limit: Int, offset: Long): Flux<AnimationListItemResponse>

    @Query("""
        SELECT DISTINCT a.animation_id,
               COALESCE(t.title, t_ja.title, a.title) AS title,
               COALESCE(t.description, t_ja.description, a.description) AS description,
               a.thumbnail_url, a.rating AS age_rating,
               a.status, a.air_day, a.released_at, at.name AS type, a.created_at
        FROM animation a
        JOIN animation_type at ON a.type_id = at.type_id
        LEFT JOIN animation_translation t ON t.animation_id = a.animation_id AND t.locale = :locale
        LEFT JOIN animation_translation t_ja ON t_ja.animation_id = a.animation_id AND t_ja.locale = 'ja'
        WHERE (:keyword IS NULL OR COALESCE(t.title, t_ja.title, a.title) ILIKE '%' || :keyword || '%')
          AND (:status IS NULL OR a.status = :status)
          AND ('ALL' IN (:typeIds) OR a.type_id::text IN (:typeIds))
          AND ('ALL' IN (:genreIds) OR
               (SELECT COUNT(ag.genre_id)
                FROM animation_genre ag
                WHERE ag.animation_id = a.animation_id
                  AND ag.genre_id::text IN (:genreIds)
               ) = :genreCount)
          AND (:startYear IS NULL OR EXTRACT(YEAR FROM a.released_at) >= :startYear)
          AND (:endYear IS NULL OR EXTRACT(YEAR FROM a.released_at) <= :endYear)
          AND (:userAge IS NULL
               OR a.rating = 'ALL'
               OR (a.rating = '15' AND :userAge >= 15)
               OR (a.rating = '19' AND :userAge >= 19))
        ORDER BY a.created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    fun searchAnimations(
        keyword: String?,
        status: String?,
        typeIds: List<String>,
        genreIds: List<String>,
        genreCount: Int,
        startYear: Int?,
        endYear: Int?,
        userAge: Int?,
        locale: String,
        limit: Int,
        offset: Long
    ): Flux<AnimationListItemResponse>

    @Query("""
        SELECT a.animation_id,
               COALESCE(t.title, t_ja.title, a.title) AS title,
               a.thumbnail_url, a.rating AS age_rating,
               at.name AS type,
               COALESCE(AVG(r.score), 0.0) AS average_score,
               COUNT(DISTINCT r.review_id) AS review_count,
               COUNT(DISTINCT wh.history_id) AS watch_count
        FROM animation a
        JOIN animation_type at ON a.type_id = at.type_id
        LEFT JOIN animation_review r ON r.animation_id = a.animation_id
        LEFT JOIN episode ep ON ep.animation_id = a.animation_id
        LEFT JOIN user_watch_history wh ON wh.episode_id = ep.episode_id
        LEFT JOIN animation_translation t ON t.animation_id = a.animation_id AND t.locale = :locale
        LEFT JOIN animation_translation t_ja ON t_ja.animation_id = a.animation_id AND t_ja.locale = 'ja'
        WHERE :userAge IS NULL
           OR a.rating = 'ALL'
           OR (a.rating = '15' AND :userAge >= 15)
           OR (a.rating = '19' AND :userAge >= 19)
        GROUP BY a.animation_id, a.title, a.thumbnail_url, a.rating, at.name, t.title, t_ja.title
        ORDER BY
          COUNT(DISTINCT CASE WHEN wh.updated_at >= NOW() - INTERVAL '7 days' THEN wh.history_id END) * 2 +
          COUNT(DISTINCT CASE WHEN r.created_at >= NOW() - INTERVAL '7 days' THEN r.review_id END) * 3 +
          COALESCE(AVG(CASE WHEN r.created_at >= NOW() - INTERVAL '7 days' THEN r.score END), 0.0) DESC
        LIMIT 10
    """)
    fun findRealTimeRankings(userAge: Int?, locale: String): Flux<AnimationRankingItemResponse>

    @Query("""
        SELECT a.animation_id,
               COALESCE(t.title, t_ja.title, a.title) AS title,
               a.thumbnail_url, a.rating AS age_rating,
               at.name AS type,
               COALESCE(AVG(r.score), 0.0) AS average_score,
               COUNT(DISTINCT r.review_id) AS review_count,
               COUNT(DISTINCT wh.history_id) AS watch_count
        FROM animation a
        JOIN animation_type at ON a.type_id = at.type_id
        LEFT JOIN animation_review r ON r.animation_id = a.animation_id
        LEFT JOIN episode ep ON ep.animation_id = a.animation_id
        LEFT JOIN user_watch_history wh ON wh.episode_id = ep.episode_id
        LEFT JOIN animation_translation t ON t.animation_id = a.animation_id AND t.locale = :locale
        LEFT JOIN animation_translation t_ja ON t_ja.animation_id = a.animation_id AND t_ja.locale = 'ja'
        WHERE EXTRACT(YEAR FROM a.released_at) = :year
          AND EXTRACT(QUARTER FROM a.released_at) = :quarter
          AND (:userAge IS NULL
               OR a.rating = 'ALL'
               OR (a.rating = '15' AND :userAge >= 15)
               OR (a.rating = '19' AND :userAge >= 19))
        GROUP BY a.animation_id, a.title, a.thumbnail_url, a.rating, at.name, t.title, t_ja.title
        ORDER BY
          COUNT(DISTINCT wh.history_id) * 2 +
          COUNT(DISTINCT r.review_id) * 3 +
          COALESCE(AVG(r.score), 0.0) DESC
        LIMIT 10
    """)
    fun findRankingsByYearAndQuarter(year: Int, quarter: Int, userAge: Int?, locale: String): Flux<AnimationRankingItemResponse>

    @Query("""
        SELECT a.animation_id,
               COALESCE(t.title, t_ja.title, a.title) AS title,
               a.thumbnail_url, a.rating AS age_rating,
               at.name AS type,
               COALESCE(AVG(r.score), 0.0) AS average_score,
               COUNT(DISTINCT r.review_id) AS review_count,
               COUNT(DISTINCT wh.history_id) AS watch_count
        FROM animation a
        JOIN animation_type at ON a.type_id = at.type_id
        LEFT JOIN animation_review r ON r.animation_id = a.animation_id
        LEFT JOIN episode ep ON ep.animation_id = a.animation_id
        LEFT JOIN user_watch_history wh ON wh.episode_id = ep.episode_id
        LEFT JOIN animation_translation t ON t.animation_id = a.animation_id AND t.locale = :locale
        LEFT JOIN animation_translation t_ja ON t_ja.animation_id = a.animation_id AND t_ja.locale = 'ja'
        WHERE EXTRACT(YEAR FROM a.released_at) = :year
          AND (:userAge IS NULL
               OR a.rating = 'ALL'
               OR (a.rating = '15' AND :userAge >= 15)
               OR (a.rating = '19' AND :userAge >= 19))
        GROUP BY a.animation_id, a.title, a.thumbnail_url, a.rating, at.name, t.title, t_ja.title
        ORDER BY
          COUNT(DISTINCT wh.history_id) * 2 +
          COUNT(DISTINCT r.review_id) * 3 +
          COALESCE(AVG(r.score), 0.0) DESC
        LIMIT 10
    """)
    fun findRankingsByYear(year: Int, userAge: Int?, locale: String): Flux<AnimationRankingItemResponse>

    @Query("""
        SELECT a.animation_id,
               COALESCE(t.title, t_ja.title, a.title) AS title,
               a.thumbnail_url, a.rating AS age_rating,
               at.name AS type,
               COALESCE(AVG(r.score), 0.0) AS average_score,
               COUNT(DISTINCT r.review_id) AS review_count,
               COUNT(DISTINCT wh.history_id) AS watch_count
        FROM animation a
        JOIN animation_type at ON a.type_id = at.type_id
        LEFT JOIN animation_review r ON r.animation_id = a.animation_id
        LEFT JOIN episode ep ON ep.animation_id = a.animation_id
        LEFT JOIN user_watch_history wh ON wh.episode_id = ep.episode_id
        LEFT JOIN animation_translation t ON t.animation_id = a.animation_id AND t.locale = :locale
        LEFT JOIN animation_translation t_ja ON t_ja.animation_id = a.animation_id AND t_ja.locale = 'ja'
        WHERE :userAge IS NULL
           OR a.rating = 'ALL'
           OR (a.rating = '15' AND :userAge >= 15)
           OR (a.rating = '19' AND :userAge >= 19)
        GROUP BY a.animation_id, a.title, a.thumbnail_url, a.rating, at.name, t.title, t_ja.title
        ORDER BY
          COUNT(DISTINCT wh.history_id) * 2 +
          COUNT(DISTINCT r.review_id) * 3 +
          COALESCE(AVG(r.score), 0.0) DESC
        LIMIT 10
    """)
    fun findAllTimeRankings(userAge: Int?, locale: String): Flux<AnimationRankingItemResponse>
}