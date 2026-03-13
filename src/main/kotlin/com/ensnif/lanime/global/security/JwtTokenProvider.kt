package com.ensnif.lanime.global.security // 패키지 경로는 프로젝트에 맞춰 수정하세요.

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.access-expiration}") private val accessExpiration: Long, // 밀리초 단위 (예: 3600000)
    @Value("\${jwt.refresh-expiration}") private val refreshExpiration: Long  // 밀리초 단위 (예: 1209600000)
) {

    private val key: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray(StandardCharsets.UTF_8))

    /**
     * Access Token 생성
     */
    fun createAccessToken(email: String): String {
        val now = Date()
        val validity = Date(now.time + accessExpiration)

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    /**
     * Refresh Token 생성
     */
    fun createRefreshToken(email: String): String {
        val now = Date()
        val validity = Date(now.time + refreshExpiration)

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    /**
     * 클라이언트 응답용 만료 시간(초) 반환
     */
    fun getExpirationSeconds(): Long {
        return accessExpiration / 1000
    }

    // 토큰 검증, 이메일 추출 등의 로직은 시큐리티 필터 구성 시 추가 구현 가능
}