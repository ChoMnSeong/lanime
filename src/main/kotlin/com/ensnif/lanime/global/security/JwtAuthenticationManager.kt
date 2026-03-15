package com.ensnif.lanime.global.security

import com.ensnif.lanime.global.context.UserProfileContext
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationManager(
    private val tokenProvider: JwtTokenProvider
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        // 1. 타입이 다르면 다른 매니저에게 기회를 주기 위해 empty (정상)
        val authToken = authentication as? JwtPreAuthenticationToken 
            ?: return Mono.error(BadCredentialsException("토큰이 만료되었거나 유효하지 않습니다."))

        val accessToken = authToken.accessToken
        val profileToken = authToken.profileToken

        // 2. 토큰 검증 및 에러 처리
        // empty()가 아니라 error()를 던져야 EntryPoint가 호출됩니다.
        if (!tokenProvider.validateToken(accessToken)) {
            return Mono.error(BadCredentialsException("토큰이 만료되었거나 유효하지 않습니다."))
        }

        return try {
            val email = tokenProvider.getEmail(accessToken)
            val profileId = if (!profileToken.isNullOrBlank() && tokenProvider.validateToken(profileToken)) {
                tokenProvider.getProfileId(profileToken)
            } else {
                null
            }

            val context = UserProfileContext(email, profileId)

            Mono.just(UsernamePasswordAuthenticationToken(
                context, 
                null, 
                tokenProvider.getAuthorities(profileToken)
            ))
        } catch (e: Exception) {
            // 토큰 파싱 중 발생하는 예외(만료 등) 처리
            Mono.error(BadCredentialsException("인증 정보 처리 중 오류가 발생했습니다: ${e.message}"))
        }
    }
}