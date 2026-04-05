package com.ensnif.lanime.domain.admin.service

import com.ensnif.lanime.domain.admin.dto.request.CreateAdBannerRequest
import com.ensnif.lanime.domain.admin.dto.request.UpdateAdBannerRequest
import com.ensnif.lanime.domain.ads.entity.AdBanner
import com.ensnif.lanime.domain.ads.repository.AdBannerRepository
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class AdminAdBannerService(private val adBannerRepository: AdBannerRepository) {

    fun getAllBanners(context: UserProfileContext): Flux<AdBanner> {
        if (!context.isAdmin) return Flux.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))
        return adBannerRepository.findAll()
    }

    fun createBanner(request: CreateAdBannerRequest, context: UserProfileContext): Mono<AdBanner> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))
        return adBannerRepository.save(
            AdBanner(
                title = request.title,
                imageUrl = request.imageUrl,
                logoImageUrl = request.logoImageUrl,
                startAt = request.startAt,
                endAt = request.endAt,
                isActive = request.isActive
            )
        )
    }

    fun updateBanner(bannerId: UUID, request: UpdateAdBannerRequest, context: UserProfileContext): Mono<AdBanner> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))
        return adBannerRepository.findById(bannerId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.AD_BANNER_NOT_FOUND)))
            .flatMap { existing ->
                adBannerRepository.save(
                    existing.copy(
                        title = request.title ?: existing.title,
                        imageUrl = request.imageUrl ?: existing.imageUrl,
                        logoImageUrl = if (request.logoImageUrl != null) request.logoImageUrl else existing.logoImageUrl,
                        startAt = if (request.startAt != null) request.startAt else existing.startAt,
                        endAt = if (request.endAt != null) request.endAt else existing.endAt,
                        isActive = request.isActive ?: existing.isActive
                    ).apply { createdAt = existing.createdAt }
                )
            }
    }

    fun deleteBanner(bannerId: UUID, context: UserProfileContext): Mono<Unit> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))
        return adBannerRepository.findById(bannerId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.AD_BANNER_NOT_FOUND)))
            .flatMap { adBannerRepository.deleteById(bannerId).thenReturn(Unit) }
    }
}
