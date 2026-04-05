package com.ensnif.lanime.domain.admin.dto.request

import jakarta.validation.constraints.NotBlank

data class CreateAnimationTypeRequest(
    @field:NotBlank val name: String
)
