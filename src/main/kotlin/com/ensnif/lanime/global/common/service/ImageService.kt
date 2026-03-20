package com.ensnif.lanime.global.common.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*

@Service
class ImageService(
    @Value("\${upload.path}") private val uploadPath: String,
    @Value("\${server.port:8080}") private val port: String
) {
    init {
        val path = Paths.get(uploadPath)
        if (!Files.exists(path)) Files.createDirectories(path)
    }

    fun uploadImage(filePart: FilePart): Mono<String> {
        val extension = filePart.filename().substringAfterLast(".", "png")
        // 1. 일단 임시 파일로 저장 (데이터 깨짐 방지)
        val tempFile = Paths.get(uploadPath, "temp_" + UUID.randomUUID().toString())

        return filePart.transferTo(tempFile).then(Mono.fromCallable {
            // 2. 저장된 파일의 바이너리로 해시(SHA-256) 추출
            val bytes = Files.readAllBytes(tempFile)
            val hashName = MessageDigest.getInstance("SHA-256")
                .digest(bytes)
                .joinToString("") { "%02x".format(it) }

            val finalFileName = "$hashName.$extension"
            val finalPath = Paths.get(uploadPath, finalFileName)

            // 3. 동일한 해시명을 가진 파일이 이미 있는지 확인
            if (Files.exists(finalPath)) {
                Files.delete(tempFile) // 임시 파일 삭제
            } else {
                Files.move(tempFile, finalPath) // 임시 파일을 정식 파일명으로 변경
            }

            // 4. 접근 가능한 URL 반환 (WebConfig 설정의 /display/ 경로 사용)
            "http://localhost:$port/$finalFileName"
        })
    }
}