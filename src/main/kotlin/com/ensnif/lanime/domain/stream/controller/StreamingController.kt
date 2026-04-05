package com.ensnif.lanime.domain.stream.controller

import com.ensnif.lanime.domain.stream.service.StreamingService
import org.springframework.core.io.FileSystemResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/stream")
class StreamingController(private val streamingService: StreamingService) {

    @GetMapping("/{episodeId}/index.m3u8")
    fun getPlaylist(
        @PathVariable episodeId: UUID
    ): Mono<ResponseEntity<FileSystemResource>> {
        return streamingService.getPlaylistResource(episodeId)
    }

    @GetMapping("/{episodeId}/{segment}.ts")
    fun getSegment(
        @PathVariable episodeId: UUID,
        @PathVariable segment: String
    ): Mono<ResponseEntity<FileSystemResource>> {
        return streamingService.getSegmentResource(episodeId, segment)
    }
}
