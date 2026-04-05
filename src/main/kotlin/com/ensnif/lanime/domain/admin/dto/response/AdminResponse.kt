package com.ensnif.lanime.domain.admin.dto.response

import com.ensnif.lanime.domain.admin.entity.Admin
import java.time.LocalDateTime
import java.util.UUID

data class AdminResponse(
    val adminId: UUID,
    val email: String,
    val createdAt: LocalDateTime?
) {
    companion object {
        fun from(admin: Admin) = AdminResponse(
            adminId = admin.adminId!!,
            email = admin.email,
            createdAt = admin.createdAt
        )
    }
}
