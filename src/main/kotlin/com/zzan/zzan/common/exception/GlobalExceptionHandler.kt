package com.zzan.zzan.common.exception

import com.zzan.zzan.common.response.ApiResponse
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler : KLogging() {
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Custom exception occurred: ${e.message} ", e)

        val errorResponse = ApiResponse.error<Nothing>(e.message ?: "오류가 발생했습니다");
        return ResponseEntity.status(e.status)
            .body(errorResponse)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("Invalid argument: ${e.message}")

        val errorResponse = ApiResponse.error<Nothing>(e.message ?: "잘못된 요청입니다")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnhandledException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unhandled exception occurred: ${e.message}", e)

        val errorResponse = ApiResponse.error<Nothing>("서버 내부 오류가 발생했습니다")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorResponse)
    }
}
