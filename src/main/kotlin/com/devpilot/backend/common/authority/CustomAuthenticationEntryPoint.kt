package com.devpilot.backend.common.authority

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException

class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    constructor() : this(ObjectMapper())

    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authException: AuthenticationException?,
    ) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpStatus.UNAUTHORIZED.value()

        val errorResponse = mutableMapOf<String, Any>()
        errorResponse["resultCode"] = "ACCESS_TOKEN_EXPIRED"
        errorResponse["message"] = "Unauthorized: Token has expired. Please log in again."
//        errorResponse["data"] = null
        errorResponse["httpStatus"] = HttpStatus.UNAUTHORIZED.value()

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
        response.writer.flush()
    }
}
