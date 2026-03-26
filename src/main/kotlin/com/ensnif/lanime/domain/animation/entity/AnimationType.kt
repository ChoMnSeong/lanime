package com.ensnif.lanime.domain.animation.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("animation_type")
data class AnimationType(
    @Id
    val typeId: UUID? = null,
    val name: String
) : BaseEntity()