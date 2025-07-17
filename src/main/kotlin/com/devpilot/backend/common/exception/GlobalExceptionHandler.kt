package com.devpilot.backend.common.exception

import com.devpilot.backend.common.dto.BaseResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException

@RestControllerAdvice
class GlobalExceptionHandler {

    // ✨ 모든 CustomException을 처리하는 단일 핸들러
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException): ResponseEntity<BaseResponse<Nothing>> {
        val response = BaseResponse.error(
            code = ex.resultCode,
            message = ex.message,
            status = ex.httpStatus.value(),
            data = null
        )
        return ResponseEntity.status(ex.httpStatus).body(response)
    }

    // ✨ HttpMessageNotReadableException (JSON 요청 본문 파싱 오류)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<BaseResponse<Nothing>> {
        val response = BaseResponse.error<Nothing>(
            code = "INVALID_REQUEST_BODY",
            message = "요청 본문 형식이 올바르지 않습니다. 요청 필드를 확인해주세요.",
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // ✨ @Valid 검증 실패 시 (선택적, DTO 유효성 검사 오류 처리)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<BaseResponse<Nothing>> {
        val errors = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }
            .joinToString(", ")
        val response = BaseResponse.error<Nothing>(
            code = "VALIDATION_FAILED",
            message = "입력 값 유효성 검증에 실패했습니다: $errors",
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
    }

    // ✨ 기타 예상치 못한 모든 RuntimeException 처리 (제일 마지막에)
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<BaseResponse<Nothing>> {
        // 실제 운영 환경에서는 스택 트레이스 등 상세한 오류 정보를 로그로 남기는 것이 중요합니다.
        ex.printStackTrace() // 개발/디버깅용, 운영에서는 사용 주의
        val response = BaseResponse.error<Nothing>(
            code = "INTERNAL_SERVER_ERROR",
            message = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
