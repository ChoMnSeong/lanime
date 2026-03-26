package com.ensnif.lanime.domain.ads.repository

import com.ensnif.lanime.domain.ads.entity.AdBanner
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.UUID

@Repository
interface AdBannerRepository : ReactiveCrudRepository<AdBanner, UUID> {
    fun findAllByIsActiveTrue(): Flux<AdBanner>
}
