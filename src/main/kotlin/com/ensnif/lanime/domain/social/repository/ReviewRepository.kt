package com.ensnif.lanime.domain.social.repository

import com.ensnif.lanime.domain.social.entity.AnimationReview
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
interface ReviewRepository : ReactiveCrudRepository<AnimationReview, UUID> {
    fun countByAnimationId(animationId: UUID): Mono<Long>
}
