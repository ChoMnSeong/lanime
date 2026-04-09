package com.ensnif.lanime.domain.animation.controller

import com.ensnif.lanime.domain.animation.dto.request.*
import com.ensnif.lanime.domain.animation.dto.response.*
import com.ensnif.lanime.domain.animation.entity.AirDay
import com.ensnif.lanime.domain.animation.entity.AnimationStatus
import com.ensnif.lanime.domain.animation.entity.AnimationType
import com.ensnif.lanime.domain.animation.entity.Genre
import com.ensnif.lanime.domain.animation.entity.RankingType
import com.ensnif.lanime.domain.animation.service.AnimationService
import com.ensnif.lanime.domain.episode.dto.EpisodeResponse
import com.ensnif.lanime.domain.episode.service.EpisodeService
import com.ensnif.lanime.domain.social.service.ReviewService
import com.ensnif.lanime.global.common.dto.ApiResponse
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import com.ensnif.lanime.global.util.LocaleUtils
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/animations")
class AnimationController(
    private val animationService: AnimationService,
    private val reviewService: ReviewService,
    private val episodeService: EpisodeService
) {

    @GetMapping
    fun getAllAnimations(
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?,
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) typeIds: List<UUID>?,
        @RequestParam(required = false) status: AnimationStatus?,
        @RequestParam(required = false) genreIds: List<UUID>?,
        @RequestParam(required = false) startYear: Int?,
        @RequestParam(required = false) endYear: Int?,
        @RequestParam(required = false) userAge: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "30") limit: Int
    ): Mono<ApiResponse<List<AnimationListResponse>>> {
        val locale = LocaleUtils.parse(acceptLanguage)
        return animationService.getAllAnimations(query, typeIds, status, genreIds, startYear, endYear, userAge, page, limit, locale)
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/types")
    fun getAnimationTypes(): Mono<ApiResponse<List<AnimationType>>> {
        return animationService.getAnimationTypes()
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/genres")
    fun getGenres(): Mono<ApiResponse<List<Genre>>> {
        return animationService.getGenres()
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/rankings")
    fun getAnimationRankings(
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?,
        @RequestParam type: RankingType,
        @RequestParam(required = false) userAge: Int?
    ): Mono<ApiResponse<List<AnimationRankingResponse>>> {
        val locale = LocaleUtils.parse(acceptLanguage)
        return animationService.getAnimationRankings(type, userAge, locale)
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/weekly")
    fun getWeeklyAnimations(
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?,
        @RequestParam(required = false) airDay: AirDay?,
        @RequestParam(required = false) userAge: Int?
    ): Mono<ApiResponse<out Any>> {
        val locale = LocaleUtils.parse(acceptLanguage)
        return if (airDay != null) {
            animationService.getAnimationsByAirDay(airDay, userAge, locale)
                .collectList()
                .map { ApiResponse.success(it) }
        } else {
            animationService.getWeeklyAnimations(userAge, locale)
                .map { ApiResponse.success(it) }
        }
    }

    @GetMapping("/{animationId}")
    fun getAnimationDetail(
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?,
        @PathVariable animationId: UUID,
        @AuthenticationPrincipal context: UserProfileContext?
    ): Mono<ApiResponse<AnimationDetailResponse>> {
        val locale = LocaleUtils.parse(acceptLanguage)
        return animationService.getAnimationDetail(animationId, context?.profileId, locale)
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/{animationId}/ratings")
    fun getAnimationRatings(
        @PathVariable animationId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @AuthenticationPrincipal context: UserProfileContext?
    ): Mono<ApiResponse<AnimationReviewRatingsResponse>> {
        val profileId = context?.profileId
        return animationService.getAnimationRatings(animationId, page, limit, profileId)
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/{animationId}/episodes")
    fun getEpisodes(
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?,
        @PathVariable animationId: UUID,
        @AuthenticationPrincipal context: UserProfileContext?
    ): Mono<ApiResponse<List<EpisodeResponse>>> {
        val locale = LocaleUtils.parse(acceptLanguage)
        return episodeService.getEpisodes(animationId, context?.profileId, locale)
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/{animationId}/similar")
    fun getSimilarAnimations(
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?,
        @PathVariable animationId: UUID,
        @RequestParam(required = false, defaultValue = "50") matchPercentage: Int,
        @RequestParam(required = false) userAge: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") limit: Int
    ): Mono<ApiResponse<List<AnimationListResponse>>> {
        val locale = LocaleUtils.parse(acceptLanguage)
        return animationService.getSimilarAnimations(animationId, matchPercentage, userAge, locale, page, limit)
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @PostMapping("/{animationId}/ratings")
    fun createReview(
        @PathVariable animationId: UUID,
        @AuthenticationPrincipal context: UserProfileContext,
        @Valid @RequestBody request: CreateReviewRequest
    ): Mono<ApiResponse<Unit>> {
        val profileId = context.profileId
            ?: throw BusinessException(ErrorCode.FORBIDDEN)

        return reviewService.createReview(animationId, profileId, request)
            .then(Mono.just(ApiResponse.withMessage("리뷰가 등록되었습니다.")))
    }

    @DeleteMapping("/{animationId}/ratings")
    fun deleteReview(
        @PathVariable animationId: UUID,
        @AuthenticationPrincipal context: UserProfileContext
    ): Mono<ApiResponse<Unit>> {
        val profileId = context.profileId
            ?: throw BusinessException(ErrorCode.FORBIDDEN)

        return reviewService.deleteReview(animationId, profileId)
            .then(Mono.just(ApiResponse.withMessage("리뷰가 삭제되었습니다.")))
    }

    @PatchMapping("/{animationId}/ratings")
    fun updateReview(
        @PathVariable animationId: UUID,
        @AuthenticationPrincipal context: UserProfileContext,
        @Valid @RequestBody request: CreateReviewRequest
    ): Mono<ApiResponse<Unit>> {
        val profileId = context.profileId
            ?: throw BusinessException(ErrorCode.FORBIDDEN)

        return reviewService.updateReview(animationId, profileId, request)
            .then(Mono.just(ApiResponse.withMessage("리뷰가 수정되었습니다.")))
    }
}
