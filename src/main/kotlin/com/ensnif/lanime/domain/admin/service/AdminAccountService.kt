package com.ensnif.lanime.domain.admin.service

import com.ensnif.lanime.domain.admin.dto.request.CreateAdminRequest
import com.ensnif.lanime.domain.admin.dto.response.AdminResponse
import com.ensnif.lanime.domain.admin.entity.Admin
import com.ensnif.lanime.domain.admin.repository.AdminRepository
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class AdminAccountService(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun getAdmins(context: UserProfileContext): Flux<AdminResponse> {
        if (!context.isAdmin) return Flux.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))
        return adminRepository.findAll().map { AdminResponse.from(it) }
    }

    fun createAdmin(request: CreateAdminRequest, context: UserProfileContext): Mono<AdminResponse> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return adminRepository.existsByEmail(request.email)
            .flatMap { exists ->
                if (exists) Mono.error(BusinessException(ErrorCode.ADMIN_ALREADY_EXISTS))
                else adminRepository.save(
                    Admin(email = request.email, password = passwordEncoder.encode(request.password)!!)
                ).map { AdminResponse.from(it) }
            }
    }

    fun deleteAdmin(adminId: UUID, context: UserProfileContext): Mono<Unit> {
        if (!context.isAdmin) return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))

        return adminRepository.findById(adminId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ADMIN_NOT_FOUND)))
            .flatMap { target ->
                // 자기 자신 삭제 방지: 요청자 email과 대상 email 비교
                if (target.email == context.email) {
                    Mono.error(BusinessException(ErrorCode.ADMIN_CANNOT_DELETE_SELF))
                } else {
                    adminRepository.deleteById(adminId).thenReturn(Unit)
                }
            }
    }
}
