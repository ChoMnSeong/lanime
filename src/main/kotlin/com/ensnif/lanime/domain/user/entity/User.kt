package com.ensnif.lanime.domain.user.entity

import com.ensnif.lanime.global.common.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("user") // PostgreSQL 예약어 충돌 방지
data class User(
    @Id val userId: UUID? = null,
    val email: String,
    val password: String,
    val isActive: Boolean = true
) : BaseEntity()