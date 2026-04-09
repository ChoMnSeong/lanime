package com.ensnif.lanime.domain.interaction.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("user_watch_history")
data class UserWatchHistory(
    @Id val historyId: UUID? = null,
    val profileId: UUID,        // ManyToOne: UserProfile 참조
    val episodeId: UUID,        // ManyToOne: Episode 참조
    val lastWatchedSecond: Int = 0,
    val isFinished: Boolean = false
) : BaseEntity()