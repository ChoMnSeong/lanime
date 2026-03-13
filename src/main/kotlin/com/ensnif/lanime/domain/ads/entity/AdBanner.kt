package com.ensnif.lanime.domain.ads.entity

import com.ensnif.lanime.global.common.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID
import java.time.LocalDateTime

@Table("ad_banner")
data class AdBanner(
    @Id
    val adBannerId: UUID? = null,
    val title: String,          // 광고 캠페인 명
    val imageUrl: String,       // 광고 이미지 URL
    val linkUrl: String?,       // 클릭 시 이동할 링크
    val position: String,       // 노출 위치 (ex: MAIN_TOP, SIDEBAR)
    val startAt: LocalDateTime, // 노출 시작 일시
    val endAt: LocalDateTime,   // 노출 종료 일시
    val isActive: Boolean = true
) : BaseEntity()