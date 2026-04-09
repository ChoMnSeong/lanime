package com.ensnif.lanime.domain.admin.service

import com.ensnif.lanime.domain.admin.dto.response.ImportAnimationResponse
import com.ensnif.lanime.domain.animation.repository.AnimationRepository
import com.ensnif.lanime.global.client.AniListClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDate

@Service
class ScheduledImportService(
    private val aniListClient: AniListClient,
    private val animationImportService: AnimationImportService,
    private val animationRepository: AnimationRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 매 분기 시작(1월/4월/7월/10월 1일) 새벽 3시에 현재 시즌 애니메이션을 자동 임포트
     */
    @Scheduled(cron = "0 0 3 1 1,4,7,10 *")
    fun scheduleSeasonalImport() {
        val (season, year) = currentSeason()
        log.info("[$season $year] 시즌 애니메이션 자동 임포트 시작")
        importSeason(season, year)
            .subscribe(
                { results -> log.info("[$season $year] 임포트 완료 — ${results.size}편") },
                { error -> log.error("[$season $year] 임포트 실패: ${error.message}") }
            )
    }

    /**
     * 특정 시즌 임포트 (스케줄러 + 어드민 수동 트리거 공용)
     * MAL ID가 없는 애니메이션은 건너뜀. 이미 임포트된 항목은 자동 스킵.
     */
    fun importSeason(season: String, year: Int): Mono<List<ImportAnimationResponse>> {
        return aniListClient.fetchSeasonalAnime(season, year)
            .filter { it.idMal != null }
            .concatMap { media ->
                Mono.delay(Duration.ofMillis(1000))
                    .flatMap { animationImportService.importByMalId(media.idMal!!) }
                    .doOnSuccess { it?.let { r -> log.info("임포트 완료: ${r.title} (MAL ${media.idMal})") } }
                    .onErrorResume { error ->
                        log.warn("스킵: MAL ${media.idMal} — ${error.message}")
                        Mono.empty()
                    }
            }
            .collectList()
    }

    fun currentSeason(): Pair<String, Int> {
        val now = LocalDate.now()
        val season = when (now.monthValue) {
            1, 2, 3 -> "WINTER"
            4, 5, 6 -> "SPRING"
            7, 8, 9 -> "SUMMER"
            else -> "FALL"
        }
        return season to now.year
    }

    /**
     * startYear 부터 endYear까지 시즌을 역순으로 임포트.
     * 오래 걸리므로 구독만 시작하고 즉시 반환 (fire-and-forget).
     */
    fun clearAndBulkImport(startYear: Int = LocalDate.now().year, endYear: Int = 2000) {
        val seasons = buildSeasonList(startYear, endYear)
        log.info("벌크 임포트 시작: ${seasons.first()} → ${seasons.last()} (총 ${seasons.size}시즌)")

        Flux.fromIterable(seasons)
            .concatMap { (season, year) ->
                log.info("[$season $year] 임포트 시작")
                importSeason(season, year)
                    .doOnSuccess { results -> log.info("[$season $year] 완료 — ${results?.size}편") }
                    .onErrorResume { error ->
                        log.error("[$season $year] 실패: ${error.message}")
                        Mono.just(emptyList())
                    }
                    .then(Mono.delay(Duration.ofSeconds(2)))
            }
            .subscribe(
                {},
                { error -> log.error("벌크 임포트 중 치명적 오류: ${error.message}") },
                { log.info("벌크 임포트 전체 완료") }
            )
    }

    // startYear부터 endYear까지 역순으로 (season, year) 목록 생성
    private fun buildSeasonList(startYear: Int, endYear: Int): List<Pair<String, Int>> {
        val seasonOrder = listOf("WINTER", "SPRING", "SUMMER", "FALL")
        val (currentSeason, _) = currentSeason()
        val result = mutableListOf<Pair<String, Int>>()

        var year = startYear
        var seasonIdx = seasonOrder.indexOf(currentSeason)

        while (year > endYear || (year == endYear && seasonIdx >= 0)) {
            result.add(seasonOrder[seasonIdx] to year)
            seasonIdx--
            if (seasonIdx < 0) {
                seasonIdx = 3
                year--
            }
        }
        return result
    }
}
