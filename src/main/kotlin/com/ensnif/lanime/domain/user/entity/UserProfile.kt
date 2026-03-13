package com.ensnif.lanime.domain.user.entity

import com.ensnif.lanime.global.common.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("user_profile")
data class UserProfile(
    @Id val profileId: UUID? = null,
    val userId: UUID,
    val pin: String? = null,
    val name: String,
    val avatarUrl: String? = null,
    val isAdmin: Boolean = false
) : BaseEntity()