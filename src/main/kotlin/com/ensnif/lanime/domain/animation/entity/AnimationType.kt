package com.ensnif.lanime.domain.animation.entity

import com.ensnif.lanime.global.common.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("animation_type")
data class AnimationType(
    @Id
    val typeId: UUID? = null,
    val name: String,         // 예: TV 시리즈, 극장판, OVA
    val description: String?  // 타입에 대한 설명
) : BaseEntity()