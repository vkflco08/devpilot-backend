package com.devpilot.backend.common.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class InvalidInputException(
    val fieldName: String = "",
    message: String = "Invalid Input",
) : RuntimeException(message)
