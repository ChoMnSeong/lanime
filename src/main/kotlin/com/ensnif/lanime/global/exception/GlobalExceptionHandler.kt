package com.ensnif.lanime.global.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import reactor.core.publisher.Mono

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    protected fun handleBusinessException(e: BusinessException): Mono<ResponseEntity<ErrorResponse>> {
        val response = ErrorResponse(
            status = e.errorCode.status.value(),
            code = e.errorCode.code,
            message = e.errorCode.message
        )
        return Mono.just(ResponseEntity(response, e.errorCode.status))
    }

    @ExceptionHandler(WebExchangeBindException::class)
    protected fun handleBindException(e: WebExchangeBindException): Mono<ResponseEntity<ErrorResponse>> {
        val message = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            code = "V001",
            message = message
        )
        return Mono.just(ResponseEntity(response, HttpStatus.BAD_REQUEST))
    }

    @ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): Mono<ResponseEntity<ErrorResponse>> {
        log.error("Unhandled Exception: ", e)
        val response = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            code = "COMMON_500",
            message = "서버 내부 오류가 발생했습니다."
        )
        return Mono.just(ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR))
    }
}