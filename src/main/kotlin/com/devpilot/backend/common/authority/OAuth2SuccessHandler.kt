package com.devpilot.backend.common.authority

import com.devpilot.backend.common.dto.CustomSecurityUserDetails
import com.devpilot.backend.common.service.SignService
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.entity.MemberAuthProvider
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

        val user: Member
        // DB에서 사용자 조회
        val existingUser = memberRepository.findByEmail(email)
        if(existingUser != null) {
            // 로컬로 가입한 기록이 있을 경우
            val newProvider = MemberAuthProvider(
                member = existingUser,
                provider = AuthProvider.GOOGLE,
                providerId = providerId
            )
            existingUser.addAuthProviderIfNotExists(newProvider)
            memberRepository.save(existingUser)
            user = existingUser
        } else {
            // 로컬로 가입한 기록 없음. 소셜 로그인으로 최초가입
            val member = Member(
                loginId = email,
                password = null,
                name = name,
                email = email,
                role = "USER",
                phoneNumber = "N/A",
                department = "N/A",
                description = "만나서 반갑습니다!"
            )

            val newProvider = MemberAuthProvider(
                member = member,
                provider = AuthProvider.GOOGLE,
                providerId = providerId
            )

            member.addAuthProviderIfNotExists(newProvider)
            memberRepository.save(member)
            user = member
        }

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
