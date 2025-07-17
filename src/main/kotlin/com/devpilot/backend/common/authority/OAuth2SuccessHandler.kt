package com.devpilot.backend.common.authority

import com.devpilot.backend.common.dto.CustomSecurityUserDetails
import com.devpilot.backend.common.service.SignService
import com.devpilot.backend.member.dto.MemberPrincipal
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
    private val tokenProvider: JwtTokenProvider,
    private val signService: SignService,
    @Value("\${cors.allowed.origins}") private val fronturl: List<String>
) : AuthenticationSuccessHandler {

    @Transactional
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val principal = authentication.principal as? MemberPrincipal
            ?: throw IllegalStateException("Unsupported principal type")

        val member = principal.getMember()

        val customUserDetails = CustomSecurityUserDetails(
            userId = member.id,
            userName = member.email,
            password = "", // 비밀번호 없음
            authorities = listOf(SimpleGrantedAuthority("ROLE_MEMBER"))
        )

        val authToken = UsernamePasswordAuthenticationToken(
            customUserDetails, null, customUserDetails.authorities
        )

        val accessToken = tokenProvider.createAccessToken(authToken)
        val refreshToken = tokenProvider.createRefreshToken(authToken)

        signService.saveRefreshToken(member, refreshToken)

        response.addCookie(Cookie("task-pilot-refreshToken", refreshToken).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = (REFRESH_EXPIRATION_MILLISECONDS / 1000).toInt()
        })

        val redirectUri = UriComponentsBuilder
            .fromUriString("${fronturl.first()}/oauth/callback")
            .queryParam("task-pilot-accessToken", accessToken)
            .build()
            .toUriString()

        response.sendRedirect(redirectUri)
    }
}
