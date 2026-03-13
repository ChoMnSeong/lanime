package com.ensnif.lanime.global.security // 정확한 패키지 경로인지 확인하세요

import com.ensnif.lanime.global.context.UserProfileContext
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
        // 필터에서 보낸 CustomAuthenticationToken이라고 가정하거나 
        // credentials에 담긴 정보를 해석합니다.
        val authToken = authentication as? JwtPreAuthenticationToken 
            ?: return Mono.empty()

        val accessToken = authToken.accessToken
        val profileToken = authToken.profileToken

        // 1. AccessToken 검증 (필수)
        if (!tokenProvider.validateToken(accessToken)) {
            return Mono.empty()
        }

        val email = tokenProvider.getEmail(accessToken)
        
        // 2. ProfileToken 검증 및 추출 (선택)
        val profileId = if (!profileToken.isNullOrBlank() && tokenProvider.validateToken(profileToken)) {
            tokenProvider.getProfileId(profileToken)
        } else {
            null
        }

        // 3. 통합 컨텍스트 생성
        val context = UserProfileContext(email, profileId)

        // 4. 최종 인증 객체 반환 (Principal 자리에 context를 넣음)
        return Mono.just(
            UsernamePasswordAuthenticationToken(
                context, 
                null, 
                tokenProvider.getAuthorities(profileToken)
            )
        )
    }
}