package com.ensnif.lanime.domain.user.service

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class VerificationService(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {
    private val PREFIX = "verification:"

    /**
     * [Step 1] 인증 코드 발송 시 호출 (저장)
     */
    fun saveCode(email: String): Mono<String> { // 반환 타입을 Mono<String>으로!
        val key = "$PREFIX$email"
        val code = generateRandomCode()
        val data = mapOf("code" to code, "verified" to "false")
        
        return redisTemplate.opsForHash<String, String>().putAll(key, data)
            .flatMap { redisTemplate.expire(key, Duration.ofMinutes(5)) }
            .thenReturn(code) // .then() 대신 .thenReturn(code) 사용!
    }

    /**
     * [Step 2] /verify-code 호출 시 (상태 변경)
     */
    fun verifyCode(email: String, code: String): Mono<Boolean> {
        val key = "$PREFIX$email"

        return redisTemplate.opsForHash<String, String>().get(key, "code")
            .flatMap { savedCode ->
                if (savedCode == code) {
                    // 코드가 일치하면 verified 필드를 true로 업데이트
                    redisTemplate.opsForHash<String, String>().put(key, "verified", "true")
                        .thenReturn(true)
                } else {
                    Mono.just(false)
                }
            }
            .defaultIfEmpty(false)
    }

    /**
     * [Step 3] /signup 호출 시 (확인 및 삭제)
     */
    fun confirmAndClear(email: String): Mono<Boolean> {
        val key = "$PREFIX$email"
        
        return redisTemplate.opsForHash<String, String>().get(key, "verified")
            .flatMap { verified ->
                if (verified == "true") {
                    // 인증된 상태라면 키 삭제 후 성공 반환
                    redisTemplate.delete(key).thenReturn(true)
                } else {
                    Mono.just(false)
                }
            }
            .defaultIfEmpty(false)
    }

    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" // 템플릿 예시처럼 영문대문자+숫자 조합
        return (1..5).map { chars.random() }.joinToString("")
    }
}