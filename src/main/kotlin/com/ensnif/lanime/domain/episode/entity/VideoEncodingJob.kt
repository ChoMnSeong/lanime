package com.ensnif.lanime.domain.episode.entity

import com.ensnif.lanime.global.common.entity.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("video_encoding_job")
data class VideoEncodingJob(
    @Id val jobId: UUID? = null,
    val episodeId: UUID,
    val status: EncodingStatus = EncodingStatus.PENDING,
    val inputPath: String? = null,
    val outputPath: String? = null,
    val errorMessage: String? = null,
    val startedAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null
) : BaseEntity()
