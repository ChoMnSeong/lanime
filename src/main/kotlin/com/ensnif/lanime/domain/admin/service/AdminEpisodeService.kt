package com.ensnif.lanime.domain.admin.service

import com.ensnif.lanime.domain.admin.dto.request.CreateEpisodeRequest
import com.ensnif.lanime.domain.admin.dto.request.UpdateEpisodeRequest
import com.ensnif.lanime.domain.animation.repository.AnimationRepository
import com.ensnif.lanime.domain.episode.entity.Episode
import com.ensnif.lanime.domain.episode.repository.EpisodeRepository
import com.ensnif.lanime.domain.episode.repository.EpisodeTranslationRepository
import com.ensnif.lanime.global.client.JikanClient
import com.ensnif.lanime.global.client.KitsuClient
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class AdminEpisodeService(
    private val animationRepository: AnimationRepository,
    private val episodeRepository: EpisodeRepository,
    private val episodeTranslationRepository: EpisodeTranslationRepository,
    private val jikanClient: JikanClient,
    private val kitsuClient: KitsuClient
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
            .flatMap { saved ->
                episodeTranslationRepository.upsert(saved.episodeId!!, "ja", saved.title, saved.description)
                    .thenReturn(saved)
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
            .flatMap { saved ->
                if (request.title != null || request.description != null) {
                    episodeTranslationRepository.upsert(saved.episodeId!!, "ja", saved.title, saved.description)
                        .thenReturn(saved)
                } else {
                    Mono.just(saved)
                }
            }
    }

    fun deleteEpisode(episodeId: UUID, context: UserProfileContext): Mono<Unit> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return episodeRepository.findById(episodeId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.EPISODE_NOT_FOUND)))
            .flatMap { episodeRepository.deleteById(episodeId).thenReturn(Unit) }
    }

    fun syncEpisodeThumbnails(animationId: UUID, context: UserProfileContext): Mono<Int> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap { animation ->
                val malId = animation.malId
                if (malId == null) {
                    Mono.error(RuntimeException("MAL ID가 없어 Kitsu 연동이 불가능합니다."))
                } else {
                    kitsuClient.fetchKitsuAnimeId(malId)
                        .flatMap { kitsuAnimeId ->
                            kitsuClient.fetchAllEpisodeThumbnails(kitsuAnimeId)
                                .collectList()
                                .map { episodes ->
                                    episodes
                                        .filter { it.attributes?.number != null && it.attributes?.thumbnail != null }
                                        .associate { ep ->
                                            ep.attributes!!.number!! to
                                                (ep.attributes.thumbnail!!.original
                                                    ?: ep.attributes.thumbnail.large
                                                    ?: ep.attributes.thumbnail.medium)
                                        }
                                        .filterValues { it != null }
                                        .mapValues { it.value!! as String }
                                }
                                .flatMap { thumbnailMap: Map<Int, String> ->
                                    episodeRepository.findAllByAnimationId(animationId)
                                        .collectList()
                                        .flatMapMany { episodes ->
                                            val updated = episodes.map { ep ->
                                                ep.copy(thumbnailUrl = thumbnailMap[ep.episodeNumber] ?: ep.thumbnailUrl)
                                                    .apply { createdAt = ep.createdAt }
                                            }
                                            episodeRepository.saveAll(updated)
                                        }
                                        .count()
                                        .map { it.toInt() }
                                }
                        }
                        .onErrorResume { Mono.just(0) }
                }
            }
    }
}
