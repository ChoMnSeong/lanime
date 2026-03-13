package com.ensnif.lanime.domain.user.controller

import com.ensnif.lanime.domain.user.dto.request.ProfilePinRequest
import com.ensnif.lanime.domain.user.dto.response.ProfileAccessResponse
import com.ensnif.lanime.domain.user.service.ProfileService
import com.ensnif.lanime.global.common.ApiResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

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
        @AuthenticationPrincipal email: String, // SecurityContext에서 추출된 이메일
        @PathVariable profileId: Long
    ): Mono<ApiResponse<ProfileAccessResponse>> {
        return profileService.checkProfileAccess(email, profileId)
            .map { ApiResponse.success(it) }
    }

    /**
     * 프로필 PIN 검증 및 영구 토큰 발급
     */
    @PostMapping("/{profileId}/verify")
    fun verifyPin(
        @AuthenticationPrincipal email: String,
        @PathVariable profileId: Long,
        @RequestBody request: ProfilePinRequest
    ): Mono<ApiResponse<ProfileAccessResponse>> {
        return profileService.verifyPinAndGetToken(email, profileId, request.pin)
            .map { token ->
                ApiResponse.success(ProfileAccessResponse(
                    isPasswordRequired = false,
                    profileToken = token
                ))
            }
    }
}
