package com.devpilot.backend.common.exception.exceptions

import com.devpilot.backend.common.exception.CustomException
import org.springframework.http.HttpStatus

class TaskNotFoundException : CustomException(
    resultCode = "TASK_NOT_FOUND",
    message = "해당 일정을 찾을 수 없습니다.",
    httpStatus = HttpStatus.NOT_FOUND,
)
