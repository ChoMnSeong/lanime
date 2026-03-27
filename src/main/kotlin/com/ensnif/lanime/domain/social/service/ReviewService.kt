package com.ensnif.lanime.domain.social.service

import com.ensnif.lanime.domain.animation.dto.request.CreateReviewRequest
import com.ensnif.lanime.domain.animation.repository.AnimationRepository
import com.ensnif.lanime.domain.social.entity.AnimationReview
import com.ensnif.lanime.domain.social.repository.ReviewRepository
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val animationRepository: AnimationRepository
) {

    fun createReview(animationId: UUID, profileId: UUID, request: CreateReviewRequest): Mono<Unit> {
        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap {
                reviewRepository.existsByAnimationIdAndProfileId(animationId, profileId)
            }
            .flatMap { exists ->
                if (exists) {
                    Mono.error(BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS))
                } else {
                    reviewRepository.save(
                        AnimationReview(
                            animationId = animationId,
                            profileId = profileId,
                            score = request.rating,
                            content = request.comment
                        )
                    ).then(Mono.just(Unit))
                }
            }
    }

    fun updateReview(animationId: UUID, profileId: UUID, request: CreateReviewRequest): Mono<Unit> {
        return reviewRepository.findByAnimationIdAndProfileId(animationId, profileId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.REVIEW_NOT_FOUND)))
            .flatMap { existing ->
                val updated = existing.copy(score = request.rating, content = request.comment)
                updated.createdAt = existing.createdAt
                reviewRepository.save(updated).then(Mono.just(Unit))
            }
    }
}
