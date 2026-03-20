package com.ensnif.lanime.domain.user.controller

import com.ensnif.lanime.global.common.dto.ApiResponse
import com.ensnif.lanime.domain.user.dto.request.*
import com.ensnif.lanime.domain.user.dto.response.*
import com.ensnif.lanime.domain.user.service.AuthService
import jakarta.validation.Valid
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

    @PostMapping("/signin")
    fun signin(@Valid @RequestBody request: SigninRequest): Mono<ApiResponse<AuthResponse>> {
        return authService.signin(request)
            .map { authResponse -> 
                ApiResponse.success(authResponse) 
            }
    }
}
