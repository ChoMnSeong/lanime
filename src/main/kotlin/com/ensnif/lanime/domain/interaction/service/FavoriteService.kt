package com.ensnif.lanime.domain.interaction.service

import com.ensnif.lanime.domain.animation.repository.AnimationRepository
import com.ensnif.lanime.domain.interaction.entity.Favorite
import com.ensnif.lanime.domain.interaction.repository.FavoriteRepository
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID
import kotlin.math.ceil

@Service
class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val animationRepository: AnimationRepository
) {

    fun addFavorite(profileId: UUID, animationId: UUID): Mono<Void> {
        return animationRepository.findById(animationId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.ANIMATION_NOT_FOUND)))
            .flatMap {
                favoriteRepository.existsByProfileIdAndAnimationId(profileId, animationId)
            }
            .flatMap { exists ->
                if (exists) Mono.empty()
                else favoriteRepository.save(Favorite(profileId = profileId, animationId = animationId)).then()
            }
    }

    fun removeFavorite(profileId: UUID, animationId: UUID): Mono<Void> {
        return favoriteRepository.deleteByProfileIdAndAnimationId(profileId, animationId)
    }

    fun getFavorites(profileId: UUID, page: Int, limit: Int): Mono<Map<String, Any>> {
        val offset = (page * limit).toLong()
        val listMono = favoriteRepository.findFavoritesByProfileId(profileId, limit, offset).collectList()
        val totalMono = favoriteRepository.countByProfileId(profileId)

        return Mono.zip(listMono, totalMono).map { tuple ->
            mapOf(
                "animations" to tuple.t1,
                "total" to tuple.t2,
                "page" to page,
                "limit" to limit,
                "totalPages" to ceil(tuple.t2.toDouble() / limit).toInt()
            )
        }
    }
}
