package com.devpilot.backend.common.exception.exceptions

import com.devpilot.backend.common.exception.CustomException
import org.springframework.http.HttpStatus

/**
 * 소셜 계정 연동 요청이 유효하지 않을 때 발생하는 예외입니다.
 * 예: 세션에 필요한 정보가 없거나, 'state' 값이 유효하지 않을 때
 */
class InvalidBindingRequestException : CustomException(
    resultCode = "INVALID_BINDING_REQUEST",
    message = "잘못된 계정 연동 요청입니다. 다시 시도해주세요.",
    httpStatus = HttpStatus.BAD_REQUEST, // 400 Bad Request
)
