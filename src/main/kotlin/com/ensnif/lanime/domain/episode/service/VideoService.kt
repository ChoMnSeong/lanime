package com.ensnif.lanime.domain.episode.service

import com.ensnif.lanime.domain.episode.dto.EncodingStatusResponse
import com.ensnif.lanime.domain.episode.dto.VideoUploadResponse
import com.ensnif.lanime.domain.episode.entity.EncodingStatus
import com.ensnif.lanime.domain.episode.entity.VideoEncodingJob
import com.ensnif.lanime.domain.episode.repository.EpisodeRepository
import com.ensnif.lanime.domain.episode.repository.VideoEncodingJobRepository
import com.ensnif.lanime.global.context.UserProfileContext
import com.ensnif.lanime.global.exception.BusinessException
import com.ensnif.lanime.global.exception.ErrorCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Service
class VideoService(
    @Value("\${upload.path}") private val uploadPath: String,
    private val episodeRepository: EpisodeRepository,
    private val videoEncodingJobRepository: VideoEncodingJobRepository,
    private val hlsEncodingService: HlsEncodingService,
    private val encodingScope: CoroutineScope
) {

    fun uploadVideo(context: UserProfileContext, episodeId: UUID, filePart: FilePart): Mono<VideoUploadResponse> {
        if (!context.isAdmin) {
            return Mono.error(BusinessException(ErrorCode.FORBIDDEN_ADMIN_ONLY))
        }

        return episodeRepository.findById(episodeId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.EPISODE_NOT_FOUND)))
            .flatMap { episode ->
                // 이미 인코딩 중인 job이 있는지 확인
                videoEncodingJobRepository.findTopByEpisodeIdOrderByCreatedAtDesc(episodeId)
                    .flatMap { existingJob ->
                        if (existingJob.status == EncodingStatus.ENCODING) {
                            Mono.error(BusinessException(ErrorCode.ENCODING_IN_PROGRESS))
                        } else {
                            Mono.just(episode)
                        }
                    }
                    .switchIfEmpty(Mono.just(episode))
            }
            .flatMap { episode ->
                // 원본 파일 저장 경로 준비
                val ext = filePart.filename().substringAfterLast('.', "mp4")
                val originalDir = Paths.get(uploadPath, "videos", episode.animationId.toString(), episodeId.toString(), "original")
                Files.createDirectories(originalDir)
                val inputPath = originalDir.resolve("source.$ext")

                // 파일을 디스크에 저장 (non-blocking)
                filePart.transferTo(inputPath).then(
                    // job 레코드 생성 및 저장
                    videoEncodingJobRepository.save(
                        VideoEncodingJob(
                            episodeId = episodeId,
                            status = EncodingStatus.PENDING,
                            inputPath = inputPath.toAbsolutePath().toString()
                        )
                    )
                )
                .flatMap { savedJob ->
                    // episode encoding_status를 PENDING으로 업데이트
                    episodeRepository.save(
                        episode.copy(encodingStatus = EncodingStatus.PENDING)
                    ).thenReturn(savedJob)
                }
                .map { savedJob ->
                    // 비동기 인코딩 시작 (fire-and-forget)
                    encodingScope.launch(Dispatchers.IO) {
                        hlsEncodingService.encode(savedJob, episode)
                    }

                    VideoUploadResponse(
                        episodeId = episodeId,
                        jobId = savedJob.jobId!!,
                        encodingStatus = EncodingStatus.PENDING,
                        message = "영상 업로드가 완료되었습니다. 백그라운드에서 HLS 인코딩을 시작합니다."
                    )
                }
            }
    }

    fun getEncodingStatus(episodeId: UUID): Mono<EncodingStatusResponse> {
        return videoEncodingJobRepository.findTopByEpisodeIdOrderByCreatedAtDesc(episodeId)
            .switchIfEmpty(Mono.error(BusinessException(ErrorCode.EPISODE_NOT_FOUND)))
            .map { job ->
                EncodingStatusResponse(
                    episodeId = episodeId,
                    jobId = job.jobId!!,
                    status = job.status,
                    errorMessage = job.errorMessage,
                    startedAt = job.startedAt,
                    completedAt = job.completedAt
                )
            }
    }
}
