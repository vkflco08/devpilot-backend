package com.devpilot.backend.common.exception

import org.springframework.http.HttpStatus

open class CustomException(
    val resultCode: String,
    override val message: String,
    val httpStatus: HttpStatus,
) : RuntimeException(message)
