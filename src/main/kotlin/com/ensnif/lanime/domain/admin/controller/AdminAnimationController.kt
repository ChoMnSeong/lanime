package com.ensnif.lanime.domain.admin.controller

import com.ensnif.lanime.domain.admin.dto.request.*
import com.ensnif.lanime.domain.admin.dto.response.ImportAnimationResponse
import com.ensnif.lanime.domain.admin.service.AdminAnimationService
import com.ensnif.lanime.domain.admin.service.AdminEpisodeService
import com.ensnif.lanime.domain.admin.service.AnimationImportService
import com.ensnif.lanime.domain.admin.service.ScheduledImportService
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
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin")
class AdminAnimationController(
    private val adminAnimationService: AdminAnimationService,
    private val adminEpisodeService: AdminEpisodeService,
    private val animationImportService: AnimationImportService,
    private val scheduledImportService: ScheduledImportService
) {

    // ─── 외부 데이터 임포트 ───────────────────────────────────────────────

    @PostMapping("/animations/import")
    fun importAnimation(
        @AuthenticationPrincipal context: UserProfileContext,
        @Valid @RequestBody request: ImportAnimationRequest
    ): Mono<ApiResponse<ImportAnimationResponse>> {
        return animationImportService.importAnimation(request, context)
            .map { ApiResponse.success(it) }
    }

    @PostMapping("/animations/import/bulk-reset")
    fun bulkReset(
        @AuthenticationPrincipal context: UserProfileContext,
        @RequestParam(required = false) startYear: Int?,
        @RequestParam(required = false, defaultValue = "2000") endYear: Int
    ): Mono<ApiResponse<Map<String, Any>>> {
        if (!context.isAdmin) return Mono.just(ApiResponse.success(mapOf("error" to "forbidden")))
        val start = startYear ?: LocalDate.now().year
        scheduledImportService.clearAndBulkImport(startYear = start, endYear = endYear)
        return Mono.just(ApiResponse.success(mapOf(
            "message" to "벌크 임포트가 백그라운드에서 시작되었습니다. 서버 로그를 확인하세요.",
            "startYear" to start,
            "endYear" to endYear
        )))
    }

    @PostMapping("/animations/import/season")
    fun importSeason(
        @AuthenticationPrincipal context: UserProfileContext,
        @RequestParam(required = false) season: String?,
        @RequestParam(required = false) year: Int?
    ): Mono<ApiResponse<Map<String, Any>>> {
        if (!context.isAdmin) return Mono.just(ApiResponse.success(mapOf("error" to "forbidden")))
        val (currentSeason, currentYear) = scheduledImportService.currentSeason()
        val targetSeason = season?.uppercase() ?: currentSeason
        val targetYear = year ?: currentYear
        return scheduledImportService.importSeason(targetSeason, targetYear)
            .map { results ->
                ApiResponse.success(mapOf(
                    "season" to "$targetSeason $targetYear",
                    "imported" to results.size,
                    "animations" to results
                ))
            }
    }

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

    // ─── 번역 관리 ────────────────────────────────────────────────────

    @PutMapping("/animations/{animationId}/translations/{locale}")
    fun upsertAnimationTranslation(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable animationId: UUID,
        @PathVariable locale: String,
        @Valid @RequestBody request: UpsertAnimationTranslationRequest
    ): Mono<ApiResponse<Unit>> {
        return adminAnimationService.upsertAnimationTranslation(animationId, locale, request, context)
            .map { ApiResponse.withMessage("번역이 저장되었습니다.") }
    }

    @PutMapping("/episodes/{episodeId}/translations/{locale}")
    fun upsertEpisodeTranslation(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable episodeId: UUID,
        @PathVariable locale: String,
        @Valid @RequestBody request: UpsertEpisodeTranslationRequest
    ): Mono<ApiResponse<Unit>> {
        return adminAnimationService.upsertEpisodeTranslation(episodeId, locale, request, context)
            .map { ApiResponse.withMessage("번역이 저장되었습니다.") }
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

    @PatchMapping("/animations/{animationId}/episodes/thumbnails")
    fun syncEpisodeThumbnails(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable animationId: UUID
    ): Mono<ApiResponse<Unit>> {
        return adminEpisodeService.syncEpisodeThumbnails(animationId, context)
            .map { count -> ApiResponse.withMessage("에피소드 썸네일 ${count}개를 업데이트했습니다.") }
    }
}
