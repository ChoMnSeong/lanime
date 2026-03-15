package com.ensnif.lanime.domain.user.controller

import com.ensnif.lanime.domain.user.dto.request.*
import com.ensnif.lanime.domain.user.dto.response.*
import com.ensnif.lanime.domain.user.entity.UserProfile
import com.ensnif.lanime.domain.user.service.ProfileService
import com.ensnif.lanime.global.common.ApiResponse
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import com.ensnif.lanime.global.context.UserProfileContext
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.util.UUID

@RestController
@RequestMapping("/api/v1/profiles")
class ProfileController(
    private val profileService: ProfileService
) {

    @GetMapping("")
    fun getUserProfiles(@AuthenticationPrincipal context: UserProfileContext): Flux<UserProfile> {
        return profileService.getUserProfiles(context.email);
    }

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

    /**
     * 프로필 정보 수정
     */
    @PostMapping("")
    fun createProfile(
        // 필터에서 이미 이메일과 프로필ID를 검증해서 context에 넣어두었습니다.
        @AuthenticationPrincipal context: UserProfileContext,
        @RequestBody request: ProfileCreateRequest
    ): Mono<ApiResponse<Unit>> {

        return profileService.createProfile(context.email, request)
            .then(Mono.just(ApiResponse.success("프로필이 추가되었습니다.")))
    }

    /**
     * 프로필 정보 수정
     */
    @PatchMapping("/self")
    fun updateProfile(
        // 필터에서 이미 이메일과 프로필ID를 검증해서 context에 넣어두었습니다.
        @AuthenticationPrincipal context: UserProfileContext,
        @RequestBody request: ProfileUpdateRequest
    ): Mono<ApiResponse<Unit>> {
        
        // context에서 profileId를 추출 (null 체크 포함)
        val profileId = context.profileId 
            ?: throw BusinessException(ErrorCode.FORBIDDEN) // 또는 프로필 미선택 에러

        return profileService.updateProfile(context.email, profileId, request)
            .then(Mono.just(ApiResponse.success("프로필이 수정되었습니다.")))
    }
}
