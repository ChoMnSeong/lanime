package com.ensnif.lanime.domain.admin.repository

import com.ensnif.lanime.domain.admin.entity.Admin
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID

interface AdminRepository : ReactiveCrudRepository<Admin, UUID> {
    fun findByEmail(email: String): Mono<Admin>
    fun existsByEmail(email: String): Mono<Boolean>
}
