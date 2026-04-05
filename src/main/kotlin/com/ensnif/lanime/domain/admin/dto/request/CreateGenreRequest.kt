package com.ensnif.lanime.domain.admin.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateGenreRequest(
    @field:NotBlank val name: String
)
