package com.ensnif.lanime.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() } // REST API이므로 CSRF 비활성화
            .cors { it.configurationSource(corsConfigurationSource()) } // CORS 설정 연결
            .formLogin { it.disable() } // 기본 폼 로그인 비활성화
            .httpBasic { it.disable() } // HTTP Basic 인증 비활성화
            .authorizeExchange { exchange ->
                exchange
                    .pathMatchers("/api/v1/auth/**").permitAll() // 인증 관련은 모두 허용
                    .pathMatchers("/api/v1/animations/**").permitAll() // 조회 서비스 허용
                    .anyExchange().authenticated() // 나머지는 인증 필요
            }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        
        // 프론트엔드 주소 (개발 시 보통 3000이나 5173)
        configuration.allowedOriginPatterns = listOf("http://localhost:3000", "http://localhost:5173")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}