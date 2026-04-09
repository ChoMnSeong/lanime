package com.ensnif.lanime.domain.admin.service

import com.ensnif.lanime.domain.admin.dto.request.ImportAnimationRequest
import com.ensnif.lanime.domain.admin.dto.response.ImportAnimationResponse
import com.ensnif.lanime.domain.animation.entity.*
import com.ensnif.lanime.domain.animation.repository.*
import com.ensnif.lanime.domain.episode.entity.Episode
import com.ensnif.lanime.domain.episode.repository.EpisodeRepository
import com.ensnif.lanime.domain.episode.repository.EpisodeTranslationRepository
import com.ensnif.lanime.global.client.AniListClient
import com.ensnif.lanime.global.client.JikanClient
import com.ensnif.lanime.global.client.JikanEpisode
import com.ensnif.lanime.global.client.JikanEpisodeVideo
import com.ensnif.lanime.global.client.KitsuClient
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Service
class AnimationImportService(
    private val aniListClient: AniListClient,
    private val jikanClient: JikanClient,
    private val kitsuClient: KitsuClient,
    private val animationRepository: AnimationRepository,
    private val animationTranslationRepository: AnimationTranslationRepository,
    private val animationTypeRepository: AnimationTypeRepository,
    private val genreRepository: GenreRepository,
    private val animationGenreRepository: AnimationGenreRepository,
    private val episodeRepository: EpisodeRepository,
    private val episodeTranslationRepository: EpisodeTranslationRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun importAnimation(request: ImportAnimationRequest, context: UserProfileContext): Mono<ImportAnimationResponse> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))
        return importByMalId(request.malId)
    }

    fun importByMalId(malId: Int): Mono<ImportAnimationResponse> {
        return animationRepository.findByMalId(malId)
            .flatMap { existing -> fetchAndUpdate(existing, malId) }
            .switchIfEmpty(Mono.defer { fetchAndSave(malId) })
    }

    private fun fetchAndUpdate(existing: Animation, malId: Int): Mono<ImportAnimationResponse> {
        return Mono.zip(
            aniListClient.fetchByMalId(malId),
            jikanClient.fetchAnime(malId)
        ).flatMap { tuple ->
            val aniList = tuple.t1
            val jikan = tuple.t2

            val jaTitle = aniList.title.native ?: aniList.title.romaji ?: jikan.titleJapanese ?: jikan.title
            val enTitle = aniList.title.english ?: jikan.titleEnglish
            val status = mapStatus(jikan.status)
            val duration = parseDurationToSeconds(jikan.duration)

            animationRepository.save(
                existing.copy(
                    title         = jaTitle,
                    description   = jikan.synopsis,
                    thumbnailUrl  = aniList.coverImage?.extraLarge ?: aniList.coverImage?.large ?: jikan.images?.jpg?.largeImageUrl ?: existing.thumbnailUrl,
                    status        = status,
                    airDay        = mapAirDay(jikan.broadcast?.day),
                    releasedAt    = parseDate(jikan.aired?.from) ?: existing.releasedAt,
                    finishedAt    = if (status == AnimationStatus.FINISHED) parseDate(jikan.aired?.to) ?: existing.finishedAt else null
                ).apply { createdAt = existing.createdAt }
            ).flatMap { saved ->
                val animationId = saved.animationId!!
                val genreNames = jikan.genres.map { normalizeGenreName(it.name) }

                val saveJaTrans = animationTranslationRepository.upsert(animationId, "ja", jaTitle, jikan.synopsis)
                val saveEnTrans = if (enTitle != null) {
                    animationTranslationRepository.upsert(animationId, "en", enTitle, aniList.description)
                } else Mono.empty()
                val saveGenres = findOrCreateGenres(genreNames).flatMap { genres ->
                    val newIds = genres.map { it.genreId!! }
                    upsertGenreLinks(animationId, newIds)
                }

                Mono.`when`(saveJaTrans, saveEnTrans, saveGenres)
                    .then(importEpisodes(animationId, malId, duration, saved.thumbnailUrl))
                    .map { episodeCount ->
                        val langs = mutableListOf("ja")
                        if (enTitle != null) langs.add("en")
                        ImportAnimationResponse(
                            animationId = animationId,
                            title = jaTitle,
                            episodesImported = episodeCount,
                            translationsCreated = langs
                        )
                    }
            }
        }
    }

    private fun fetchAndSave(malId: Int): Mono<ImportAnimationResponse> {
        return Mono.zip(
            aniListClient.fetchByMalId(malId),
            jikanClient.fetchAnime(malId)
        ).flatMap { tuple ->
            val aniList = tuple.t1
            val jikan = tuple.t2

            val jaTitle = aniList.title.native ?: aniList.title.romaji ?: jikan.titleJapanese ?: jikan.title
            val enTitle = aniList.title.english ?: jikan.titleEnglish
            val typeName = mapAnimationType(jikan.type)
            val duration = parseDurationToSeconds(jikan.duration)

            // 1. Find or create AnimationType
            animationTypeRepository.findByName(typeName)
                .switchIfEmpty(animationTypeRepository.save(AnimationType(name = typeName)))
                .flatMap { animationType ->

                    // 2. Save animation
                    val status = mapStatus(jikan.status)
                    val animation = Animation(
                        typeId = animationType.typeId!!,
                        title = jaTitle,
                        description = jikan.synopsis,
                        thumbnailUrl = aniList.coverImage?.extraLarge ?: aniList.coverImage?.large ?: jikan.images?.jpg?.largeImageUrl,
                        rating = mapRating(jikan.rating),
                        status = status,
                        airDay = mapAirDay(jikan.broadcast?.day),
                        releasedAt = parseDate(jikan.aired?.from),
                        finishedAt = if (status == AnimationStatus.FINISHED) parseDate(jikan.aired?.to) else null,
                        malId = malId
                    )

                    animationRepository.save(animation).flatMap { saved ->
                        val animationId = saved.animationId!!

                        // 3. Save translations
                        val saveJaTrans = animationTranslationRepository.upsert(
                            animationId, "ja", jaTitle, jikan.synopsis
                        )
                        val saveEnTrans = if (enTitle != null) {
                            animationTranslationRepository.upsert(
                                animationId, "en", enTitle, aniList.description
                            )
                        } else Mono.empty()

                        // 4. Find or create genres, save mappings
                        val genreNames = jikan.genres.map { normalizeGenreName(it.name) }
                        val saveGenres = findOrCreateGenres(genreNames).flatMap { genres ->
                            val newIds = genres.map { it.genreId!! }
                            upsertGenreLinks(animationId, newIds)
                        }

                        // 5. Fetch and save episodes
                        Mono.`when`(saveJaTrans, saveEnTrans, saveGenres)
                            .then(importEpisodes(animationId, malId, duration, saved.thumbnailUrl))
                            .map { episodeCount ->
                                val langs = mutableListOf("ja")
                                if (enTitle != null) langs.add("en")
                                ImportAnimationResponse(
                                    animationId = animationId,
                                    title = jaTitle,
                                    episodesImported = episodeCount,
                                    translationsCreated = langs
                                )
                            }
                    }
                }
        }
    }

    private fun importEpisodes(animationId: UUID, malId: Int, defaultDuration: Int?, animationThumbnailUrl: String? = null): Mono<Int> {
        return kitsuClient.fetchKitsuAnimeId(malId)
            .flatMap { kitsuAnimeId ->
                log.info("Kitsu Anime ID 조회 성공: $kitsuAnimeId (MAL: $malId)")

                // Fetch Kitsu thumbnails
                val kitsuThumbnailsMono = kitsuClient.fetchAllEpisodeThumbnails(kitsuAnimeId)
                    .collectList()
                    .doOnNext { episodes ->
                        log.info("Kitsu에서 ${episodes.size}개 에피소드 썸네일 조회 성공")
                        episodes.take(5).forEach { ep ->
                            log.info("  - EP${ep.attributes?.number}: ${ep.attributes?.thumbnail?.original?.take(60)}...")
                        }
                        if (episodes.size > 5) log.info("  ... 외 ${episodes.size - 5}개")
                    }
                    .map { episodes ->
                        episodes.associate { ep ->
                            ep.attributes?.number to
                                (ep.attributes?.thumbnail?.original
                                    ?: ep.attributes?.thumbnail?.large
                                    ?: ep.attributes?.thumbnail?.medium)
                        }
                    }

                // Fetch Jikan episode videos for thumbnail fallback
                val jikanVideosMono = jikanClient.fetchAllEpisodeVideos(malId)
                    .collectList()
                    .doOnNext { videos ->
                        log.info("Jikan에서 ${videos.size}개 에피소드 비디오 조회 성공")
                        videos.take(5).forEach { vid ->
                            log.info("  - EP${vid.malId}: ${vid.images?.jpg?.largeImageUrl?.take(60)}...")
                        }
                    }
                    .map { videos ->
                        videos.associate { vid ->
                            (vid.malId as Int?) to vid.images?.jpg?.largeImageUrl
                        }
                    }
                    .onErrorResume { Mono.just(emptyMap<Int?, String?>()) }

                // Merge thumbnails: Kitsu primary, Jikan fallback
                Mono.zip(kitsuThumbnailsMono, jikanVideosMono)
                    .flatMap { tuple ->
                        val kitsuMap = tuple.t1
                        val jikanMap = tuple.t2

                        // Create merged map: start with Jikan, overwrite with Kitsu non-null values
                        val mergedMap = jikanMap.toMutableMap()
                        kitsuMap.forEach { (epNum, kitsuUrl) ->
                            if (kitsuUrl != null) {
                                mergedMap[epNum] = kitsuUrl
                            }
                        }

                        log.info("에피소드 썸네일 병합: Kitsu=${kitsuMap.count { it.value != null }}개, Jikan=${jikanMap.count { it.value != null }}개, 병합=${mergedMap.count { it.value != null }}개")

                        jikanClient.fetchAllEpisodes(malId)
                            .flatMapSequential { ep -> saveEpisode(animationId, ep, defaultDuration, mergedMap as Map<Int?, String?>, animationThumbnailUrl) }
                            .count()
                            .map { it.toInt() }
                    }
            }
            .doOnError { error ->
                log.warn("Kitsu 에피소드 썸네일 조회 실패: ${error.message}, Jikan만으로 진행")
            }
            .onErrorResume { error ->
                // Kitsu 연동 실패 시 Jikan만으로 진행
                jikanClient.fetchAllEpisodes(malId)
                    .flatMapSequential { ep -> saveEpisode(animationId, ep, defaultDuration, emptyMap(), animationThumbnailUrl) }
                    .count()
                    .map { it.toInt() }
            }
    }

    private fun saveEpisode(
        animationId: UUID,
        ep: JikanEpisode,
        defaultDuration: Int?,
        thumbnailMap: Map<Int?, String?>,
        animationThumbnailUrl: String? = null
    ): Mono<Episode> {
        val jaTitle = ep.titleJapanese ?: ep.title ?: "Episode ${ep.malId}"
        val enTitle = if (ep.title != null && ep.title != jaTitle) ep.title else null
        val thumbnailUrl = thumbnailMap[ep.malId] ?: animationThumbnailUrl

        return episodeRepository.upsertRaw(
            animationId = animationId,
            episodeNumber = ep.malId,
            title = jaTitle,
            thumbnailUrl = thumbnailUrl,
            description = null,
            videoUrl = null,
            duration = defaultDuration
        ).then(episodeRepository.findByAnimationIdAndEpisodeNumber(animationId, ep.malId))
            .doOnNext { savedEp ->
                if (thumbnailUrl != null) {
                    log.debug("EP${ep.malId} 저장: thumbnail_url=${thumbnailUrl.take(60)}...")
                } else {
                    log.debug("EP${ep.malId} 저장: thumbnail_url=null")
                }
            }
            .flatMap { savedEp ->
            val epId = savedEp.episodeId!!
            val saveJa = episodeTranslationRepository.upsert(epId, "ja", jaTitle, null)
            val saveEn = if (enTitle != null) {
                episodeTranslationRepository.upsert(epId, "en", enTitle, null)
            } else Mono.empty()
            Mono.`when`(saveJa, saveEn).thenReturn(savedEp)
        }
    }

    private fun findOrCreateGenres(names: List<String>): Mono<List<Genre>> {
        if (names.isEmpty()) return Mono.just(emptyList())

        return Flux.fromIterable(names.distinct())
            .flatMapSequential { name ->
                genreRepository.insertIfAbsent(name)
                    .switchIfEmpty(genreRepository.findByName(name))
            }
            .collectList()
    }

    private fun upsertGenreLinks(animationId: UUID, genreIds: List<UUID>): Mono<Void> {
        if (genreIds.isEmpty()) return Mono.empty()
        return Flux.fromIterable(genreIds)
            .flatMap { animationGenreRepository.insertIfAbsent(animationId, it) }
            .then()
    }

    // ─── Mapping helpers ──────────────────────────────────────────────────────

    private fun mapAnimationType(type: String?): String = when (type?.lowercase()) {
        "tv" -> "TVA"
        "movie" -> "MOVIE"
        "ova" -> "OVA"
        "ona" -> "ONA"
        "special" -> "SPECIAL"
        "music" -> "MUSIC"
        else -> type?.uppercase() ?: "TVA"
    }

    private fun mapStatus(status: String?): AnimationStatus = when (status) {
        "Airing" -> AnimationStatus.ONGOING
        "Finished Airing" -> AnimationStatus.FINISHED
        "Not yet aired" -> AnimationStatus.UPCOMING
        else -> AnimationStatus.UPCOMING
    }

    private fun mapAirDay(day: String?): AirDay? = when (day?.lowercase()?.trimEnd('s')) {
        "monday" -> AirDay.MONDAY
        "tuesday" -> AirDay.TUESDAY
        "wednesday" -> AirDay.WEDNESDAY
        "thursday" -> AirDay.THURSDAY
        "friday" -> AirDay.FRIDAY
        "saturday" -> AirDay.SATURDAY
        "sunday" -> AirDay.SUNDAY
        else -> null
    }

    private fun mapRating(rating: String?): String = when {
        rating == null -> "ALL"
        rating.startsWith("G") || rating.startsWith("PG -") -> "ALL"
        rating.startsWith("PG-13") -> "15"
        rating.startsWith("R+") || rating.startsWith("Rx") -> "19"
        rating.startsWith("R -") -> "15"
        else -> "ALL"
    }

    private fun parseDurationToSeconds(durationStr: String?): Int? {
        if (durationStr.isNullOrBlank() || durationStr.contains("Unknown", ignoreCase = true)) return null
        var total = 0
        Regex("(\\d+)\\s*hr").find(durationStr)?.groupValues?.get(1)?.toIntOrNull()?.let { total += it * 3600 }
        Regex("(\\d+)\\s*min").find(durationStr)?.groupValues?.get(1)?.toIntOrNull()?.let { total += it * 60 }
        return if (total > 0) total else null
    }

    private fun parseDate(dateStr: String?): LocalDate? = dateStr?.let {
        runCatching { OffsetDateTime.parse(it).toLocalDate() }.getOrNull()
    }

    private fun normalizeGenreName(name: String): String =
        name.uppercase().replace(" ", "_")
}
