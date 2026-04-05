package com.ensnif.lanime.domain.admin.controller

import com.ensnif.lanime.domain.admin.dto.request.CreateAdminRequest
import com.ensnif.lanime.domain.admin.dto.response.AdminResponse
import com.ensnif.lanime.domain.admin.service.AdminAccountService
import com.ensnif.lanime.global.common.dto.ApiResponse
import com.ensnif.lanime.global.context.UserProfileContext
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/accounts")
class AdminAccountController(private val adminAccountService: AdminAccountService) {

    @GetMapping
    fun getAdmins(
        @AuthenticationPrincipal context: UserProfileContext
    ): Mono<ApiResponse<List<AdminResponse>>> {
        return adminAccountService.getAdmins(context).collectList().map { ApiResponse.success(it) }
    }

    @PostMapping
    fun createAdmin(
        @AuthenticationPrincipal context: UserProfileContext,
        @Valid @RequestBody request: CreateAdminRequest
    ): Mono<ApiResponse<AdminResponse>> {
        return adminAccountService.createAdmin(request, context).map { ApiResponse.success(it) }
    }

    @DeleteMapping("/{adminId}")
    fun deleteAdmin(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable adminId: UUID
    ): Mono<ApiResponse<Unit>> {
        return adminAccountService.deleteAdmin(adminId, context).map { ApiResponse.withMessage("관리자 계정이 삭제되었습니다.") }
    }
}
