package com.ensnif.lanime.global.common.controller

import com.ensnif.lanime.global.common.service.ImageService
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import com.ensnif.lanime.global.common.dto.ApiResponse

@RestController
@RequestMapping("/api/v1/images")
class ImageController(private val imageService: ImageService) {

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart("file") file: Mono<FilePart>): Mono<ApiResponse<String>> {
        return file.flatMap { imageService.uploadImage(it) }
            .map { ApiResponse.success(it) }
    }
}