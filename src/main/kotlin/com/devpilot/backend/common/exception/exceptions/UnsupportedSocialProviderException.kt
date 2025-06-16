package com.devpilot.backend.common.exception.exceptions

import com.devpilot.backend.common.exception.CustomException
import org.springframework.http.HttpStatus

/**
 * 지원하지 않는 소셜 로그인 제공자(provider)일 때 발생하는 예외입니다.
 */
class UnsupportedSocialProviderException(registrationId: String) : CustomException(
    resultCode = "UNSUPPORTED_SOCIAL_PROVIDER",
    message = "지원하지 않는 소셜 로그인 서비스입니다: $registrationId",
    httpStatus = HttpStatus.BAD_REQUEST, // 400 Bad Request
)
