package com.devpilot.backend.common.authority

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class OAuth2FailureHandler : AuthenticationFailureHandler {

    private val logger = LoggerFactory.getLogger(OAuth2FailureHandler::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: org.springframework.security.core.AuthenticationException
    ) {
        logger.error("소셜 로그인 실패: {}", exception.message)

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "application/json;charset=UTF-8"
        response.writer.write("{\"error\": \"소셜 로그인에 실패했습니다: ${exception.message}\"}")
    }
}
