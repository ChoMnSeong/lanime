package com.ensnif.lanime.domain.ads.service

import com.ensnif.lanime.domain.ads.dto.response.AdResponse
import com.ensnif.lanime.domain.ads.repository.AdBannerRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class AdService(private val adBannerRepository: AdBannerRepository) {

    fun getActiveAds(): Flux<AdResponse> {
        return adBannerRepository.findAllByIsActiveTrue()
            .map { banner ->
                AdResponse(
                    id = banner.adBannerId.toString(),
                    webImageURL = banner.imageUrl,
                    logoImageURL = banner.logoImageUrl
                )
            }
    }
}
