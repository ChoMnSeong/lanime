package com.ensnif.lanime.domain.episode.service

import com.ensnif.lanime.domain.animation.repository.AnimationRepository
import com.ensnif.lanime.domain.episode.dto.EpisodeResponse
import com.ensnif.lanime.domain.episode.repository.EpisodeRepository
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class EpisodeService(
    private val animationRepository: AnimationRepository,
    private val episodeRepository: EpisodeRepository
) {

    fun getEpisodes(animationId: UUID, profileId: UUID?, locale: String = "ja"): Mono<List<EpisodeResponse>> {
        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap {
                val rows = if (profileId != null) {
                    episodeRepository.findAllByAnimationIdWithWatchHistory(animationId, profileId, locale)
                } else {
                    episodeRepository.findAllByAnimationId(animationId, locale)
                }
                rows.collectList()
            }
    }
}
