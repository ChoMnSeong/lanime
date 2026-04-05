package com.ensnif.lanime.domain.animation.repository

import com.ensnif.lanime.domain.animation.entity.AnimationGenre
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

interface AnimationGenreRepository : ReactiveCrudRepository<AnimationGenre, UUID> {
    fun findAllByAnimationId(animationId: UUID): Flux<AnimationGenre>
    fun deleteAllByAnimationId(animationId: UUID): reactor.core.publisher.Mono<Void>
}
