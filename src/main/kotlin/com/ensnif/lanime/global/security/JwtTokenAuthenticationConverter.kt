package com.ensnif.lanime.global.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtTokenAuthenticationConverter : ServerAuthenticationConverter {

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        val profileToken = exchange.request.headers.getFirst("X-Profile-Token")

        return Mono.justOrEmpty(authHeader)
            .filter { it.startsWith("Bearer ") }
            .map { it.substring(7) }
            .map { accessToken -> 
                // 두 토큰을 커스텀 인증 객체에 담아 Manager로 전달
                JwtPreAuthenticationToken(accessToken, profileToken) 
            }
    }
}