package com.devpilot.backend.common.exception.exceptions

import com.devpilot.backend.common.exception.CustomException
import org.springframework.http.HttpStatus

class ProjectNotFoundException : CustomException(
    resultCode = "PROJECT_NOT_FOUND",
    message = "프로젝트를 찾을 수 없습니다.",
    httpStatus = HttpStatus.UNAUTHORIZED,
)
