package com.ensnif.lanime.domain.user.dto.response

import com.ensnif.lanime.domain.user.entity.UserProfile
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class ProfileInfoResponse(
    val profileId: UUID,
    val name: String,
    val avatarUrl: String?,
    @JsonProperty("isOwner") val isOwner: Boolean,
    val age: Int? = null
) {
    companion object {
        fun from(profile: UserProfile) = ProfileInfoResponse(
            profileId = profile.profileId!!,
            name = profile.name,
            avatarUrl = profile.avatarUrl,
            isOwner = profile.isOwner,
            age = profile.age
        )
    }
}
