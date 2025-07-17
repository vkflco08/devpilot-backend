package com.devpilot.backend.common.exception.exceptions

import com.devpilot.backend.common.exception.CustomException
import org.springframework.http.HttpStatus

class UserNotFoundException : CustomException(
    resultCode = "USER_NOT_FOUND",
    message = "유저를 찾을 수 없습니다.",
    httpStatus = HttpStatus.NOT_FOUND,
)
