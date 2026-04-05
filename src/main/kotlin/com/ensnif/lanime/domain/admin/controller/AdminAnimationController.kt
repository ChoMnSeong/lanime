package com.ensnif.lanime.domain.admin.controller

import com.ensnif.lanime.domain.admin.dto.request.*
import com.ensnif.lanime.domain.admin.service.AdminAnimationService
import com.ensnif.lanime.domain.admin.service.AdminEpisodeService
import com.ensnif.lanime.domain.animation.entity.Animation
import com.ensnif.lanime.domain.animation.entity.AnimationType
import com.ensnif.lanime.domain.animation.entity.Genre
import com.ensnif.lanime.domain.episode.entity.Episode
import com.ensnif.lanime.global.common.dto.ApiResponse
import com.ensnif.lanime.global.context.UserProfileContext
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin")
class AdminAnimationController(
    private val adminAnimationService: AdminAnimationService,
    private val adminEpisodeService: AdminEpisodeService
) {

    // ─── 애니메이션 CRUD ───────────────────────────────────────────────

    @PostMapping("/animations")
    fun createAnimation(
        @AuthenticationPrincipal context: UserProfileContext,
        @Valid @RequestBody request: CreateAnimationRequest
    ): Mono<ApiResponse<Animation>> {
        return adminAnimationService.createAnimation(request, context).map { ApiResponse.success(it) }
    }

    @PostMapping("/animations/types")
    fun createAnimationType(
        @AuthenticationPrincipal context: UserProfileContext,
        @Valid @RequestBody request: CreateAnimationTypeRequest
    ): Mono<ApiResponse<AnimationType>> {
        return adminAnimationService.createAnimationType(request, context).map { ApiResponse.success(it) }
    }

    @PostMapping("/animations/genres")
    fun createGenre(
        @AuthenticationPrincipal context: UserProfileContext,
        @Valid @RequestBody request: CreateGenreRequest
    ): Mono<ApiResponse<Genre>> {
        return adminAnimationService.createGenre(request, context).map { ApiResponse.success(it) }
    }

    @PatchMapping("/animations/{animationId}")
    fun updateAnimation(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable animationId: UUID,
        @RequestBody request: UpdateAnimationRequest
    ): Mono<ApiResponse<Animation>> {
        return adminAnimationService.updateAnimation(animationId, request, context).map { ApiResponse.success(it) }
    }

    @DeleteMapping("/animations/{animationId}")
    fun deleteAnimation(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable animationId: UUID
    ): Mono<ApiResponse<Unit>> {
        return adminAnimationService.deleteAnimation(animationId, context)
            .map { ApiResponse.withMessage("애니메이션이 삭제되었습니다.") }
    }

    // ─── 에피소드 CRUD ────────────────────────────────────────────────

    @PostMapping("/animations/{animationId}/episodes")
    fun createEpisode(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable animationId: UUID,
        @Valid @RequestBody request: CreateEpisodeRequest
    ): Mono<ApiResponse<Episode>> {
        return adminEpisodeService.createEpisode(animationId, request, context).map { ApiResponse.success(it) }
    }

    @PatchMapping("/episodes/{episodeId}")
    fun updateEpisode(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable episodeId: UUID,
        @RequestBody request: UpdateEpisodeRequest
    ): Mono<ApiResponse<Episode>> {
        return adminEpisodeService.updateEpisode(episodeId, request, context).map { ApiResponse.success(it) }
    }

    @DeleteMapping("/episodes/{episodeId}")
    fun deleteEpisode(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable episodeId: UUID
    ): Mono<ApiResponse<Unit>> {
        return adminEpisodeService.deleteEpisode(episodeId, context)
            .map { ApiResponse.withMessage("에피소드가 삭제되었습니다.") }
    }
}
