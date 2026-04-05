package com.ensnif.lanime.domain.episode.service

import com.ensnif.lanime.domain.episode.entity.EncodingStatus
import com.ensnif.lanime.domain.episode.entity.Episode
import com.ensnif.lanime.domain.episode.entity.VideoEncodingJob
import com.ensnif.lanime.domain.episode.repository.EpisodeRepository
import com.ensnif.lanime.domain.episode.repository.VideoEncodingJobRepository
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime

@Service
class HlsEncodingService(
    @Value("\${upload.path}") private val uploadPath: String,
    @Value("\${ffmpeg.path:ffmpeg}") private val ffmpegPath: String,
    private val episodeRepository: EpisodeRepository,
    private val videoEncodingJobRepository: VideoEncodingJobRepository
) {
    private val log = LoggerFactory.getLogger(HlsEncodingService::class.java)

    @PostConstruct
    fun init() {
        // FFmpeg 설치 여부 확인
        try {
            ProcessBuilder(ffmpegPath, "-version")
                .redirectErrorStream(true)
                .start()
                .waitFor()
            log.info("FFmpeg found: {}", ffmpegPath)
        } catch (e: IOException) {
            log.warn("FFmpeg not found at '{}'. HLS encoding will fail at runtime.", ffmpegPath)
        }

        // 서버 재시작 시 ENCODING 상태로 남아있던 job을 FAILED로 초기화
        videoEncodingJobRepository.findAllEncoding()
            .flatMap { job ->
                val failedJob = job.copy(
                    status = EncodingStatus.FAILED,
                    errorMessage = "서버 재시작으로 인해 인코딩이 중단되었습니다.",
                    completedAt = LocalDateTime.now()
                )
                videoEncodingJobRepository.save(failedJob)
                    .flatMap { episodeRepository.findById(job.episodeId) }
                    .flatMap { episode ->
                        episodeRepository.save(episode.copy(encodingStatus = EncodingStatus.FAILED))
                    }
            }
            .subscribe(
                { log.info("Interrupted encoding job reset to FAILED for episode: {}", it.episodeId) },
                { e -> log.error("Error resetting interrupted encoding jobs", e) }
            )
    }

    suspend fun encode(job: VideoEncodingJob, episode: Episode) {
        val jobId = job.jobId!!
        val episodeId = episode.episodeId!!

        // 1. 상태를 ENCODING으로 업데이트
        val encodingJob = job.copy(status = EncodingStatus.ENCODING, startedAt = LocalDateTime.now())
        withContext(Dispatchers.IO) {
            videoEncodingJobRepository.save(encodingJob).awaitSingle()
            episodeRepository.save(episode.copy(encodingStatus = EncodingStatus.ENCODING)).awaitSingle()
        }

        // 2. HLS 출력 디렉토리 준비
        val hlsDir = Paths.get(uploadPath, "videos", episode.animationId.toString(), episodeId.toString(), "hls")
        withContext(Dispatchers.IO) {
            Files.createDirectories(hlsDir)
        }

        val m3u8Path = hlsDir.resolve("index.m3u8").toAbsolutePath().toString()
        val segmentPattern = hlsDir.resolve("segment_%03d.ts").toAbsolutePath().toString()
        val inputPath = job.inputPath!!

        // 3. FFmpeg 실행
        val exitCode = withContext(Dispatchers.IO) {
            val process = ProcessBuilder(
                ffmpegPath,
                "-i", inputPath,
                "-c:v", "libx264",
                "-crf", "23",
                "-preset", "fast",
                "-c:a", "aac",
                "-b:a", "128k",
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-hls_segment_filename", segmentPattern,
                "-f", "hls",
                m3u8Path
            )
                .redirectErrorStream(true)
                .start()

            process.waitFor()
        }

        // 4. 결과에 따라 DB 업데이트
        if (exitCode == 0) {
            val hlsRelativePath = "videos/${episode.animationId}/${episodeId}/hls/index.m3u8"
            withContext(Dispatchers.IO) {
                videoEncodingJobRepository.save(
                    encodingJob.copy(
                        jobId = jobId,
                        status = EncodingStatus.COMPLETED,
                        outputPath = m3u8Path,
                        completedAt = LocalDateTime.now()
                    )
                ).awaitSingle()
                episodeRepository.save(
                    episode.copy(
                        hlsPath = hlsRelativePath,
                        encodingStatus = EncodingStatus.COMPLETED
                    )
                ).awaitSingle()
            }
            log.info("HLS encoding completed for episode: {}", episodeId)

            // 원본 파일 삭제 (선택적)
            withContext(Dispatchers.IO) {
                try {
                    val originalDir = Paths.get(uploadPath, "videos", episode.animationId.toString(), episodeId.toString(), "original")
                    originalDir.toFile().deleteRecursively()
                } catch (e: Exception) {
                    log.warn("Failed to delete original file for episode: {}", episodeId)
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                videoEncodingJobRepository.save(
                    encodingJob.copy(
                        jobId = jobId,
                        status = EncodingStatus.FAILED,
                        errorMessage = "FFmpeg 프로세스가 종료 코드 $exitCode 로 실패했습니다.",
                        completedAt = LocalDateTime.now()
                    )
                ).awaitSingle()
                episodeRepository.save(
                    episode.copy(encodingStatus = EncodingStatus.FAILED)
                ).awaitSingle()
            }
            log.error("HLS encoding failed for episode: {}, exit code: {}", episodeId, exitCode)
        }
    }
}
