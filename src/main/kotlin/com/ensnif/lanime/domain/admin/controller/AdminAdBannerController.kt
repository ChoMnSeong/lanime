package com.ensnif.lanime.domain.admin.controller

import com.ensnif.lanime.domain.admin.dto.request.CreateAdBannerRequest
import com.ensnif.lanime.domain.admin.dto.request.UpdateAdBannerRequest
import com.ensnif.lanime.domain.admin.service.AdminAdBannerService
import com.ensnif.lanime.domain.ads.entity.AdBanner
import com.ensnif.lanime.global.common.dto.ApiResponse
import com.ensnif.lanime.global.context.UserProfileContext
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/banners")
class AdminAdBannerController(private val adminAdBannerService: AdminAdBannerService) {

    @GetMapping
    fun getAllBanners(
        @AuthenticationPrincipal context: UserProfileContext
    ): Mono<ApiResponse<List<AdBanner>>> {
        return adminAdBannerService.getAllBanners(context)
            .collectList()
            .map { ApiResponse.success(it) }
    }

    @PostMapping
    fun createBanner(
        @AuthenticationPrincipal context: UserProfileContext,
        @Valid @RequestBody request: CreateAdBannerRequest
    ): Mono<ApiResponse<AdBanner>> {
        return adminAdBannerService.createBanner(request, context).map { ApiResponse.success(it) }
    }

    @PatchMapping("/{bannerId}")
    fun updateBanner(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable bannerId: UUID,
        @RequestBody request: UpdateAdBannerRequest
    ): Mono<ApiResponse<AdBanner>> {
        return adminAdBannerService.updateBanner(bannerId, request, context).map { ApiResponse.success(it) }
    }

    @DeleteMapping("/{bannerId}")
    fun deleteBanner(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable bannerId: UUID
    ): Mono<ApiResponse<Unit>> {
        return adminAdBannerService.deleteBanner(bannerId, context)
            .map { ApiResponse.withMessage("광고 배너가 삭제되었습니다.") }
    }
}
