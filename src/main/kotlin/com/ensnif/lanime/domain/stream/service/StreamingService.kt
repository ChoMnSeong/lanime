package com.ensnif.lanime.domain.stream.service

import com.ensnif.lanime.domain.episode.entity.EncodingStatus
import com.ensnif.lanime.domain.episode.repository.EpisodeRepository
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.file.Paths
import java.util.UUID

@Service
class StreamingService(
    @Value("\${upload.path}") private val uploadPath: String,
    private val episodeRepository: EpisodeRepository
) {

    fun getPlaylistResource(episodeId: UUID): Mono<ResponseEntity<FileSystemResource>> {
        return episodeRepository.findById(episodeId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.EPISODE_NOT_FOUND)))
            .map { episode ->
                if (episode.encodingStatus != EncodingStatus.COMPLETED || episode.hlsPath == null) {
                    throw BusinessException(ErrorCode.VIDEO_NOT_FOUND)
                }
                val file = Paths.get(uploadPath, episode.hlsPath).toFile()
                if (!file.exists()) throw BusinessException(ErrorCode.VIDEO_NOT_FOUND)

                ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                    .body(FileSystemResource(file))
            }
    }

    fun getSegmentResource(episodeId: UUID, segment: String): Mono<ResponseEntity<FileSystemResource>> {
        return episodeRepository.findById(episodeId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.EPISODE_NOT_FOUND)))
            .map { episode ->
                if (episode.encodingStatus != EncodingStatus.COMPLETED || episode.hlsPath == null) {
                    throw BusinessException(ErrorCode.VIDEO_NOT_FOUND)
                }

                // 경로 순회 공격 방지: 영숫자, 언더스코어, 하이픈만 허용
                val sanitized = segment.replace(Regex("[^a-zA-Z0-9_\\-]"), "")
                if (sanitized.isEmpty()) throw BusinessException(ErrorCode.VIDEO_NOT_FOUND)

                val hlsDir = Paths.get(uploadPath, episode.hlsPath!!).parent
                val segmentFile = hlsDir.resolve("$sanitized.ts").toFile()

                // 실제 경로가 uploadPath 하위인지 이중 검증
                val uploadsCanonical = Paths.get(uploadPath).toFile().canonicalPath
                if (!segmentFile.exists() || !segmentFile.canonicalPath.startsWith(uploadsCanonical)) {
                    throw BusinessException(ErrorCode.VIDEO_NOT_FOUND)
                }

                ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("video/mp2t"))
                    .body(FileSystemResource(segmentFile))
            }
    }
}
