package com.ensnif.lanime.domain.animation.entity

import com.ensnif.lanime.global.common.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID
import java.time.LocalDate

@Table("animation")
data class Animation(
    @Id val animationId: UUID? = null,
    val typeId: UUID,           // ManyToOne: AnimationType 참조
    val title: String,
    val description: String?,
    val rating: String,          // 연령 등급 (ALL, 15, 19)
    val status: String,          // 방영 상태 (ONGOING, FINISHED)
    val releasedAt: LocalDate?
) : BaseEntity()