package com.ensnif.lanime.domain.user.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID
import java.time.LocalDateTime

@Table("user_device")
data class UserDevice(
    @Id val deviceId: UUID? = null,
    val userId: UUID,           // ManyToOne: User
    val deviceToken: String?,   // Push 알림용
    val deviceType: String,     // IOS, ANDROID, WEB
    val lastLoggedInAt: LocalDateTime = LocalDateTime.now()
) : BaseEntity()