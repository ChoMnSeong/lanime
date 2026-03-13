package com.ensnif.lanime.global.security // 패키지 경로는 프로젝트에 맞춰 수정하세요.

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils

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
     * Refresh Token 생성
     */
    fun createProfileToken(email: String, profileId: UUID, isAdmin: Boolean): String {
        val now = Date()
        
        return Jwts.builder()
            .subject(email)
            .claim("pid", profileId.toString())
            .claim("admin", isAdmin) // Boolean 값 그대로 저장
            .issuedAt(Date())
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
    /**
     * 토큰에서 이메일(Subject) 추출
     */
    fun getEmail(token: String): String {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject
    }

    // 토큰 검증, 이메일 추출 등의 로직은 시큐리티 필터 구성 시 추가 구현 가능
    /**
     * 토큰에서 ProfileId 추출 (PID 클레임 사용)
     */
    fun getProfileId(token: String): UUID {
        val pidString = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload["pid"] as String
        return UUID.fromString(pidString)
    }

    /**
     * 토큰 유효성 검증
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            true
        } catch (e: Exception) {
            // 만료되었거나 변조된 토큰일 경우 false
            false
        }
    }

    // 권한 추출
    fun getAuthorities(token: String?): Collection<GrantedAuthority> {
        
        // 1. 토큰이 null이거나 비어있으면 즉시 기본 권한(또는 빈 리스트) 반환
        if (token.isNullOrBlank()) {
            // 프로젝트 설정에 따라 ROLE_USER 또는 AuthorityUtils.NO_AUTHORITIES 선택
            return AuthorityUtils.createAuthorityList("ROLE_USER")
        }

        // 2. null이 아님이 보장된 상태에서만 JJWT 파싱 진행
        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token) // 이제 여기서 IllegalArgumentException이 나지 않음
                .payload

            val roles = claims["roles"] as? List<*> ?: emptyList<Any>()
            roles.map { SimpleGrantedAuthority(it.toString()) }
            
        } catch (e: Exception) {
            // 토큰이 유효하지 않을 경우의 예외 처리
            AuthorityUtils.createAuthorityList("ROLE_USER")
        }
    }
}