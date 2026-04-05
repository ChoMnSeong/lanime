package com.ensnif.lanime.domain.admin.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("admin")
data class Admin(
    @Id val adminId: UUID? = null,
    val email: String,
    val password: String
) : BaseEntity()
