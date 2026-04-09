package com.ensnif.lanime.global.client

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

// ─── Response DTOs ───────────────────────────────────────────────────────────

data class JikanAnimeResponse @JsonCreator constructor(
    @JsonProperty("data") val data: JikanAnimeData?
)

data class JikanAnimeData @JsonCreator constructor(
    @JsonProperty("mal_id") val malId: Int,
    @JsonProperty("title") val title: String,
    @JsonProperty("title_english") val titleEnglish: String?,
    @JsonProperty("title_japanese") val titleJapanese: String?,
    @JsonProperty("type") val type: String?,
    @JsonProperty("status") val status: String?,
    @JsonProperty("aired") val aired: JikanAired?,
    @JsonProperty("broadcast") val broadcast: JikanBroadcast?,
    @JsonProperty("duration") val duration: String?,
    @JsonProperty("rating") val rating: String?,
    @JsonProperty("synopsis") val synopsis: String?,
    @JsonProperty("genres") val genres: List<JikanNamedItem> = emptyList(),
    @JsonProperty("images") val images: JikanImages?
)

data class JikanAired @JsonCreator constructor(
    @JsonProperty("from") val from: String?,
    @JsonProperty("to") val to: String?
)
data class JikanBroadcast @JsonCreator constructor(
    @JsonProperty("day") val day: String?
)
data class JikanNamedItem @JsonCreator constructor(
    @JsonProperty("name") val name: String
)
data class JikanImages @JsonCreator constructor(
    @JsonProperty("jpg") val jpg: JikanJpg?
)
data class JikanJpg @JsonCreator constructor(
    @JsonProperty("large_image_url") val largeImageUrl: String?
)

data class JikanEpisodesResponse @JsonCreator constructor(
    @JsonProperty("data") val data: List<JikanEpisode> = emptyList(),
    @JsonProperty("pagination") val pagination: JikanPagination
)

data class JikanEpisode @JsonCreator constructor(
    @JsonProperty("mal_id") val malId: Int,
    @JsonProperty("title") val title: String?,
    @JsonProperty("title_japanese") val titleJapanese: String?,
    @JsonProperty("title_romanji") val titleRomanji: String?,
    @JsonProperty("aired") val aired: String?
)

data class JikanPagination @JsonCreator constructor(
    @JsonProperty("last_visible_page") val lastVisiblePage: Int,
    @JsonProperty("has_next_page") val hasNextPage: Boolean
)

data class JikanEpisodeVideosResponse @JsonCreator constructor(
    @JsonProperty("data") val data: List<JikanEpisodeVideo> = emptyList(),
    @JsonProperty("pagination") val pagination: JikanPagination
)

data class JikanEpisodeVideo @JsonCreator constructor(
    @JsonProperty("mal_id") val malId: Int,
    @JsonProperty("images") val images: JikanImages?
)

// ─── Client ──────────────────────────────────────────────────────────────────

@Component
class JikanClient(
    @Qualifier("jikanWebClient") private val webClient: WebClient
) {

    fun fetchAnime(malId: Int): Mono<JikanAnimeData> {
        return webClient.get()
            .uri("/anime/$malId")
            .retrieve()
            .bodyToMono(JikanAnimeResponse::class.java)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                .filter { it is WebClientResponseException.TooManyRequests })
            .mapNotNull { it.data }
            .switchIfEmpty(Mono.error(RuntimeException("Jikan: MAL ID $malId 를 찾을 수 없습니다.")))
    }

    fun fetchAllEpisodes(malId: Int): Flux<JikanEpisode> {
        return fetchEpisodesPage(malId, 1)
    }

    fun fetchAllEpisodeVideos(malId: Int): Flux<JikanEpisodeVideo> {
        return fetchEpisodeVideosPage(malId, 1)
    }

    private fun fetchEpisodeVideosPage(malId: Int, page: Int): Flux<JikanEpisodeVideo> {
        return webClient.get()
            .uri("/anime/$malId/videos/episodes?page=$page")
            .retrieve()
            .bodyToMono(JikanEpisodeVideosResponse::class.java)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                .filter { it is WebClientResponseException.TooManyRequests })
            .flatMapMany { response ->
                val current = Flux.fromIterable(response.data)
                if (response.pagination.hasNextPage) {
                    current.concatWith(
                        Mono.delay(Duration.ofMillis(700))
                            .flatMapMany { fetchEpisodeVideosPage(malId, page + 1) }
                    )
                } else {
                    current
                }
            }
            .onErrorResume { Flux.empty() }
    }

    private fun fetchEpisodesPage(malId: Int, page: Int): Flux<JikanEpisode> {
        return webClient.get()
            .uri("/anime/$malId/episodes?page=$page")
            .retrieve()
            .bodyToMono(JikanEpisodesResponse::class.java)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                .filter { it is WebClientResponseException.TooManyRequests })
            .flatMapMany { response ->
                val current = Flux.fromIterable(response.data)
                if (response.pagination.hasNextPage) {
                    current.concatWith(
                        // Jikan rate limit: 1.5req/s — 700ms delay between pages
                        Mono.delay(Duration.ofMillis(700))
                            .flatMapMany { fetchEpisodesPage(malId, page + 1) }
                    )
                } else {
                    current
                }
            }
    }
}
