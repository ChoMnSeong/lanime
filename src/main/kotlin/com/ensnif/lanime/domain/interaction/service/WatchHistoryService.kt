package com.ensnif.lanime.domain.interaction.service

import com.ensnif.lanime.domain.interaction.dto.UpdateWatchProgressRequest
import com.ensnif.lanime.domain.interaction.repository.WatchHistoryRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID
import kotlin.math.ceil

@Service
class WatchHistoryService(
    private val watchHistoryRepository: WatchHistoryRepository
) {

    fun updateWatchProgress(profileId: UUID, episodeId: UUID, request: UpdateWatchProgressRequest): Mono<Void> {
        return watchHistoryRepository.upsertWatchProgress(profileId, episodeId, request.lastWatchedSecond)
    }

    fun getWatchHistory(profileId: UUID, page: Int, limit: Int, locale: String = "ja"): Mono<Map<String, Any>> {
        val offset = (page * limit).toLong()
        val episodesMono = watchHistoryRepository.findWatchedEpisodes(profileId, limit, offset, locale).collectList()
        val totalMono = watchHistoryRepository.countByProfileId(profileId)

        return Mono.zip(episodesMono, totalMono).map { tuple ->
            val episodes = tuple.t1
            val total = tuple.t2
            mapOf(
                "episodes" to episodes,
                "total" to total,
                "page" to page,
                "limit" to limit,
                "totalPages" to ceil(total.toDouble() / limit).toInt()
            )
        }
    }
}
