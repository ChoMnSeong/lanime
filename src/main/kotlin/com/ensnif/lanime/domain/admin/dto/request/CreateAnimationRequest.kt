package com.ensnif.lanime.domain.admin.dto.request

import com.ensnif.lanime.domain.animation.entity.AirDay
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

data class CreateAnimationRequest(
    @field:NotNull val typeId: UUID,
    @field:NotBlank val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    @field:NotBlank val rating: String,   // ALL, 15, 19
    @field:NotBlank val status: String, 
    val airDay: AirDay?,
    val releasedAt: LocalDate?,
    val genreIds: List<UUID> = emptyList()
)
