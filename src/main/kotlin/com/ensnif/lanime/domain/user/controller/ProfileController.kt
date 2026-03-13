package com.ensnif.lanime.domain.user.controller

import com.ensnif.lanime.domain.user.dto.request.*
import com.ensnif.lanime.domain.user.dto.response.*
import com.ensnif.lanime.domain.user.service.ProfileService
import com.ensnif.lanime.global.common.ApiResponse
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import com.ensnif.lanime.global.context.UserProfileContext
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/profiles")
class ProfileController(
    private val profileService: ProfileService
) {

    /**
     * 프로필 선택 시도 (PIN 필요 여부 체크)
     */
    @PostMapping("/{profileId}/access")
    fun checkAccess(    
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable profileId: UUID
    ): Mono<ApiResponse<ProfileAccessResponse>> {
        return profileService.checkProfileAccess(context.email, profileId)
            .map { ApiResponse.success(it) }
    }

    /**
     * 프로필 PIN 검증 및 영구 토큰 발급
     */
    @PostMapping("/{profileId}/verify")
    fun verifyPin(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable profileId: UUID,
        @RequestBody request: ProfilePinRequest
    ): Mono<ApiResponse<ProfileAccessResponse>> {
        return profileService.verifyPinAndGetToken(context.email, profileId, request.pin)
            .map { token ->
                ApiResponse.success(ProfileAccessResponse(
                    isPasswordRequired = false,
                    profileToken = token
                ))
            }
    }
}
