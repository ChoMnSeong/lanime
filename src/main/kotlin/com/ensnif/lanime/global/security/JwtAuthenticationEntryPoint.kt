package com.ensnif.lanime.global.security

import com.ensnif.lanime.global.exception.ErrorCode
import com.ensnif.lanime.global.exception.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : ServerAuthenticationEntryPoint {

    init {
        objectMapper.registerModule(JavaTimeModule())
    }

    override fun commence(exchange: ServerWebExchange, e: AuthenticationException): Mono<Void> {
        return Mono.defer {
            val response = exchange.response
            val errorCode = ErrorCode.UNAUTHORIZED // ✨ Enum에서 에러 정보 가져오기

            // 응답 헤더 설정
            response.statusCode = errorCode.status
            response.headers.contentType = MediaType.APPLICATION_JSON

            // ErrorCode 규격을 ErrorResponse 스펙으로 매핑
            val errorResponse = ErrorResponse(
                success = false,
                status = errorCode.status.value(),
                code = errorCode.code,
                message = errorCode.message
            )

            // 객체를 바이트 배열로 변환 후 버퍼에 담아 반환
            val bytes = objectMapper.writeValueAsBytes(errorResponse)
            val buffer = response.bufferFactory().wrap(bytes)

            response.writeWith(Mono.just(buffer))
        }
    }
}