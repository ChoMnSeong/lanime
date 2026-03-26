package com.ensnif.lanime.domain.social.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("episode_comment")
data class EpisodeComment(
    @Id val commentId: UUID? = null,
    val episodeId: UUID,
    val profileId: UUID,
    val content: String,
    val parentCommentId: UUID? = null
) : BaseEntity()
