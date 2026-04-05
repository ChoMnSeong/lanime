package com.ensnif.lanime.domain.episode.controller

import com.ensnif.lanime.domain.episode.dto.EncodingStatusResponse
import com.ensnif.lanime.domain.episode.dto.VideoUploadResponse
import com.ensnif.lanime.domain.episode.service.VideoService
import com.ensnif.lanime.global.common.dto.ApiResponse
import com.ensnif.lanime.global.context.UserProfileContext
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/episodes")
class VideoController(private val videoService: VideoService) {

    @PostMapping("/{episodeId}/video", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadVideo(
        @AuthenticationPrincipal context: UserProfileContext,
        @PathVariable episodeId: UUID,
        @RequestPart("file") file: Mono<FilePart>
    ): Mono<ApiResponse<VideoUploadResponse>> {
        return file.flatMap { videoService.uploadVideo(context, episodeId, it) }
            .map { ApiResponse.success(it) }
    }

    @GetMapping("/{episodeId}/encoding-status")
    fun getEncodingStatus(
        @PathVariable episodeId: UUID
    ): Mono<ApiResponse<EncodingStatusResponse>> {
        return videoService.getEncodingStatus(episodeId)
            .map { ApiResponse.success(it) }
    }
}
