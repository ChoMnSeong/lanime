package com.ensnif.lanime.domain.animation.service

import com.ensnif.lanime.domain.animation.dto.response.*
import com.ensnif.lanime.domain.social.dto.RatingCountResponse
import com.ensnif.lanime.domain.social.dto.ReviewResponse
import com.ensnif.lanime.domain.animation.repository.AnimationRepository
import com.ensnif.lanime.domain.animation.repository.AnimationTypeRepository
import com.ensnif.lanime.domain.animation.repository.GenreRepository
import com.ensnif.lanime.domain.social.repository.ReviewRepository
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class AnimationService(
    private val animationRepository: AnimationRepository,
    private val animationTypeRepository: AnimationTypeRepository,
    private val genreRepository: GenreRepository,
    private val reviewRepository: ReviewRepository
) {

    fun getWeeklyAnimations(airDay: String?): Flux<AnimationListResponse> {
        val animations = if (airDay != null) {
            animationRepository.findAllByAirDay(airDay)
        } else {
            animationRepository.findAllByAirDayIsNotNull()
        }

        return animations.flatMap { animation ->
            animationTypeRepository.findById(animation.typeId)
                .map { type ->
                    AnimationListResponse(
                        id = animation.animationId.toString(),
                        title = animation.title,
                        thumbnailURL = animation.thumbnailUrl ?: "",
                        type = type.name,
                        ageRating = animation.rating
                    )
                }
        }
    }

    fun getAnimationDetail(animationId: UUID): Mono<AnimationDetailResponse> {
        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap { animation ->
                Mono.zip(
                    animationTypeRepository.findById(animation.typeId),
                    genreRepository.findAllByAnimationId(animationId).collectList()
                ).map { tuple ->
                    AnimationDetailResponse(
                        id = animation.animationId.toString(),
                        title = animation.title,
                        description = animation.description ?: "",
                        thumbnailURL = animation.thumbnailUrl ?: "",
                        type = tuple.t1.name,
                        genres = tuple.t2.map { it.name },
                        ageRating = animation.rating,
                        status = animation.status
                    )
                }
            }
    }

    fun getAnimationRatings(animationId: UUID, page: Int, limit: Int, profileId: UUID? = null): Mono<AnimationReviewRatingsResponse> {
        val offset = (page * limit).toLong()

        val reviewsFlux = if (profileId != null) {
            reviewRepository.findReviewsWithProfileFirst(animationId, profileId, limit, offset)
        } else {
            reviewRepository.findReviews(animationId, limit, offset)
        }

        return Mono.zip(
            reviewRepository.findAverageScore(animationId),
            reviewRepository.countByAnimationId(animationId),
            reviewRepository.countScoreByGroup(animationId).collectList(),
            reviewsFlux.collectList()
        ).map { tuple ->
            AnimationReviewRatingsResponse(
                averageRating = tuple.t1,
                totalCount = tuple.t2,
                ratingCounts = tuple.t3.map { RatingCount(it.rating, it.count) },
                reviews = tuple.t4.map { row ->
                    Review(
                        reviewId = row.reviewId.toString(),
                        profileId = row.profileId.toString(),
                        rating = row.score,
                        comment = row.content ?: "",
                        createdAt = row.createdAt?.toString() ?: "",
                        updateAt = row.updatedAt?.toString() ?: "",
                        profileName = row.profileName,
                        avatarURL = row.avatarUrl ?: ""
                    )
                }
            )
        }
    }
}
