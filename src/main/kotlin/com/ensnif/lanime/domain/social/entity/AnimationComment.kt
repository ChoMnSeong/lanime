package com.ensnif.lanime.domain.social.entity

import com.ensnif.lanime.global.common.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("animation_comment")
data class AnimationComment(
    @Id val commentId: UUID? = null,
    val episodeId: UUID,        // ManyToOne: Episode 참조
    val profileId: UUID,        // ManyToOne: UserProfile 참조
    val content: String,
    val parentCommentId: UUID? = null // 대댓글용 자기 참조
) : BaseEntity()