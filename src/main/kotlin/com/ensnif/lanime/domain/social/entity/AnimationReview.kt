package com.ensnif.lanime.domain.social.entity

import com.ensnif.lanime.global.common.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("review")
data class Animaton(
    @Id val reviewId: UUID? = null,
    val animationId: UUID,  // 어떤 애니메이션에 대한 리뷰인지
    val profileId: UUID,    // 작성자 프로필
    val score: Int,         // 평점 (예: 1~5점)
    val content: String?    // 리뷰 내용 (선택 사항)
) : BaseEntity()