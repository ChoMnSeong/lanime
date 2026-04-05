package com.ensnif.lanime.domain.admin.service

import com.ensnif.lanime.domain.admin.dto.request.CreateEpisodeRequest
import com.ensnif.lanime.domain.admin.dto.request.UpdateEpisodeRequest
import com.ensnif.lanime.domain.animation.repository.AnimationRepository
import com.ensnif.lanime.domain.episode.entity.Episode
import com.ensnif.lanime.domain.episode.repository.EpisodeRepository
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class AdminEpisodeService(
    private val animationRepository: AnimationRepository,
    private val episodeRepository: EpisodeRepository
) {

    fun createEpisode(animationId: UUID, request: CreateEpisodeRequest, context: UserProfileContext): Mono<Episode> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap {
                episodeRepository.save(
                    Episode(
                        animationId = animationId,
                        episodeNumber = request.episodeNumber,
                        title = request.title,
                        thumbnailUrl = request.thumbnailUrl,
                        description = request.description,
                        videoUrl = null,
                        duration = request.duration
                    )
                )
            }
    }

    fun updateEpisode(episodeId: UUID, request: UpdateEpisodeRequest, context: UserProfileContext): Mono<Episode> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return episodeRepository.findById(episodeId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.EPISODE_NOT_FOUND)))
            .flatMap { existing ->
                episodeRepository.save(
                    existing.copy(
                        episodeNumber = request.episodeNumber ?: existing.episodeNumber,
                        title = request.title ?: existing.title,
                        thumbnailUrl = request.thumbnailUrl ?: existing.thumbnailUrl,
                        description = request.description ?: existing.description,
                        duration = request.duration ?: existing.duration
                    ).apply { createdAt = existing.createdAt }
                )
            }
    }

    fun deleteEpisode(episodeId: UUID, context: UserProfileContext): Mono<Unit> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return episodeRepository.findById(episodeId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.EPISODE_NOT_FOUND)))
            .flatMap { episodeRepository.deleteById(episodeId).thenReturn(Unit) }
    }
}
