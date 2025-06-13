package com.devpilot.backend.common.exception.exceptions

import com.devpilot.backend.common.exception.CustomException
import org.springframework.http.HttpStatus

/**
 * 이미 다른 계정에 연동된 소셜 계정으로 로그인 시도 시 발생하는 예외입니다.
 */
class SocialAccountAlreadyLinkedException : CustomException(
    resultCode = "ACCOUNT_ALREADY_LINKED",
    message = "이미 다른 계정에 연동된 소셜 계정입니다. 해당 계정으로 로그인해주세요.",
    httpStatus = HttpStatus.CONFLICT, // 409 Conflict
)