package com.ensnif.lanime.domain.animation.repository

import com.ensnif.lanime.domain.animation.entity.AnimationType
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AnimationTypeRepository : ReactiveCrudRepository<AnimationType, UUID>
