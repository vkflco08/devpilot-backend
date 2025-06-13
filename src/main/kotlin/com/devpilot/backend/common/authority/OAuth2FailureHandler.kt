package com.devpilot.backend.common.authority

import com.devpilot.backend.common.exception.CustomException
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

@Component
class OAuth2FailureHandler(
    @Value("\${cors.allowed.origins}") private val fronturl: List<String>
) : AuthenticationFailureHandler {

    private val logger = LoggerFactory.getLogger(OAuth2FailureHandler::class.java)

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        var errorMessage = "소셜 로그인에 실패했습니다. 다시 시도해주세요."
        var httpStatus = HttpServletResponse.SC_UNAUTHORIZED

        if (exception is OAuth2AuthenticationException) {
            val cause = exception.cause

            if (cause is CustomException) {
                errorMessage = cause.message
                httpStatus = cause.httpStatus.value()
                logger.warn("CustomException 처리됨 (Result Code: ${cause.resultCode}): {}", cause.message)
            } else {
                when (exception.message) {
                    "잘못된 계정 연동 요청입니다" -> {
                        errorMessage = "계정 연동 요청이 잘못되었습니다. 유효하지 않은 요청입니다."
                        logger.warn("OAuth2 연동 실패 (잘못된 요청): {}", exception.message)
                    }
                    "사용자를 찾을 수 없습니다" -> {
                        errorMessage = "계정을 찾을 수 없습니다. 회원가입 또는 다른 계정으로 로그인해주세요."
                        logger.warn("OAuth2 연동 실패 (사용자 없음): {}", exception.message)
                    }
                    "이미 다른 계정에 연동된 소셜 계정입니다." -> {
                        errorMessage = "이미 다른 계정에 연동된 소셜 계정입니다. 해당 계정으로 로그인해주세요."
                        logger.warn("OAuth2 연동 실패 (중복 연동): {}", exception.message)
                    }
                    else -> if (exception.message?.startsWith("지원하지 않는 소셜 로그인") == true) {
                        errorMessage = "지원하지 않는 소셜 로그인 서비스입니다. 다른 방법을 시도해주세요."
                        logger.warn("OAuth2 연동 실패 (지원하지 않는 서비스): {}", exception.message)
                    } else {
                        errorMessage = "소셜 로그인 중 알 수 없는 오류가 발생했습니다: ${exception.message}"
                        logger.error("소셜 로그인 실패 (알 수 없는 OAuth2 에러): {}", exception.message, exception)
                    }
                }
            }
        } else {
            errorMessage = "로그인 처리 중 예상치 못한 오류가 발생했습니다: ${exception.message}"
            logger.error("소셜 로그인 실패 (일반 Authentication 에러): {}", exception.message, exception)
        }

        response.status = httpStatus
        response.contentType = "application/json;charset=UTF-8"

        response.writer.write("{\"error\": \"$errorMessage\"}")

        val redirectUri = UriComponentsBuilder
            .fromUriString("${fronturl.first()}/mypage")
            .build()
            .toUriString()

        response.sendRedirect(redirectUri)
    }
}
