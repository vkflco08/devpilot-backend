package com.devpilot.backend.common.authority

import com.devpilot.backend.common.dto.CustomSecurityUserDetails
import com.devpilot.backend.common.service.SignService
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.enum.AuthProvider
import com.devpilot.backend.member.repository.MemberRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2SuccessHandler(
    private val tokenProvider: JwtTokenProvider,
    private val memberRepository: MemberRepository,
    private val signService: SignService,
    @Value("\${cors.allowed.origins}") private val fronturl: List<String>
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oidcUser = authentication.principal as DefaultOidcUser
        val email = oidcUser.email
        val name = oidcUser.fullName
        val providerId = oidcUser.name // 고유 사용자 ID

        // DB에서 사용자 조회
        val user = memberRepository.findByEmail(email)
            ?: memberRepository.save(
                Member(
                    loginId = email,
                    password = null, // 소셜 로그인은 비번 없음
                    name = name,
                    email = email,
                    role = "USER", // 기본 권한 부여
                    phoneNumber = "N/A", // 소셜에서 제공되지 않음
                    department = "N/A", // 필수면 기본값 설정
                    description = "만나서 반갑습니다!",
                    provider = AuthProvider.GOOGLE,
                    providerId = providerId
                )
            ) // 없다면 저장

        val customUserDetails = CustomSecurityUserDetails(
            userId = user.id,
            userName = user.email,
            password = "", // 비밀번호 없음
            authorities = listOf(SimpleGrantedAuthority("ROLE_MEMBER"))
        )

        val authToken = UsernamePasswordAuthenticationToken(
            customUserDetails, null, customUserDetails.authorities
        )

        val accessToken = tokenProvider.createAccessToken(authToken)
        val refreshToken = tokenProvider.createRefreshToken(authToken)

        signService.saveRefreshToken(user, refreshToken)

        response.addCookie(Cookie("task-manager-refreshToken", refreshToken).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 60 * 60 * 24 * 7
        })

        println("OAuth2 Success Redirect URI: ${fronturl.first()}")

        val redirectUri = UriComponentsBuilder
            .fromUriString("${fronturl.first()}/oauth/callback")
            .queryParam("task-manager-accessToken", accessToken)
            .build()
            .toUriString()

        response.sendRedirect(redirectUri)
    }
}
