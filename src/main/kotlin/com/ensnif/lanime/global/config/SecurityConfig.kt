package com.ensnif.lanime.global.config

import com.ensnif.lanime.global.security.JwtTokenAuthenticationConverter
import com.ensnif.lanime.global.security.JwtAuthenticationManager
import com.ensnif.lanime.global.security.JwtAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val authenticationManager: JwtAuthenticationManager,
    private val authenticationConverter: JwtTokenAuthenticationConverter,
    private val entryPoint: JwtAuthenticationEntryPoint
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        // JWT 필터 생성 및 설정
        val authenticationWebFilter = AuthenticationWebFilter(authenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter)

        authenticationWebFilter.setAuthenticationFailureHandler(
            ServerAuthenticationEntryPointFailureHandler(entryPoint)
        )

        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .exceptionHandling { handling ->
                handling.authenticationEntryPoint(entryPoint)
            }
            .authorizeExchange { exchange ->
                exchange
                    .pathMatchers("/api/v1/auth/**").permitAll()
                    .pathMatchers("/api/v1/ad").permitAll()
                    .pathMatchers("/api/v1/animations/**").permitAll()
                    .pathMatchers("/api/v1/images/upload").authenticated() // 업로드는 인증 필요
                    .pathMatchers("/*.png", "/*.jpg", "/*.jpeg").permitAll() // 이미지는 누구나 조회 가능
                    .anyExchange().authenticated()
            }
            // 핵심: 인증 필터를 AUTHENTICATION 단계에 추가합니다.
            .addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        
        configuration.allowedOriginPatterns = listOf("http://localhost:3000", "http://localhost:5173")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}