package com.devpilot.backend.common.exception.exceptions

import com.devpilot.backend.common.exception.CustomException
import org.springframework.http.HttpStatus

class TokenValidationException(
    resultCode: String,
    message: String,
    httpStatus: HttpStatus = HttpStatus.UNAUTHORIZED
) : CustomException(resultCode, message, httpStatus)
