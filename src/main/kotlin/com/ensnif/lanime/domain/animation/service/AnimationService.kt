package com.ensnif.lanime.domain.animation.service

import com.ensnif.lanime.domain.animation.dto.response.*
import com.ensnif.lanime.domain.animation.entity.AirDay
import com.ensnif.lanime.domain.animation.entity.AnimationType
import com.ensnif.lanime.domain.animation.entity.Genre
import com.ensnif.lanime.domain.animation.entity.RankingType
import com.ensnif.lanime.domain.animation.repository.AnimationRepository
import com.ensnif.lanime.domain.animation.repository.AnimationTypeRepository
import com.ensnif.lanime.domain.animation.repository.GenreRepository
import com.ensnif.lanime.domain.social.dto.RatingCountResponse
import com.ensnif.lanime.domain.social.dto.ReviewResponse
import com.ensnif.lanime.domain.social.repository.ReviewRepository
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.UUID

@Service
class AnimationService(
    private val animationRepository: AnimationRepository,
    private val animationTypeRepository: AnimationTypeRepository,
    private val genreRepository: GenreRepository,
    private val reviewRepository: ReviewRepository
) {

    fun getAllAnimations(): Flux<AnimationListResponse> {
        return animationRepository.findAllDetailedAnimations()
            .flatMap { item ->
                genreRepository.findAllByAnimationId(item.animationId)
                    .map { it.name }
                    .collectList()
                    .map { genres ->
                        AnimationListResponse(
                            id = item.animationId.toString(),
                            title = item.title,
                            description = item.description,
                            thumbnailUrl = item.thumbnailUrl,
                            type = item.type,
                            genres = genres,
                            ageRating = item.ageRating,
                            status = item.status,
                            airDay = item.airDay,
                            releasedAt = item.releasedAt
                        )
                    }
            }
    }

    fun getAnimationTypes(): Flux<AnimationType> {
        return animationTypeRepository.findAll()
    }

    fun getGenres(): Flux<Genre> {
        return genreRepository.findAll()
    }

    fun getWeeklyAnimations(): Mono<Map<String, List<AnimationListResponse>>> {
        val dayOrder = AirDay.entries.map { it.name }

        return animationRepository.findAllByAirDayIsNotNull()
            .flatMap { animation ->
                Mono.zip(
                    animationTypeRepository.findById(animation.typeId),
                    genreRepository.findAllByAnimationId(animation.animationId!!).map { it.name }.collectList()
                ).map { tuple ->
                    val type = tuple.t1
                    val genres = tuple.t2
                    animation.airDay!!.name to AnimationListResponse(
                        id = animation.animationId.toString(),
                        title = animation.title,
                        description = animation.description,
                        thumbnailUrl = animation.thumbnailUrl,
                        type = type.name,
                        genres = genres,
                        ageRating = animation.rating,
                        status = animation.status,
                        airDay = animation.airDay?.name,
                        releasedAt = animation.releasedAt
                    )
                }
            }
            .collectList()
            .map { pairs ->
                val grouped = pairs.groupBy({ it.first }, { it.second })
                dayOrder.associateWith { grouped[it] ?: emptyList() }
            }
    }

    fun getAnimationsByAirDay(airDay: AirDay): Flux<AnimationListResponse> {
        return animationRepository.findAllByAirDay(airDay)
            .flatMap { animation ->
                Mono.zip(
                    animationTypeRepository.findById(animation.typeId),
                    genreRepository.findAllByAnimationId(animation.animationId!!).map { it.name }.collectList()
                ).map { tuple ->
                    val type = tuple.t1
                    val genres = tuple.t2
                    AnimationListResponse(
                        id = animation.animationId.toString(),
                        title = animation.title,
                        description = animation.description,
                        thumbnailUrl = animation.thumbnailUrl,
                        type = type.name,
                        genres = genres,
                        ageRating = animation.rating,
                        status = animation.status,
                        airDay = animation.airDay?.name,
                        releasedAt = animation.releasedAt
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
                        thumbnailUrl = animation.thumbnailUrl ?: "",
                        type = tuple.t1.name,
                        genres = tuple.t2.map { it.name },
                        ageRating = animation.rating,
                        status = animation.status
                    )
                }
            }
    }

    fun getAnimationRankings(type: RankingType): Flux<AnimationRankingResponse> {
        val currentYear = LocalDate.now().year
        val itemsFlux = when (type) {
            RankingType.REALTIME -> animationRepository.findRealTimeRankings()
            RankingType.Q1 -> animationRepository.findRankingsByYearAndQuarter(currentYear, 1)
            RankingType.Q2 -> animationRepository.findRankingsByYearAndQuarter(currentYear, 2)
            RankingType.Q3 -> animationRepository.findRankingsByYearAndQuarter(currentYear, 3)
            RankingType.Q4 -> animationRepository.findRankingsByYearAndQuarter(currentYear, 4)
            RankingType.LAST_YEAR -> animationRepository.findRankingsByYear(currentYear - 1)
            RankingType.ALL -> animationRepository.findAllTimeRankings()
        }
        return itemsFlux.index().map { tuple ->
            AnimationRankingResponse(
                rank = tuple.t1 + 1,
                id = tuple.t2.animationId.toString(),
                title = tuple.t2.title,
                thumbnailUrl = tuple.t2.thumbnailUrl ?: "",
                type = tuple.t2.type,
                ageRating = tuple.t2.ageRating,
                averageScore = tuple.t2.averageScore,
                reviewCount = tuple.t2.reviewCount
            )
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
                ratingCounts = tuple.t3.map { RatingCountItemResponse(it.rating, it.count) },
                reviews = tuple.t4.map { row ->
                    ReviewItemResponse(
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
