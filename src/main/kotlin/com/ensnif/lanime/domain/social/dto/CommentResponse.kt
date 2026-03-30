package com.ensnif.lanime.domain.social.dto

import java.time.LocalDateTime
import java.util.UUID

data class CommentResponse(
    val commentId: UUID,
    val profileId: UUID,
    val profileName: String,
    val avatarUrl: String?,
    val content: String,
    val parentCommentId: UUID?,
    val replyCount: Long,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
