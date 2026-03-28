package com.ensnif.lanime.domain.user.service

import com.ensnif.lanime.domain.user.entity.User
import com.ensnif.lanime.domain.user.entity.UserProfile
import com.ensnif.lanime.domain.user.dto.request.*
import com.ensnif.lanime.domain.user.dto.response.*
import com.ensnif.lanime.domain.user.repository.UserRepository
import com.ensnif.lanime.domain.user.repository.UserProfileRepository
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import com.ensnif.lanime.global.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val verificationService: VerificationService,
    private val emailService: EmailService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    /**
     * 이메일 중복 체크
     */
    fun checkEmail(email: String): Mono<EmailCheckResponse> {
        return userRepository.existsByEmail(email)
            .map { isRegistered -> EmailCheckResponse(email, isRegistered) }
    }

    /**
     * 인증 코드 발송
     */
    fun sendVerificationCode(email: String): Mono<Unit> {
        return verificationService.saveCode(email) // 1. 여기서 Mono<String> (코드)이 나옴
            .flatMap { code -> 
                // 2. 넘어온 코드를 사용하여 메일 발송
                emailService.sendVerificationMail(email, code) 
            }
            .thenReturn(Unit)
    }

    /**
     * 인증 코드 검증
     */
    fun verifyCode(email: String, code: String): Mono<Boolean> {
        return verificationService.verifyCode(email, code)
            .flatMap { isValid ->
                if (!isValid) {
                    Mono.error(BusinessException(ErrorCode.INVALID_INPUT_VALUE))
                } else {
                    Mono.just(true)
                }
            }
    }

    @Transactional
    fun signup(request: SignupRequest): Mono<Unit> {
        // [변경] 코드를 직접 검증하는 대신, 이미 검증된 상태인지 확인하고 레코드를 삭제합니다.
        return verificationService.confirmAndClear(request.email)
            .flatMap { isVerified ->
                if (!isVerified) {
                    return@flatMap Mono.error(BusinessException(ErrorCode.EMAIL_NOT_VERIFIED)) // 인증 안 됨
                }
                userRepository.existsByEmail(request.email)
            }
            .flatMap { isExist ->
                if (isExist) return@flatMap Mono.error(BusinessException(ErrorCode.EMAIL_DUPLICATION))
                
                // User 생성 로직 (이전과 동일)
                val encodedPassword = passwordEncoder.encode(request.password) as String
                userRepository.save(User(
                    email = request.email,
                    password = encodedPassword,
                    isActive = true
                ))
            }
            .flatMap { savedUser ->
                // UserProfile 생성 및 응답 반환 (이전과 동일)
                userProfileRepository.save(UserProfile(
                    userId = savedUser.userId!!,
                    name = request.nickname,
                    avatarUrl = "http://localhost:8080/ec6acdc61ea087fbd502e95973af28e6768a56722ba45d69c84f95e26baff139.jpg",
                    isAdmin = true
                ))
            }
            .thenReturn(Unit)
    }

    /**
     * 비밀번호 재설정 코드 발송
     */
    fun sendPasswordResetCode(email: String): Mono<Unit> {
        return userRepository.existsByEmail(email)
            .flatMap { exists ->
                if (!exists) Mono.error(BusinessException(ErrorCode.USER_NOT_FOUND))
                else verificationService.saveResetToken(email)
            }
            .flatMap { token -> emailService.sendPasswordResetMail(email, token) }
            .thenReturn(Unit)
    }

    /**
     * 비밀번호 재설정
     */
    @Transactional
    fun resetPassword(request: ResetPasswordRequest): Mono<Unit> {
        return verificationService.verifyAndClearResetToken(request.email, request.token)
            .flatMap { isValid ->
                if (!isValid) Mono.error(BusinessException(ErrorCode.INVALID_INPUT_VALUE))
                else userRepository.findByEmail(request.email)
                    .switchIfEmpty(Mono.error(BusinessException(ErrorCode.USER_NOT_FOUND)))
            }
            .flatMap { user ->
                userRepository.save(
                    user.copy(password = passwordEncoder.encode(request.newPassword)!!)
                        .apply { createdAt = user.createdAt }
                )
            }
            .thenReturn(Unit)
    }

    @Transactional
    fun deleteAccount(email: String): Mono<Unit> {
        return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.USER_NOT_FOUND)))
            .flatMap { user -> userRepository.deleteById(user.userId!!) }
            .thenReturn(Unit)
    }

    fun sendEmailChangeVerification(newEmail: String): Mono<Unit> {
        return userRepository.existsByEmail(newEmail)
            .flatMap { exists ->
                if (exists) Mono.error(BusinessException(ErrorCode.EMAIL_DUPLICATION))
                else verificationService.saveCode(newEmail)
            }
            .flatMap { code -> emailService.sendVerificationMail(newEmail, code) }
            .thenReturn(Unit)
    }

    @Transactional
    fun updateEmail(email: String, newEmail: String, verificationCode: String): Mono<Unit> {
        return verificationService.confirmAndClear(newEmail)
            .flatMap { isVerified ->
                if (!isVerified) Mono.error(BusinessException(ErrorCode.EMAIL_NOT_VERIFIED))
                else userRepository.existsByEmail(newEmail)
            }
            .flatMap { exists ->
                if (exists) Mono.error(BusinessException(ErrorCode.EMAIL_DUPLICATION))
                else userRepository.findByEmail(email)
                    .switchIfEmpty(Mono.error(BusinessException(ErrorCode.USER_NOT_FOUND)))
            }
            .flatMap { user ->
                userRepository.save(
                    user.copy(email = newEmail)
                        .apply { createdAt = user.createdAt }
                )
            }
            .thenReturn(Unit)
    }

    @Transactional
    fun updatePassword(email: String, currentPassword: String, newPassword: String): Mono<Unit> {
        return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.USER_NOT_FOUND)))
            .flatMap { user ->
                if (!passwordEncoder.matches(currentPassword, user.password)) {
                    Mono.error(BusinessException(ErrorCode.INVALID_INPUT_VALUE))
                } else {
                    userRepository.save(
                        user.copy(password = passwordEncoder.encode(newPassword)!!)
                            .apply { createdAt = user.createdAt }
                    )
                }
            }
            .thenReturn(Unit)
    }

    fun signin(request: SigninRequest): Mono<AuthResponse> {
        return userRepository.findByEmail(request.email)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.USER_NOT_FOUND)))
            .flatMap { user ->
                // 1. 비밀번호 검증
                if (!passwordEncoder.matches(request.password, user.password)) {
                    return@flatMap Mono.error(BusinessException(ErrorCode.INVALID_INPUT_VALUE))
                }
                
                // 2. 인증 성공 시 바로 토큰 응답 생성
                Mono.just(
                    AuthResponse(
                        accessToken = jwtTokenProvider.createAccessToken(user.email),
                        refreshToken = jwtTokenProvider.createRefreshToken(user.email),
                        expiresIn = jwtTokenProvider.getExpirationSeconds()
                    )
                )
            }
    }
}