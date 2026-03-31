package com.ensnif.lanime.domain.user.controller

import com.ensnif.lanime.global.common.dto.ApiResponse
import com.ensnif.lanime.domain.user.dto.request.*
import com.ensnif.lanime.domain.user.dto.response.*
import com.ensnif.lanime.domain.user.service.AuthService
import com.ensnif.lanime.global.context.UserProfileContext
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    // 이메일 중복 체크
    @PostMapping("/check-email")
    fun checkEmail(@Valid @RequestBody request: EmailCheckRequest): Mono<ApiResponse<EmailCheckResponse>> {
        return authService.checkEmail(request.email)
            .map { ApiResponse.success(it) }
    }

    // 인증 메일 발송
    @PostMapping("/send-verification")
    fun sendVerification(@Valid @RequestBody request: VerificationSendRequest): Mono<ApiResponse<Unit>> {
        return authService.sendVerificationCode(request.email)
            .thenReturn(ApiResponse.withMessage("인증 번호가 이메일로 발송되었습니다."))
    }

    // 코드 검증
    @PostMapping("/verify-code")
    fun verifyCode(@Valid @RequestBody request: VerificationRequest): Mono<ApiResponse<Boolean>> {
        return authService.verifyCode(request.email, request.code)
            .map { ApiResponse.success(it) }
    }

    // 최종 회원가입
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): Mono<ApiResponse<Unit>> {
        return authService.signup(request)
            .thenReturn(ApiResponse.withMessage("회원가입에 성공했습니다."))
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): Mono<ApiResponse<AuthResponse>> {
        return authService.refreshAccessToken(request.refreshToken)
            .map { ApiResponse.success(it) }
    }

    @PostMapping("/signin")
    fun signin(@Valid @RequestBody request: SigninRequest): Mono<ApiResponse<AuthResponse>> {
        return authService.signin(request)
            .map { authResponse ->
                ApiResponse.success(authResponse)
            }
    }

    // ─── 계정 관리 ────────────────────────────────────────────────────────

    @DeleteMapping("/account")
    fun deleteAccount(
        @AuthenticationPrincipal context: UserProfileContext
    ): Mono<ApiResponse<Unit>> {
        return authService.deleteAccount(context.email)
            .thenReturn(ApiResponse.withMessage("계정이 삭제되었습니다."))
    }

    @PostMapping("/account/email/send-verification")
    fun sendEmailChangeVerification(
        @AuthenticationPrincipal context: UserProfileContext,
        @Valid @RequestBody request: VerificationSendRequest
    ): Mono<ApiResponse<Unit>> {
        return authService.sendEmailChangeVerification(request.email)
            .thenReturn(ApiResponse.withMessage("인증 번호가 이메일로 발송되었습니다."))
    }

    @PatchMapping("/account/email")
    fun updateEmail(
        @AuthenticationPrincipal context: UserProfileContext,
        @Valid @RequestBody request: AdminUpdateEmailRequest
    ): Mono<ApiResponse<Unit>> {
        return authService.updateEmail(context.email, request.newEmail, request.verificationCode)
            .thenReturn(ApiResponse.withMessage("이메일이 변경되었습니다."))
    }

    @PatchMapping("/account/password")
    fun updatePassword(
        @AuthenticationPrincipal context: UserProfileContext,
        @Valid @RequestBody request: AdminResetPasswordRequest
    ): Mono<ApiResponse<Unit>> {
        return authService.updatePassword(context.email, request.currentPassword, request.newPassword)
            .thenReturn(ApiResponse.withMessage("비밀번호가 변경되었습니다."))
    }

    // 비밀번호 재설정 코드 발송
    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): Mono<ApiResponse<Unit>> {
        return authService.sendPasswordResetCode(request.email)
            .thenReturn(ApiResponse.withMessage("비밀번호 재설정 코드가 이메일로 발송되었습니다."))
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): Mono<ApiResponse<Unit>> {
        return authService.resetPassword(request)
            .thenReturn(ApiResponse.withMessage("비밀번호가 변경되었습니다."))
    }
}
