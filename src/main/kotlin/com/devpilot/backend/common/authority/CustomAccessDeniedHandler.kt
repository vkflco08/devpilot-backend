package com.devpilot.backend.common.authority

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.access.AccessDeniedHandler
import java.io.IOException

class CustomAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    private val logger = LoggerFactory.getLogger(CustomAccessDeniedHandler::class.java)

    @Throws(IOException::class)
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        val username = authentication?.name ?: "N/A"
        val authorities = authentication?.authorities?.map { it.authority } ?: emptyList()

        logger.error("👻 Access Denied: User '{}' with authorities {} tried to access protected resource: '{}'",
            username, authorities, request.requestURI, accessDeniedException) // 💡 상세 로그

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpStatus.FORBIDDEN.value() // 403 Forbidden

        val errorResponse = mutableMapOf<String, Any>()
        errorResponse["resultCode"] = "ACCESS_DENIED"
        errorResponse["message"] = "접근 권한이 없습니다. (인증은 되었으나, 해당 리소스에 대한 권한 없음)"
//        errorResponse["data"] = null
        errorResponse["httpStatus"] = HttpStatus.FORBIDDEN.value()

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
        response.writer.flush()
    }
}
