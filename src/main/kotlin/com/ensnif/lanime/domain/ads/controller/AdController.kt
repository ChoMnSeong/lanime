package com.ensnif.lanime.domain.ads.controller

import com.ensnif.lanime.domain.ads.dto.response.AdResponse
import com.ensnif.lanime.domain.ads.service.AdService
import com.ensnif.lanime.global.common.dto.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/ad")
class AdController(private val adService: AdService) {

    @GetMapping
    fun getAdList(): Mono<ApiResponse<List<AdResponse>>> {
        return adService.getActiveAds()
            .collectList()
            .map { ApiResponse.success(it) }
    }
}
