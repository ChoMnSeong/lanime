package com.ensnif.lanime.domain.ads.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID
import java.time.LocalDateTime

@Table("ad_banner")
data class AdBanner(
    @Id
    val adBannerId: UUID? = null,
    val title: String,
    val imageUrl: String,
    val logoImageUrl: String?,
    val linkUrl: String?,
    val startAt: LocalDateTime?,
    val endAt: LocalDateTime?,
    val isActive: Boolean = true
) : BaseEntity()