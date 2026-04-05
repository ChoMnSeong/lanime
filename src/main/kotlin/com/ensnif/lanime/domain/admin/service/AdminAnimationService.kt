package com.ensnif.lanime.domain.admin.service

import com.ensnif.lanime.domain.admin.dto.request.CreateAnimationRequest
import com.ensnif.lanime.domain.admin.dto.request.CreateAnimationTypeRequest
import com.ensnif.lanime.domain.admin.dto.request.CreateGenreRequest
import com.ensnif.lanime.domain.admin.dto.request.UpdateAnimationRequest
import com.ensnif.lanime.domain.animation.entity.Animation
import com.ensnif.lanime.domain.animation.entity.AnimationGenre
import com.ensnif.lanime.domain.animation.entity.AnimationType
import com.ensnif.lanime.domain.animation.entity.Genre
import com.ensnif.lanime.domain.animation.repository.AnimationGenreRepository
import com.ensnif.lanime.domain.animation.repository.AnimationRepository
import com.ensnif.lanime.domain.animation.repository.AnimationTypeRepository
import com.ensnif.lanime.domain.animation.repository.GenreRepository
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class AdminAnimationService(
    private val animationRepository: AnimationRepository,
    private val animationGenreRepository: AnimationGenreRepository,
    private val animationTypeRepository: AnimationTypeRepository,
    private val genreRepository: GenreRepository
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
            if (request.genreIds.isEmpty()) {
                Mono.just(saved)
            } else {
                val genres = request.genreIds.map { AnimationGenre(animationId = saved.animationId!!, genreId = it) }
                animationGenreRepository.saveAll(genres).then(Mono.just(saved))
            }
        }
    }

    fun createAnimationType(request: CreateAnimationTypeRequest, context: UserProfileContext): Mono<AnimationType> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return animationTypeRepository.save(
            AnimationType(name = request.name)
        )
    }

    fun createGenre(request: CreateGenreRequest, context: UserProfileContext): Mono<Genre> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return genreRepository.save(
            Genre(name = request.name)
        )
    }

    fun updateAnimation(animationId: UUID, request: UpdateAnimationRequest, context: UserProfileContext): Mono<Animation> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap { existing ->
                animationRepository.save(
                    existing.copy(
                        typeId = request.typeId ?: existing.typeId,
                        title = request.title ?: existing.title,
                        description = request.description ?: existing.description,
                        thumbnailUrl = request.thumbnailUrl ?: existing.thumbnailUrl,
                        rating = request.rating ?: existing.rating,
                        status = request.status ?: existing.status,
                        airDay = request.airDay ?: existing.airDay,
                        releasedAt = request.releasedAt ?: existing.releasedAt
                    ).apply { createdAt = existing.createdAt }
                )
            }
            .flatMap { saved ->
                if (request.genreIds == null) {
                    Mono.just(saved)
                } else {
                    animationGenreRepository.deleteAllByAnimationId(animationId)
                        .then(
                            if (request.genreIds.isEmpty()) Mono.just(saved)
                            else {
                                val genres = request.genreIds.map { AnimationGenre(animationId = animationId, genreId = it) }
                                animationGenreRepository.saveAll(genres).then(Mono.just(saved))
                            }
                        )
                }
            }
    }

    fun deleteAnimation(animationId: UUID, context: UserProfileContext): Mono<Unit> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap { animationRepository.deleteById(animationId).thenReturn(Unit) }
    }
}
