package com.ensnif.lanime.domain.user.repository

import com.ensnif.lanime.domain.user.entity.UserProfile
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID
import reactor.core.publisher.Flux

@Repository
interface UserProfileRepository : ReactiveCrudRepository<UserProfile, UUID> {
    fun findAllByUserId(userId: UUID): Flux<UserProfile>
}