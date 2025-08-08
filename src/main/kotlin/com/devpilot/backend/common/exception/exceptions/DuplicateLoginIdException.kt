package com.devpilot.backend.common.exception.exceptions

import com.devpilot.backend.common.exception.CustomException
import org.springframework.http.HttpStatus

class DuplicateLoginIdException : CustomException(
    resultCode = "DUPLICATE_LOGIN_ID",
    message = "이미 등록된 ID입니다.",
    httpStatus = HttpStatus.CONFLICT,  // 409 Conflict
)
