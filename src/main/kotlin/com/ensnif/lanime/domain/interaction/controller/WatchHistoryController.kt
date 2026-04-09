package com.ensnif.lanime.domain.interaction.controller

import com.ensnif.lanime.domain.interaction.dto.UpdateWatchProgressRequest
import com.ensnif.lanime.domain.interaction.service.WatchHistoryService
import com.ensnif.lanime.global.common.dto.ApiResponse
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import com.ensnif.lanime.global.util.LocaleUtils
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/watch-history")
class WatchHistoryController(
    private val watchHistoryService: WatchHistoryService
) {

    @PutMapping("/{episodeId}")
    fun updateWatchProgress(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable episodeId: UUID,
        @RequestBody request: UpdateWatchProgressRequest
    ): Mono<ApiResponse<Unit>> {
        val profileId = context.profileId
            ?: throw BusinessException(ErrorCode.FORBIDDEN)

        return watchHistoryService.updateWatchProgress(profileId, episodeId, request)
            .then(Mono.just(ApiResponse.withMessage("시청 기록이 저장되었습니다.")))
    }

    @GetMapping
    fun getWatchHistory(
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?,
        @AuthenticationPrincipal context: UserProfileContext,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): Mono<ApiResponse<Map<String, Any>>> {
        val profileId = context.profileId
            ?: throw BusinessException(ErrorCode.FORBIDDEN)
        val locale = LocaleUtils.parse(acceptLanguage)

        return watchHistoryService.getWatchHistory(profileId, page, limit, locale)
            .map { ApiResponse.success(it) }
    }
}
