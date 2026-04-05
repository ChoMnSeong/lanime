package com.ensnif.lanime.domain.animation.controller

import com.ensnif.lanime.domain.animation.dto.request.*
import com.ensnif.lanime.domain.animation.dto.response.*
import com.ensnif.lanime.domain.animation.entity.AirDay
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
    fun getAllAnimations(): Mono<ApiResponse<List<AnimationListResponse>>> {
        return animationService.getAllAnimations()
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
        @RequestParam type: RankingType
    ): Mono<ApiResponse<List<AnimationRankingResponse>>> {
        return animationService.getAnimationRankings(type)
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/weekly")
    fun getWeeklyAnimations(
        @RequestParam(required = false) airDay: AirDay?
    ): Mono<ApiResponse<out Any>> {
        return if (airDay != null) {
            animationService.getAnimationsByAirDay(airDay)
                .collectList()
                .map { ApiResponse.success(it) }
        } else {
            animationService.getWeeklyAnimations()
                .map { ApiResponse.success(it) }
        }
    }

    @GetMapping("/{animationId}")
    fun getAnimationDetail(
        @PathVariable animationId: UUID
    ): Mono<ApiResponse<AnimationDetailResponse>> {
        return animationService.getAnimationDetail(animationId)
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
        @PathVariable animationId: UUID,
        @AuthenticationPrincipal context: UserProfileContext?
    ): Mono<ApiResponse<List<EpisodeResponse>>> {
        return episodeService.getEpisodes(animationId, context?.profileId)
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
