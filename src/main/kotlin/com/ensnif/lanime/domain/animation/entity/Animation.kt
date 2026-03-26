package com.ensnif.lanime.domain.animation.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID
import java.time.LocalDate

@Table("animation")
data class Animation(
    @Id val animationId: UUID? = null,
    val typeId: UUID,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val rating: String,
    val status: String,
    val airDay: String?,
    val releasedAt: LocalDate?
) : BaseEntity()