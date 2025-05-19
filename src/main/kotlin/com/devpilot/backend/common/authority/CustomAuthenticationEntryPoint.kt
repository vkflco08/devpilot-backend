package com.devpilot.backend.common.authority

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import java.io.IOException

class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {
    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authException: AuthenticationException?,
    ) {
//        // 리디렉션 URL 설정
//        val redirectUrl = "/api/member/login"
//
//        println("redirect to login")
//
//        // 클라이언트로 리디렉션
//        response.sendRedirect(redirectUrl)
//
        // 401 상태 코드와 메시지를 클라이언트로 전송
        response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized: Token has expired.")
    }
}
