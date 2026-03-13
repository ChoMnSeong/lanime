package com.ensnif.lanime.global.security

import com.ensnif.lanime.global.exception.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : ServerAuthenticationEntryPoint {

    override fun commence(exchange: ServerWebExchange, e: AuthenticationException): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        response.headers.contentType = MediaType.APPLICATION_JSON

        val errorResponse = ErrorResponse(
            status = HttpStatus.UNAUTHORIZED.value(),
            code = "AUTH_001",
            message = "인증 정보가 없거나 유효하지 않습니다."
        )

        val json = objectMapper.writeValueAsString(errorResponse)
        val buffer: DataBuffer = response.bufferFactory().wrap(json.toByteArray(StandardCharsets.UTF_8))

        return response.writeWith(Mono.just(buffer))
    }
}