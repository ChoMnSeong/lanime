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

data class AniListResponse @JsonCreator constructor(
    @JsonProperty("data") val data: AniListData?
)
data class AniListData @JsonCreator constructor(
    @JsonProperty("Media") val Media: AniListMedia?
)

data class AniListPageResponse @JsonCreator constructor(
    @JsonProperty("data") val data: AniListPageData?
)
data class AniListPageData @JsonCreator constructor(
    @JsonProperty("Page") val Page: AniListPage?
)
data class AniListPage @JsonCreator constructor(
    @JsonProperty("pageInfo") val pageInfo: AniListPageInfo,
    @JsonProperty("media") val media: List<AniListMedia> = emptyList()
)
data class AniListPageInfo @JsonCreator constructor(
    @JsonProperty("hasNextPage") val hasNextPage: Boolean,
    @JsonProperty("currentPage") val currentPage: Int
)
data class AniListMedia @JsonCreator constructor(
    @JsonProperty("id") val id: Int,
    @JsonProperty("idMal") val idMal: Int?,
    @JsonProperty("title") val title: AniListTitle,
    @JsonProperty("description") val description: String?,
    @JsonProperty("genres") val genres: List<String> = emptyList(),
    @JsonProperty("status") val status: String?,
    @JsonProperty("format") val format: String?,
    @JsonProperty("episodes") val episodes: Int?,
    @JsonProperty("duration") val duration: Int?,
    @JsonProperty("startDate") val startDate: AniListDate?,
    @JsonProperty("coverImage") val coverImage: AniListCoverImage?,
    @JsonProperty("bannerImage") val bannerImage: String?
)
data class AniListTitle @JsonCreator constructor(
    @JsonProperty("native") val native: String?,
    @JsonProperty("english") val english: String?,
    @JsonProperty("romaji") val romaji: String?
)
data class AniListDate @JsonCreator constructor(
    @JsonProperty("year") val year: Int?,
    @JsonProperty("month") val month: Int?,
    @JsonProperty("day") val day: Int?
)
data class AniListCoverImage @JsonCreator constructor(
    @JsonProperty("extraLarge") val extraLarge: String?,
    @JsonProperty("large") val large: String?
)

// ─── Client ──────────────────────────────────────────────────────────────────

@Component
class AniListClient(
    @Qualifier("aniListWebClient") private val webClient: WebClient
) {

    private val query = """
        query (${'$'}idMal: Int) {
            Media(idMal: ${'$'}idMal, type: ANIME) {
                id
                idMal
                title { native english romaji }
                description(asHtml: false)
                genres
                status
                format
                episodes
                duration
                startDate { year month day }
                coverImage { extraLarge large }
                bannerImage
            }
        }
    """.trimIndent()

    private val seasonalQuery = """
        query (${'$'}season: MediaSeason, ${'$'}seasonYear: Int, ${'$'}page: Int) {
            Page(page: ${'$'}page, perPage: 50) {
                pageInfo { hasNextPage currentPage }
                media(season: ${'$'}season, seasonYear: ${'$'}seasonYear, type: ANIME, sort: POPULARITY_DESC) {
                    id
                    idMal
                    title { native english romaji }
                    description(asHtml: false)
                    genres
                    status
                    format
                    episodes
                    duration
                    startDate { year month day }
                    coverImage { extraLarge large }
                    bannerImage
                }
            }
        }
    """.trimIndent()

    fun fetchSeasonalAnime(season: String, year: Int): Flux<AniListMedia> {
        return fetchSeasonalPage(season, year, 1)
    }

    private fun fetchSeasonalPage(season: String, year: Int, page: Int): Flux<AniListMedia> {
        val body = mapOf(
            "query" to seasonalQuery,
            "variables" to mapOf("season" to season, "seasonYear" to year, "page" to page)
        )
        return webClient.post()
            .bodyValue(body)
            .retrieve()
            .bodyToMono(AniListPageResponse::class.java)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(30))
                .filter { it is WebClientResponseException.TooManyRequests })
            .flatMapMany { response ->
                val pageData = response.data?.Page ?: return@flatMapMany Flux.empty()
                val current = Flux.fromIterable(pageData.media)
                if (pageData.pageInfo.hasNextPage) {
                    current.concatWith(
                        Mono.delay(Duration.ofMillis(600))
                            .flatMapMany { fetchSeasonalPage(season, year, page + 1) }
                    )
                } else {
                    current
                }
            }
    }

    fun fetchByMalId(malId: Int): Mono<AniListMedia> {
        val body = mapOf(
            "query" to query,
            "variables" to mapOf("idMal" to malId)
        )
        return webClient.post()
            .bodyValue(body)
            .retrieve()
            .bodyToMono(AniListResponse::class.java)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(30))
                .filter { it is WebClientResponseException.TooManyRequests })
            .mapNotNull { it.data?.Media }
            .switchIfEmpty(Mono.error(RuntimeException("AniList: MAL ID $malId 를 찾을 수 없습니다.")))
    }
}
