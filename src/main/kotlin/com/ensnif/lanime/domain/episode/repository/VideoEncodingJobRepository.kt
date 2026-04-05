package com.ensnif.lanime.domain.episode.repository

import com.ensnif.lanime.domain.episode.entity.VideoEncodingJob
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface VideoEncodingJobRepository : ReactiveCrudRepository<VideoEncodingJob, UUID> {

    fun findTopByEpisodeIdOrderByCreatedAtDesc(episodeId: UUID): Mono<VideoEncodingJob>

    @Query("SELECT * FROM video_encoding_job WHERE status = 'ENCODING'")
    fun findAllEncoding(): Flux<VideoEncodingJob>
}
