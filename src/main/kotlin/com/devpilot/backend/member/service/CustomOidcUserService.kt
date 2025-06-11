package com.devpilot.backend.member.service

import com.devpilot.backend.member.dto.MemberPrincipal
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.entity.MemberAuthProvider
import com.devpilot.backend.member.enum.AuthProvider
import com.devpilot.backend.member.oauth.OAuth2UserInfo
import com.devpilot.backend.member.oauth.OAuth2UserInfoFactory
import com.devpilot.backend.member.repository.MemberAuthProviderRepository
import com.devpilot.backend.member.repository.MemberRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service

@Service
@Transactional
class CustomOidcUserService(
    private val memberRepository: MemberRepository,
    private val memberAuthProviderRepository: MemberAuthProviderRepository
) : OAuth2UserService<OidcUserRequest, OidcUser> {

    private val logger = LoggerFactory.getLogger(CustomOidcUserService::class.java)

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        println("✅ CustomOidcUserService loadUser 호출됨")
        val delegate = OidcUserService()
        val oidcUser = delegate.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId
        val oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oidcUser.attributes)

        val oauthEmail = oauth2UserInfo.email
        if (oauthEmail.isNullOrEmpty()) {
            throw OAuth2AuthenticationException("OAuth2 provider에서 이메일을 찾을 수 없습니다.")
        }

        val provider = getAuthProvider(registrationId)

        // ✅ 일반 소셜 로그인 흐름
        val existingAuthProvider = memberAuthProviderRepository.findByEmailAndProvider(
            email = oauthEmail,
            provider = provider
        )

        val member = existingAuthProvider?.member ?: registerNewMember(userRequest, oauth2UserInfo)

        logger.info("[소셜 계정 로그인] 사용자: ${member.email}, Provider: $provider")
        return MemberPrincipal.create(member, oidcUser.attributes, oidcUser.idToken, oidcUser.userInfo)
    }

    private fun registerNewMember(userRequest: OidcUserRequest, oAuth2UserInfo: OAuth2UserInfo): Member {
        val provider = getAuthProvider(userRequest.clientRegistration.registrationId)

        val member = Member.createSocialMember(
            email = oAuth2UserInfo.email!!,
            name = oAuth2UserInfo.name ?: "사용자",
            providerId = oAuth2UserInfo.id,
            department = "일반",
            phoneNumber = "",
            description = "소셜 로그인 사용자"
        )

        val memberAuthProvider = MemberAuthProvider(
            member = member,
            provider = provider,
            providerId = oAuth2UserInfo.id
        )
        member.addAuthProviderIfNotExists(memberAuthProvider)

        logger.info("새로운 소셜 로그인 사용자 등록: ${member.email}, Provider: $provider")
        return memberRepository.save(member)
    }

    private fun getAuthProvider(registrationId: String): AuthProvider {
        return when (registrationId.uppercase()) {
            "GOOGLE" -> AuthProvider.GOOGLE
            "KAKAO" -> AuthProvider.KAKAO
            else -> throw OAuth2AuthenticationException("지원하지 않는 소셜 로그인: $registrationId")
        }
    }
}
