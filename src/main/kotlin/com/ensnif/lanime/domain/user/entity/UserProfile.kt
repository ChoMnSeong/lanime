package com.ensnif.lanime.domain.user.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("user_profile")
data class UserProfile(
    @Id val profileId: UUID? = null,
    val userId: UUID,
    var pin: String? = null,
    var name: String,
    var avatarUrl: String? = null,
    val isAdmin: Boolean = false
) : BaseEntity()