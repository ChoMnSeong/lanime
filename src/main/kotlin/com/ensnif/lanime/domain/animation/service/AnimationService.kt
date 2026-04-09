package com.ensnif.lanime.domain.animation.service

import com.ensnif.lanime.domain.animation.dto.response.*
import com.ensnif.lanime.domain.animation.entity.AirDay
import com.ensnif.lanime.domain.animation.entity.Animation
import com.ensnif.lanime.domain.animation.entity.AnimationStatus
import com.ensnif.lanime.domain.animation.entity.AnimationTranslation
import com.ensnif.lanime.domain.animation.entity.AnimationType
import com.ensnif.lanime.domain.animation.entity.Genre
import com.ensnif.lanime.domain.animation.entity.RankingType
import com.ensnif.lanime.domain.animation.repository.AnimationRepository
import com.ensnif.lanime.domain.animation.repository.AnimationTranslationRepository
import com.ensnif.lanime.domain.animation.repository.AnimationTypeRepository
import com.ensnif.lanime.domain.animation.repository.GenreRepository
import com.ensnif.lanime.domain.interaction.repository.FavoriteRepository
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
    private val animationTranslationRepository: AnimationTranslationRepository,
    private val animationTypeRepository: AnimationTypeRepository,
    private val genreRepository: GenreRepository,
    private val reviewRepository: ReviewRepository,
    private val favoriteRepository: FavoriteRepository
) {

    private fun resolveTranslation(animation: Animation, locale: String): Mono<AnimationTranslation> {
        val id = animation.animationId!!
        return animationTranslationRepository.findByAnimationIdAndLocale(id, locale)
            .switchIfEmpty(animationTranslationRepository.findByAnimationIdAndLocale(id, "ja"))
            .defaultIfEmpty(AnimationTranslation(id, locale, animation.title, animation.description))
    }

    private fun isAgeEligible(rating: String, userAge: Int?): Boolean {
        if (userAge == null) return true
        return when (rating) {
            "ALL" -> true
            "15" -> userAge >= 15
            "19" -> userAge >= 19
            else -> true
        }
    }

    fun getAllAnimations(
        keyword: String? = null,
        typeIds: List<UUID>? = null,
        status: AnimationStatus? = null,
        genreIds: List<UUID>? = null,
        startYear: Int? = null,
        endYear: Int? = null,
        userAge: Int? = null,
        page: Int = 0,
        limit: Int = 30,
        locale: String = "ja"
    ): Flux<AnimationListResponse> {
        val offset = (page * limit).toLong()

        val processedTypeIds = if (typeIds.isNullOrEmpty()) listOf("ALL") else typeIds.map { it.toString() }
        val processedGenreIds = if (genreIds.isNullOrEmpty()) listOf("ALL") else genreIds.map { it.toString() }
        val genreCount = if (genreIds.isNullOrEmpty()) 0 else genreIds.size

        val source = if (keyword == null && typeIds.isNullOrEmpty() && status == null && genreIds.isNullOrEmpty() && startYear == null && endYear == null) {
            animationRepository.findAllDetailedAnimations(locale, userAge, limit, offset)
        } else {
            animationRepository.searchAnimations(
                keyword,
                status?.name,
                processedTypeIds,
                processedGenreIds,
                genreCount,
                startYear,
                endYear,
                userAge,
                locale,
                limit,
                offset
            )
        }

        return source.flatMap { item ->
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

    fun getWeeklyAnimations(userAge: Int? = null, locale: String = "ja"): Mono<Map<String, List<AnimationListResponse>>> {
        val dayOrder = AirDay.entries.map { it.name }

        return animationRepository.findAllByAirDayIsNotNull(userAge)
            .flatMap { animation ->
                Mono.zip(
                    animationTypeRepository.findById(animation.typeId),
                    genreRepository.findAllByAnimationId(animation.animationId!!).map { it.name }.collectList(),
                    resolveTranslation(animation, locale)
                ).map { tuple ->
                    val type = tuple.t1
                    val genres = tuple.t2
                    val translation = tuple.t3
                    animation.airDay!!.name to AnimationListResponse(
                        id = animation.animationId.toString(),
                        title = translation.title,
                        description = translation.description,
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

    fun getAnimationsByAirDay(airDay: AirDay, userAge: Int? = null, locale: String = "ja"): Flux<AnimationListResponse> {
        return animationRepository.findAllByAirDay(airDay, userAge)
            .flatMap { animation ->
                Mono.zip(
                    animationTypeRepository.findById(animation.typeId),
                    genreRepository.findAllByAnimationId(animation.animationId!!).map { it.name }.collectList(),
                    resolveTranslation(animation, locale)
                ).map { tuple ->
                    val type = tuple.t1
                    val genres = tuple.t2
                    val translation = tuple.t3
                    AnimationListResponse(
                        id = animation.animationId.toString(),
                        title = translation.title,
                        description = translation.description,
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

    fun getAnimationDetail(animationId: UUID, profileId: UUID? = null, locale: String = "ja"): Mono<AnimationDetailResponse> {
        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap { animation ->
                val isFavoriteMono = if (profileId != null) {
                    favoriteRepository.existsByProfileIdAndAnimationId(profileId, animationId)
                } else {
                    Mono.just(false)
                }
                val translationMono = resolveTranslation(animation, locale)

                Mono.zip(
                    animationTypeRepository.findById(animation.typeId),
                    genreRepository.findAllByAnimationId(animationId).collectList(),
                    isFavoriteMono,
                    translationMono
                ).map { tuple ->
                    AnimationDetailResponse(
                        id = animation.animationId.toString(),
                        title = tuple.t4.title,
                        description = tuple.t4.description ?: "",
                        thumbnailUrl = animation.thumbnailUrl ?: "",
                        type = tuple.t1.name,
                        genres = tuple.t2.map { it.name },
                        ageRating = animation.rating,
                        status = animation.status,
                        isFavorite = tuple.t3
                    )
                }
            }
    }

    fun getAnimationRankings(type: RankingType, userAge: Int? = null, locale: String = "ja"): Flux<AnimationRankingResponse> {
        val currentYear = LocalDate.now().year
        val itemsFlux = when (type) {
            RankingType.REALTIME -> animationRepository.findRealTimeRankings(userAge, locale)
            RankingType.Q1 -> animationRepository.findRankingsByYearAndQuarter(currentYear, 1, userAge, locale)
            RankingType.Q2 -> animationRepository.findRankingsByYearAndQuarter(currentYear, 2, userAge, locale)
            RankingType.Q3 -> animationRepository.findRankingsByYearAndQuarter(currentYear, 3, userAge, locale)
            RankingType.Q4 -> animationRepository.findRankingsByYearAndQuarter(currentYear, 4, userAge, locale)
            RankingType.LAST_YEAR -> animationRepository.findRankingsByYear(currentYear - 1, userAge, locale)
            RankingType.ALL -> animationRepository.findAllTimeRankings(userAge, locale)
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
                    reviewCount = tuple.t2.reviewCount,
                    watchCount = tuple.t2.watchCount
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

    fun getSimilarAnimations(
        animationId: UUID,
        matchPercentage: Int = 50,
        userAge: Int? = null,
        locale: String = "ja",
        page: Int = 0,
        limit: Int = 10
    ): Flux<AnimationListResponse> {
        val offset = (page * limit).toLong()

        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMapMany { _ ->
                // Get genres of the target animation (exclude AWARD_WINNING)
                genreRepository.findAllByAnimationId(animationId)
                    .filter { it.name != "AWARD_WINNING" }
                    .map { it.genreId!! }
                    .collectList()
                    .flatMapMany { targetGenreIds ->
                        if (targetGenreIds.isEmpty()) {
                            return@flatMapMany Flux.empty<AnimationListResponse>()
                        }

                        val targetGenreSet = targetGenreIds.toSet()

                        // Get all animations and filter by genre match percentage
                        animationRepository.findAllDetailedAnimations(locale, userAge, 10000, 0)
                            .filter { it.animationId != animationId } // Exclude target animation
                            .flatMap { item ->
                                // Get genres of current animation (exclude AWARD_WINNING)
                                genreRepository.findAllByAnimationId(item.animationId)
                                    .filter { it.name != "AWARD_WINNING" }
                                    .map { it.genreId!! }
                                    .collectList()
                                    .flatMap { currentGenreIds ->
                                        // Calculate match percentage
                                        val matchCount = currentGenreIds.intersect(targetGenreSet).size
                                        val percentage = (matchCount * 100) / targetGenreIds.size

                                        if (percentage >= matchPercentage) {
                                            genreRepository.findAllByAnimationId(item.animationId)
                                                .filter { it.name != "AWARD_WINNING" }
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
                                        } else {
                                            Mono.empty<AnimationListResponse>()
                                        }
                                    }
                            }
                            .skip(offset)
                            .take(limit.toLong())
                    }
            }
    }
}