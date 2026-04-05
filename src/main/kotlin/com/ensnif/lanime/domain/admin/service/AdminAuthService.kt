package com.ensnif.lanime.domain.admin.service

import com.ensnif.lanime.domain.admin.dto.request.AdminSigninRequest
import com.ensnif.lanime.domain.admin.dto.response.AdminSigninResponse
import com.ensnif.lanime.domain.admin.repository.AdminRepository
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import com.ensnif.lanime.global.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AdminAuthService(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun signin(request: AdminSigninRequest): Mono<AdminSigninResponse> {
        return adminRepository.findByEmail(request.email)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ADMIN_NOT_FOUND)))
            .map { admin ->
                if (!passwordEncoder.matches(request.password, admin.password)) {
                    throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
                }
                val token = jwtTokenProvider.createAdminToken(admin.email, admin.adminId!!)
                AdminSigninResponse(
                    accessToken = token,
                    expiresIn = jwtTokenProvider.getExpirationSeconds()
                )
            }
    }
}
