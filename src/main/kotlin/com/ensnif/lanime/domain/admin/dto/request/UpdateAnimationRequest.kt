package com.ensnif.lanime.domain.admin.dto.request

import com.ensnif.lanime.domain.animation.entity.AirDay
import java.time.LocalDate
import java.util.UUID

data class UpdateAnimationRequest(
    val typeId: UUID?,
    val title: String?,
    val description: String?,
    val thumbnailUrl: String?,
    val rating: String?,
    val status: String?,
    val airDay: AirDay?,
    val releasedAt: LocalDate?,
    val genreIds: List<UUID>?
)
