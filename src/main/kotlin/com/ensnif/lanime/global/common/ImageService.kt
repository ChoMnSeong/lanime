package com.ensnif.lanime.global.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Service
class ImageUploadService(
    @Value("\${upload.path}") private val uploadPath: String,
    @Value("\${server.port:8080}") private val port: String
) {
    init {
        val path = Paths.get(uploadPath)
        if (!Files.exists(path)) {
            Files.createDirectories(path)
        }
    }

    fun uploadImage(filePart: FilePart): Mono<String> {
        val extension = filePart.filename().substringAfterLast(".", "png")
        val fileName = "${UUID.randomUUID()}.$extension"
        val filePath = Paths.get(uploadPath, fileName)

        return filePart.transferTo(filePath)
            .then(Mono.just("http://localhost:$port/$fileName")) // 접근 가능한 URL 반환
    }
}