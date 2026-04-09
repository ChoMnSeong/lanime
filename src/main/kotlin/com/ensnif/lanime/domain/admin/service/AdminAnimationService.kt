package com.ensnif.lanime.domain.admin.service

import com.ensnif.lanime.domain.admin.dto.request.CreateAnimationRequest
import com.ensnif.lanime.domain.admin.dto.request.CreateAnimationTypeRequest
import com.ensnif.lanime.domain.admin.dto.request.CreateGenreRequest
import com.ensnif.lanime.domain.admin.dto.request.UpdateAnimationRequest
import com.ensnif.lanime.domain.admin.dto.request.UpsertAnimationTranslationRequest
import com.ensnif.lanime.domain.admin.dto.request.UpsertEpisodeTranslationRequest
import com.ensnif.lanime.domain.animation.entity.Animation
import com.ensnif.lanime.domain.animation.entity.AnimationGenre
import com.ensnif.lanime.domain.animation.entity.AnimationStatus
import com.ensnif.lanime.domain.animation.entity.AnimationType
import com.ensnif.lanime.domain.animation.entity.Genre
import java.time.LocalDate
import com.ensnif.lanime.domain.animation.repository.AnimationGenreRepository
import com.ensnif.lanime.domain.animation.repository.AnimationRepository
import com.ensnif.lanime.domain.animation.repository.AnimationTranslationRepository
import com.ensnif.lanime.domain.animation.repository.AnimationTypeRepository
import com.ensnif.lanime.domain.animation.repository.GenreRepository
import com.ensnif.lanime.domain.episode.repository.EpisodeRepository
import com.ensnif.lanime.domain.episode.repository.EpisodeTranslationRepository
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class AdminAnimationService(
    private val animationRepository: AnimationRepository,
    private val animationTranslationRepository: AnimationTranslationRepository,
    private val animationGenreRepository: AnimationGenreRepository,
    private val animationTypeRepository: AnimationTypeRepository,
    private val genreRepository: GenreRepository,
    private val episodeRepository: EpisodeRepository,
    private val episodeTranslationRepository: EpisodeTranslationRepository
) {

    fun createAnimation(request: CreateAnimationRequest, context: UserProfileContext): Mono<Animation> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return animationRepository.save(
            Animation(
                typeId = request.typeId,
                title = request.title,
                description = request.description,
                thumbnailUrl = request.thumbnailUrl,
                rating = request.rating,
                status = request.status,
                airDay = request.airDay,
                releasedAt = request.releasedAt
            )
        ).flatMap { saved ->
            val genreSave = if (request.genreIds.isEmpty()) {
                Mono.just(saved)
            } else {
                val genres = request.genreIds.map { AnimationGenre(animationId = saved.animationId!!, genreId = it) }
                animationGenreRepository.saveAll(genres).then(Mono.just(saved))
            }
            genreSave.flatMap { animation ->
                animationTranslationRepository.upsert(animation.animationId!!, "ja", animation.title, animation.description)
                    .thenReturn(animation)
            }
        }
    }

    fun createAnimationType(request: CreateAnimationTypeRequest, context: UserProfileContext): Mono<AnimationType> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return animationTypeRepository.save(
            AnimationType(name = request.name.uppercase().trim())
        )
    }

    fun createGenre(request: CreateGenreRequest, context: UserProfileContext): Mono<Genre> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return genreRepository.save(
            Genre(name = request.name.uppercase().trim())
        )
    }

    fun updateAnimation(animationId: UUID, request: UpdateAnimationRequest, context: UserProfileContext): Mono<Animation> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap { existing ->
                val newStatus = request.status ?: existing.status
                animationRepository.save(
                    existing.copy(
                        typeId = request.typeId ?: existing.typeId,
                        title = request.title ?: existing.title,
                        description = request.description ?: existing.description,
                        thumbnailUrl = request.thumbnailUrl ?: existing.thumbnailUrl,
                        rating = request.rating ?: existing.rating,
                        status = newStatus,
                        airDay = request.airDay ?: existing.airDay,
                        releasedAt = request.releasedAt ?: existing.releasedAt,
                        finishedAt = when {
                            newStatus == AnimationStatus.FINISHED && existing.finishedAt == null -> LocalDate.now()
                            newStatus != AnimationStatus.FINISHED -> null
                            else -> existing.finishedAt
                        }
                    ).apply { createdAt = existing.createdAt }
                )
            }
            .flatMap { saved ->
                if (request.genreIds == null) {
                    Mono.just(saved)
                } else {
                    animationGenreRepository.findAllByAnimationId(animationId)
                        .map { it.genreId }
                        .collectList()
                        .flatMap { existingIds ->
                            val newIds = request.genreIds.toSet()
                            val toDelete = existingIds.filterNot { it in newIds }
                            val toAdd = request.genreIds.filterNot { it in existingIds.toSet() }

                            val deleteMono = Flux.fromIterable(toDelete)
                                .flatMap { animationGenreRepository.deleteByAnimationIdAndGenreId(animationId, it) }
                                .then()
                            val addMono = Flux.fromIterable(toAdd)
                                .flatMap { animationGenreRepository.insertIfAbsent(animationId, it) }
                                .then()

                            Mono.`when`(deleteMono, addMono).thenReturn(saved)
                        }
                }
            }
            .flatMap { saved ->
                if (request.title != null || request.description != null) {
                    animationTranslationRepository.upsert(animationId, "ja", saved.title, saved.description)
                        .thenReturn(saved)
                } else {
                    Mono.just(saved)
                }
            }
    }

    fun deleteAnimation(animationId: UUID, context: UserProfileContext): Mono<Unit> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap { animationRepository.deleteById(animationId).thenReturn(Unit) }
    }

    fun upsertAnimationTranslation(
        animationId: UUID,
        locale: String,
        request: UpsertAnimationTranslationRequest,
        context: UserProfileContext
    ): Mono<Unit> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap {
                animationTranslationRepository.upsert(animationId, locale, request.title, request.description)
                    .thenReturn(Unit)
            }
    }

    fun upsertEpisodeTranslation(
        episodeId: UUID,
        locale: String,
        request: UpsertEpisodeTranslationRequest,
        context: UserProfileContext
    ): Mono<Unit> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return episodeRepository.findById(episodeId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.EPISODE_NOT_FOUND)))
            .flatMap {
                episodeTranslationRepository.upsert(episodeId, locale, request.title, request.description)
                    .thenReturn(Unit)
            }
    }
}
