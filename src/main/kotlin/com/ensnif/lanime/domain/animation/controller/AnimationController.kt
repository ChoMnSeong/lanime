package com.ensnif.lanime.domain.animation.controller

import com.ensnif.lanime.domain.animation.dto.request.CreateReviewRequest
import com.ensnif.lanime.domain.animation.dto.response.AnimationDetailResponse
import com.ensnif.lanime.domain.animation.dto.response.AnimationListResponse
import com.ensnif.lanime.domain.animation.dto.response.AnimationReviewRatingsResponse
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

    @GetMapping("/weekly")
    fun getWeeklyAnimations(
        @RequestParam(required = false) airDay: String?
    ): Mono<ApiResponse<List<AnimationListResponse>>> {
        return animationService.getWeeklyAnimations(airDay)
            .collectList()
            .map { ApiResponse.success(it) }
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
