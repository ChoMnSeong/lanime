package com.ensnif.lanime.domain.user.repository

import com.ensnif.lanime.domain.user.entity.User
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Repository
interface UserRepository : ReactiveCrudRepository<User, UUID> {
    // 이메일로 가입 여부를 확인하는 쿼리 메서드
    fun existsByEmail(email: String): Mono<Boolean>
    
    // 이메일로 유저 정보를 찾는 메서드 (로그인 시 필요)
    fun findByEmail(email: String): Mono<User>
}