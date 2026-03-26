package com.ensnif.lanime.domain.animation.repository

import com.ensnif.lanime.domain.animation.entity.Animation
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.util.UUID

@Repository
interface AnimationRepository : ReactiveCrudRepository<Animation, UUID> {
    fun findAllByAirDay(airDay: String): Flux<Animation>
    fun findAllByAirDayIsNotNull(): Flux<Animation>
}
