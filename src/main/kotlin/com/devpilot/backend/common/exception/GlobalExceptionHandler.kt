package com.devpilot.backend.common.exception

import com.devpilot.backend.common.dto.BaseResponse
import com.devpilot.backend.common.exception.exceptions.DuplicateLoginIdException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateLoginIdException::class)
    fun handleDuplicateLoginIdException(ex: DuplicateLoginIdException): ResponseEntity<BaseResponse<Nothing>> {
        val response = BaseResponse.error<Nothing>(
            code = "DUPLICATE_LOGIN_ID",
            message = ex.message ?: "이미 등록된 ID입니다.",
            status = HttpStatus.CONFLICT.value()
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    // 다른 커스텀 예외 처리들도 여기에 추가 가능
}