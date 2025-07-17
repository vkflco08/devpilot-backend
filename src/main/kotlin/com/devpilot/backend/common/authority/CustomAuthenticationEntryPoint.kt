package com.devpilot.backend.common.authority

import com.devpilot.backend.common.dto.BaseResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import java.io.IOException

class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authException: AuthenticationException?,
    ) {

        logger.info("CustomAuthenticationEntryPoint called for URI: {}. Auth Exception: {}", request?.requestURI, authException?.message) // ✨ 로그 추가

        // 인증 실패 시 (401 Unauthorized) 응답 설정
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = BaseResponse.error<Any>(
            code = "ACCESS_TOKEN_EXPIRED",
            message = "Access Token이 만료되었습니다. Refresh Token으로 갱신이 필요합니다.",
            status = HttpStatus.UNAUTHORIZED.value(),
            data = null // 필요시 추가 데이터
        )

        // JSON 직렬화하여 응답 본문에 쓰기
        objectMapper.writeValue(response.writer, errorResponse)
    }
}
