package com.ensnif.lanime.domain.user.service

import com.ensnif.lanime.domain.user.entity.UserProfile
import com.ensnif.lanime.domain.user.dto.request.ProfileUpdateRequest
import com.ensnif.lanime.domain.user.dto.response.ProfileAccessResponse
import com.ensnif.lanime.domain.user.repository.UserProfileRepository
import com.ensnif.lanime.domain.user.repository.UserRepository
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import com.ensnif.lanime.global.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class ProfileService(
    private val userProfileRepository: UserProfileRepository,
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional(readOnly = true)
    fun checkProfileAccess(email: String, profileId: UUID): Mono<ProfileAccessResponse> {
        return validateOwnerAndGetProfile(email, profileId)
            .map { profile ->
                if (profile.pin.isNullOrBlank()) {
                    // PIN 없으면 즉시 만료 없는 토큰 발급 (Subject는 email)
                    val token = jwtTokenProvider.createProfileToken(email, profile.profileId!!, profile.isAdmin)
                    ProfileAccessResponse(isPasswordRequired = false, profileToken = token)
                } else {
                    ProfileAccessResponse(isPasswordRequired = true)
                }
            }
    }

    @Transactional(readOnly = true)
    fun verifyPinAndGetToken(email: String, profileId: UUID, rawPin: String): Mono<String> {
        return validateOwnerAndGetProfile(email, profileId)
            .flatMap { profile ->
                if (profile.pin == null || !passwordEncoder.matches(rawPin, profile.pin)) {
                    Mono.error(BusinessException(ErrorCode.INVALID_INPUT_VALUE))
                } else {
                    // 검증 성공 시 영구 토큰 발급
                    Mono.just(jwtTokenProvider.createProfileToken(email, profile.profileId!!, profile.isAdmin))
                }
            }
    }

    @Transactional
    fun updateProfile(email: String, profileId: UUID, request: ProfileUpdateRequest): Mono<Unit> {
        return validateOwnerAndGetProfile(email, profileId)
            .flatMap { profile ->

                profile.name = request.name ?: profile.name
                profile.avatarUrl = request.avatarUrl ?: profile.avatarUrl
                profile.pin = request.pin?.let { passwordEncoder.encode(it) } ?: profile.pin

                // 핵심: flatMap 내부에서 Mono를 반환해야 합니다.
                userProfileRepository.save(profile)
            }
            .then(Mono.just(Unit))
    }

    /**
     * 이메일을 통해 유저를 찾고, 프로필의 소유권을 검증하는 로직
     */
    private fun validateOwnerAndGetProfile(email: String, profileId: UUID): Mono<UserProfile> {
        return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.USER_NOT_FOUND)))
            .flatMap { user ->
                userProfileRepository.findById(profileId)
                    .filter { it.userId == user.userId } // 유저 ID로 소유권 대조
                    .switchIfEmpty(Mono.error(BusinessException(ErrorCode.FORBIDDEN)))
            }
    }
}