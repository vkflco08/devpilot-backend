package com.devpilot.backend.member.service

import com.devpilot.backend.member.dto.MemberPrincipal
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.enum.AuthProvider
import com.devpilot.backend.member.oauth.OAuth2UserInfo
import com.devpilot.backend.member.oauth.OAuth2UserInfoFactory
import com.devpilot.backend.member.repository.MemberRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
@Transactional
class CustomOAuth2UserService(
    private val memberRepository: MemberRepository
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val logger = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oauth2User = delegate.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId
        val userNameAttributeName = userRequest.clientRegistration
            .providerDetails.userInfoEndpoint.userNameAttributeName

        // OAuth2 사용자 정보 파싱
        val oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.attributes)

        if (oauth2UserInfo.email.isNullOrEmpty()) {
            throw OAuth2AuthenticationException("OAuth2 provider에서 이메일을 찾을 수 없습니다.")
        }

        val member = memberRepository.findByEmailAndProvider(oauth2UserInfo.email!!, getAuthProvider(registrationId))
            ?.let { updateExistingMember(it, oauth2UserInfo) }
            ?: registerNewMember(userRequest, oauth2UserInfo)

        return MemberPrincipal.create(member, oauth2User.attributes)
    }

    private fun registerNewMember(oAuth2UserRequest: OAuth2UserRequest, oAuth2UserInfo: OAuth2UserInfo): Member {
        val provider = getAuthProvider(oAuth2UserRequest.clientRegistration.registrationId)

        val member = Member.createSocialMember(
            email = oAuth2UserInfo.email!!,
            name = oAuth2UserInfo.name ?: "사용자",
            provider = provider,
            providerId = oAuth2UserInfo.id,
            department = "일반", // 기본값 설정
            phoneNumber = "", // 소셜 로그인에서는 전화번호를 받지 않으므로 기본값
            description = "${provider.name} 소셜 로그인 사용자"
        )

        logger.info("새로운 소셜 로그인 사용자 등록: ${member.email}, Provider: ${member.provider}")
        return memberRepository.save(member)
    }

    private fun updateExistingMember(existingMember: Member, oAuth2UserInfo: OAuth2UserInfo): Member {
        // 소셜 로그인 사용자 정보 업데이트
        existingMember.name = oAuth2UserInfo.name ?: existingMember.name
//        existingMember.profileImage = oAuth2UserInfo.imageUrl ?: existingMember.profileImage

        logger.info("기존 소셜 로그인 사용자 정보 업데이트: ${existingMember.email}")
        return memberRepository.save(existingMember)
    }

    private fun getAuthProvider(registrationId: String): AuthProvider {
        return when (registrationId.uppercase()) {
            "GOOGLE" -> AuthProvider.GOOGLE
            "KAKAO" -> AuthProvider.KAKAO
            else -> throw OAuth2AuthenticationException("지원하지 않는 소셜 로그인: $registrationId")
        }
    }
}
