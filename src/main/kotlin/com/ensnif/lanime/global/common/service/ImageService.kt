package com.ensnif.lanime.global.common.service

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.webp.WebpWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
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
        // 1. 일단 임시 파일로 저장 (데이터 깨짐 방지)
        val tempFile = Paths.get(uploadPath, "temp_" + UUID.randomUUID().toString())

        return filePart.transferTo(tempFile).then(
            Mono.fromCallable {
                // 2. WebP로 변환
                val webpBytes = ImmutableImage.loader().fromPath(tempFile).bytes(WebpWriter.DEFAULT)
                Files.delete(tempFile)

                // 3. WebP 바이너리로 해시(SHA-256) 추출
                val hashName = MessageDigest.getInstance("SHA-256")
                    .digest(webpBytes)
                    .joinToString("") { "%02x".format(it) }

                val finalFileName = "$hashName.webp"
                val finalPath = Paths.get(uploadPath, finalFileName)

                // 4. 동일한 해시명을 가진 파일이 이미 있는지 확인
                if (!Files.exists(finalPath)) {
                    Files.write(finalPath, webpBytes)
                }

                // 5. 접근 가능한 URL 반환
                "http://localhost:$port/$finalFileName"
            }.subscribeOn(Schedulers.boundedElastic())
        )
    }
}