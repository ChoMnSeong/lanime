package com.ensnif.lanime.global.common

import com.ensnif.lanime.global.common.ImageUploadService
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/images")
class ImageController(private val imageUploadService: ImageUploadService) {

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart("file") file: Mono<FilePart>): Mono<ApiResponse<String>> {
        return file.flatMap { imageUploadService.uploadImage(it) }
            .map { ApiResponse.success(it) }
    }
}