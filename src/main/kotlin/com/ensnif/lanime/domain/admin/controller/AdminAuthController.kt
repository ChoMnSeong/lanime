package com.ensnif.lanime.domain.admin.controller

import com.ensnif.lanime.domain.admin.dto.request.AdminSigninRequest
import com.ensnif.lanime.domain.admin.dto.response.AdminSigninResponse
import com.ensnif.lanime.domain.admin.service.AdminAuthService
import com.ensnif.lanime.global.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/admin/auth")
class AdminAuthController(private val adminAuthService: AdminAuthService) {

    @PostMapping("/signin")
    fun signin(@Valid @RequestBody request: AdminSigninRequest): Mono<ApiResponse<AdminSigninResponse>> {
        return adminAuthService.signin(request).map { ApiResponse.success(it) }
    }
}
