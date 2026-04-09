package com.ensnif.lanime.global.client

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

// ─── Response DTOs ───────────────────────────────────────────────────────────

data class KitsuMappingResponse(
    @JsonProperty("data") val data: List<KitsuMappingData> = emptyList(),
    @JsonProperty("included") val included: List<KitsuAnimeResource>? = null
)

data class KitsuMappingData(
    @JsonProperty("id") val id: String,
    @JsonProperty("type") val type: String
)

data class KitsuAnimeResource(
    @JsonProperty("id") val id: String,
    @JsonProperty("type") val type: String,
    @JsonProperty("attributes") val attributes: KitsuAnimeAttributes?
)

data class KitsuAnimeAttributes(
    @JsonProperty("posterImage") val posterImage: KitsuPosterImage?
)

data class KitsuPosterImage(
    @JsonProperty("original") val original: String?,
    @JsonProperty("large") val large: String?,
    @JsonProperty("medium") val medium: String?
)

data class KitsuEpisodesResponse(
    @JsonProperty("data") val data: List<KitsuEpisodeData> = emptyList(),
    @JsonProperty("links") val links: KitsuLinks?
)

data class KitsuEpisodeData(
    @JsonProperty("id") val id: String,
    @JsonProperty("attributes") val attributes: KitsuEpisodeAttributes?
)

data class KitsuEpisodeAttributes(
    @JsonProperty("number") val number: Int?,
    @JsonProperty("thumbnail") val thumbnail: KitsuThumbnail?
)

data class KitsuThumbnail(
    @JsonProperty("original") val original: String?,
    @JsonProperty("large") val large: String?,
    @JsonProperty("medium") val medium: String?
)

data class KitsuLinks(
    @JsonProperty("next") val next: String?
)

// ─── Client ──────────────────────────────────────────────────────────────────

@Component
class KitsuClient(
    @Qualifier("kitsuWebClient") private val webClient: WebClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun fetchPosterUrl(malId: Int): Mono<String> {
        return webClient.get()
            .uri("/mappings?filter[externalSite]=myanimelist/anime&filter[externalId]=$malId&include=item")
            .retrieve()
            .bodyToMono(KitsuMappingResponse::class.java)
            .retryWhen(Retry.backoff(2, Duration.ofSeconds(3))
                .filter { it is WebClientResponseException.TooManyRequests })
            .mapNotNull { response ->
                (response.included ?: emptyList())
                    .firstOrNull { it.type == "anime" }
                    ?.attributes?.posterImage
                    ?.let { it.original ?: it.large ?: it.medium }
            }
            .onErrorResume { Mono.empty() }
    }

    fun fetchKitsuAnimeId(malId: Int): Mono<String> {
        return webClient.get()
            .uri("/mappings?filter[externalSite]=myanimelist/anime&filter[externalId]=$malId&include=item")
            .retrieve()
            .bodyToMono(KitsuMappingResponse::class.java)
            .retryWhen(Retry.backoff(2, Duration.ofSeconds(3))
                .filter { it is WebClientResponseException.TooManyRequests })
            .doOnNext { response ->
                log.info("Kitsu Mapping 응답: included=${response.included?.size ?: 0}개 항목")
                response.included?.forEach { log.info("  - type=${it.type}, id=${it.id}") }
            }
            .mapNotNull { (it.included ?: emptyList()).firstOrNull { r -> r.type == "anime" }?.id }
            .switchIfEmpty(Mono.error(RuntimeException("Kitsu: MAL ID $malId 를 찾을 수 없습니다.")))
            .doOnError { error ->
                log.error("Kitsu fetchKitsuAnimeId 실패: ${error.message}", error)
            }
    }

    fun fetchAllEpisodeThumbnails(kitsuAnimeId: String): Flux<KitsuEpisodeData> {
        return fetchEpisodesPage(kitsuAnimeId, 0)
    }

    private fun fetchEpisodesPage(kitsuAnimeId: String, offset: Int): Flux<KitsuEpisodeData> {
        val uri = "/anime/$kitsuAnimeId/episodes?page[limit]=20&page[offset]=$offset"
        log.info("Kitsu 에피소드 조회: $uri")

        return webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(KitsuEpisodesResponse::class.java)
            .retryWhen(Retry.backoff(2, Duration.ofSeconds(3))
                .filter { it is WebClientResponseException.TooManyRequests })
            .doOnNext { response ->
                log.info("Kitsu 에피소드 응답 (offset=$offset): ${response.data.size}개 에피소드, hasNext=${response.links?.next != null}")
                response.data.take(3).forEach { ep ->
                    log.info("  EP${ep.attributes?.number}: thumbnail=${ep.attributes?.thumbnail}, original=${ep.attributes?.thumbnail?.original?.take(60)}")
                }
            }
            .flatMapMany { response ->
                val current = Flux.fromIterable(response.data)
                if (response.links?.next != null) {
                    current.concatWith(
                        Mono.delay(Duration.ofMillis(500))
                            .flatMapMany { fetchEpisodesPage(kitsuAnimeId, offset + 20) }
                    )
                } else {
                    current
                }
            }
            .doOnError { error ->
                log.error("Kitsu fetchEpisodesPage 실패 (offset=$offset): ${error.message}", error)
            }
            .onErrorResume { Flux.empty() }
    }
}
