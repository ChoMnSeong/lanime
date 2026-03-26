package com.ensnif.lanime.domain.animation.controller

import com.ensnif.lanime.domain.animation.dto.response.AnimationDetailResponse
import com.ensnif.lanime.domain.animation.dto.response.AnimationListResponse
import com.ensnif.lanime.domain.animation.dto.response.AnimationReviewRatingsResponse
import com.ensnif.lanime.domain.animation.service.AnimationService
import com.ensnif.lanime.global.common.dto.ApiResponse
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/animations")
class AnimationController(private val animationService: AnimationService) {

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
        @RequestParam(defaultValue = "20") limit: Int
    ): Mono<ApiResponse<AnimationReviewRatingsResponse>> {
        return animationService.getAnimationRatings(animationId, page, limit)
            .map { ApiResponse.success(it) }
    }
}
