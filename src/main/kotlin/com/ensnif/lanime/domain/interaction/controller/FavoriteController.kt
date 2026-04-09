package com.ensnif.lanime.domain.interaction.controller

import com.ensnif.lanime.domain.interaction.service.FavoriteService
import com.ensnif.lanime.global.common.dto.ApiResponse
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
class FavoriteController(
    private val favoriteService: FavoriteService
) {

    @PostMapping("/api/v1/animations/{animationId}/favorite")
    fun addFavorite(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable animationId: UUID
    ): Mono<ApiResponse<Unit>> {
        val profileId = context.profileId
            ?: throw BusinessException(ErrorCode.FORBIDDEN)

        return favoriteService.addFavorite(profileId, animationId)
            .then(Mono.just(ApiResponse.withMessage("좋아요가 등록되었습니다.")))
    }

    @DeleteMapping("/api/v1/animations/{animationId}/favorite")
    fun removeFavorite(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable animationId: UUID
    ): Mono<ApiResponse<Unit>> {
        val profileId = context.profileId
            ?: throw BusinessException(ErrorCode.FORBIDDEN)

        return favoriteService.removeFavorite(profileId, animationId)
            .then(Mono.just(ApiResponse.withMessage("좋아요가 취소되었습니다.")))
    }

    @GetMapping("/api/v1/favorites")
    fun getFavorites(
        @AuthenticationPrincipal context: UserProfileContext,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): Mono<ApiResponse<Map<String, Any>>> {
        val profileId = context.profileId
            ?: throw BusinessException(ErrorCode.FORBIDDEN)

        return favoriteService.getFavorites(profileId, page, limit)
            .map { ApiResponse.success(it) }
    }
}
