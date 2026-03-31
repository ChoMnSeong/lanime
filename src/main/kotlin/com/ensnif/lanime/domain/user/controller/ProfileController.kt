package com.ensnif.lanime.domain.user.controller

import com.ensnif.lanime.domain.user.dto.request.*
import com.ensnif.lanime.domain.user.dto.response.*
import com.ensnif.lanime.domain.user.entity.UserProfile
import com.ensnif.lanime.domain.user.service.ProfileService
import com.ensnif.lanime.global.common.dto.ApiResponse
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
    fun getUserProfiles(@AuthenticationPrincipal context: UserProfileContext): Mono<ApiResponse<List<UserProfile>>> {
        return profileService.getUserProfiles(context.email)
            .collectList() // Flux<UserProfile>를 Mono<List<UserProfile>>로 변환
            .map { profiles -> ApiResponse.success(profiles) } // 리스트 전체를 딱 한 번 감쌈
    }

    /**
     * 현재 선택된 프로필 정보 조회 (유저 토큰 + 프로필 토큰 필요)
     */
    @GetMapping("/self")
    fun getMyProfile(@AuthenticationPrincipal context: UserProfileContext): Mono<ApiResponse<ProfileInfoResponse>> {
        val profileId = context.profileId
            ?: throw BusinessException(ErrorCode.FORBIDDEN)

        return profileService.getMyProfile(context.email, profileId)
            .map { ApiResponse.success(it) }
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
            .then(Mono.just(ApiResponse.withMessage("프로필이 추가되었습니다.")))
    }

    /**
     * 프로필 PIN 초기화 (PIN을 잊어버렸을 때 - 계정 비밀번호 인증 필요)
     */
    @DeleteMapping("/{profileId}/pin")
    fun resetPin(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable profileId: UUID,
        @RequestBody request: ResetPinRequest
    ): Mono<ApiResponse<Unit>> {
        return profileService.resetPin(context.email, profileId, request.password)
            .then(Mono.just(ApiResponse.withMessage("PIN이 초기화되었습니다.")))
    }

    /**
     * 프로필 삭제 (관리자 프로필 제외)
     */
    @DeleteMapping("/{profileId}")
    fun deleteProfile(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable profileId: UUID
    ): Mono<ApiResponse<Unit>> {
        return profileService.deleteProfile(context.email, profileId, context.isAdmin)
            .then(Mono.just(ApiResponse.withMessage("프로필이 삭제되었습니다.")))
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
            .then(Mono.just(ApiResponse.withMessage("프로필이 수정되었습니다.")))
    }
}
