package com.ensnif.lanime.domain.admin.dto.request

import jakarta.validation.constraints.NotBlank

data class UpsertAnimationTranslationRequest(
    @field:NotBlank val title: String,
    val description: String? = null
)
