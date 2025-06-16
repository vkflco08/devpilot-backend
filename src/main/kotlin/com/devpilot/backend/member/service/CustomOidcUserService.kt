package com.devpilot.backend.member.service

import com.devpilot.backend.common.exception.exceptions.InvalidBindingRequestException
import com.devpilot.backend.common.exception.exceptions.SocialAccountAlreadyLinkedException
import com.devpilot.backend.common.exception.exceptions.UnsupportedSocialProviderException
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.common.repository.CustomAuthorizationRequestRepository.Companion.SPRING_SECURITY_OAUTH2_BINDING_DATA
import com.devpilot.backend.member.dto.MemberPrincipal
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.entity.MemberAuthProvider
import com.devpilot.backend.member.enum.AuthProvider
import com.devpilot.backend.member.oauth.OAuth2UserInfo
import com.devpilot.backend.member.oauth.OAuth2UserInfoFactory
import com.devpilot.backend.member.repository.MemberAuthProviderRepository
import com.devpilot.backend.member.repository.MemberRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

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

        val provider = getAuthProvider(registrationId)

        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
            ?: throw IllegalStateException("HttpServletRequest not available in RequestContextHolder.")

        println("Session ID (load): ${request.session.id}")
        println("Session attributes before extract: ${request.session.attributeNames.toList()}")

        // IdP로부터 콜백된 state (Spring Security의 원래 state)를 가져옵니다.
        // 이 state는 CustomAuthorizationRequestRepository에서 bind 정보를 저장할 때 키로 사용됩니다.
        val returnedSpringSecurityState = request.getParameter("state")

        println("반환된 state (IdP로부터): ${returnedSpringSecurityState}")

        val BIND_STATE_PREFIX = "bind:" // 연동 요청을 식별하는 접두사

        var isBindingRequest = false
        var bindingData: Map<String, Any>? = null

        if (returnedSpringSecurityState != null) {
            // Spring Security의 원래 state를 키로 사용하여 세션에 저장된 Map 형태의 바인딩 데이터를 가져옵니다.
            bindingData = request.session.getAttribute(
                SPRING_SECURITY_OAUTH2_BINDING_DATA + "_" + returnedSpringSecurityState
            ) as? Map<String, Any>

            if (bindingData != null) {
                val bindStateFromMap = bindingData["bindState"] as? String
                val userIdFromMap = bindingData["userId"] as? Long

                // bindState와 userId가 모두 유효하고, bindState가 "bind:"로 시작하면 연동 요청으로 확정
                if (bindStateFromMap != null && bindStateFromMap.startsWith(BIND_STATE_PREFIX) && userIdFromMap != null) {
                    isBindingRequest = true
                }
                // 세션에서 사용한 바인딩 데이터 제거 (CustomAuthorizationRequestRepository에서도 제거되지만, 혹시 모를 경우에 대비)
                request.session.removeAttribute(
                    SPRING_SECURITY_OAUTH2_BINDING_DATA + "_" + returnedSpringSecurityState
                )
                println("DEBUG: Removed binding data map from session in CustomOidcUserService.")
            }
        }

        println("추출된 연동용 데이터: ${bindingData}")
        println("연동 요청 여부: ${isBindingRequest}")

        return if (isBindingRequest && bindingData != null) {
            bindSocialAccount(bindingData, oauth2UserInfo, provider, oidcUser, request)
        } else {
            loginOrRegister(oauth2UserInfo, provider, userRequest, oidcUser)
        }
    }

    private fun bindSocialAccount(
        bindingData: Map<String, Any>,
        userInfo: OAuth2UserInfo,
        provider: AuthProvider,
        oidcUser: OidcUser,
        request: HttpServletRequest

    ): OidcUser {
        val bindState = bindingData["bindState"] as? String // 바인딩을 위한 고유 state (예: "bind:UUID")

        if (bindState == null) {
            throw OAuth2AuthenticationException(
                OAuth2Error(InvalidBindingRequestException().resultCode, InvalidBindingRequestException().message, null),
                InvalidBindingRequestException()
            )
        }

        val userId = request.session.getAttribute(bindState) as? Long
            ?: throw OAuth2AuthenticationException(
                OAuth2Error(InvalidBindingRequestException().resultCode, InvalidBindingRequestException().message, null),
                InvalidBindingRequestException()
            )

        request.session.removeAttribute(bindState)
        println("DEBUG: bindSocialAccount에서 세션으로부터 가져온 userId: $userId (bindState: $bindState)")

        // 사용자를 찾을 수 없을 때
        val userNotFoundEx = UserNotFoundException()
        val member = memberRepository.findByIdOrNull(userId)
            ?: throw OAuth2AuthenticationException(OAuth2Error(userNotFoundEx.resultCode, userNotFoundEx.message, null), userNotFoundEx)

        // 이미 가입한 소셜 계정일때
        val alreadyLinkedEx = SocialAccountAlreadyLinkedException()
        val existingProvider = memberAuthProviderRepository.findByProviderAndProviderId(provider, userInfo.id)
        if (existingProvider != null) {
            throw OAuth2AuthenticationException(OAuth2Error(alreadyLinkedEx.resultCode, alreadyLinkedEx.message, null), alreadyLinkedEx)
        }

        val authProvider = MemberAuthProvider(
            member = member,
            provider = provider,
            providerId = userInfo.id
        )
        member.addAuthProviderIfNotExists(authProvider)
        memberRepository.save(member)

        logger.info("🔗 계정 연동 완료: userId = $userId, provider = $provider")
        return MemberPrincipal.create(member, oidcUser.attributes, oidcUser.idToken, oidcUser.userInfo)
    }

    private fun loginOrRegister(
        userInfo: OAuth2UserInfo,
        provider: AuthProvider,
        userRequest: OidcUserRequest,
        oidcUser: OidcUser
    ): OidcUser {
        val existingAuthProvider = memberAuthProviderRepository.findByProviderAndProviderId(
            provider, userInfo.id
        )
        val member = existingAuthProvider?.member ?: registerNewMember(userRequest, userInfo)

        logger.info("[소셜 계정 로그인] 사용자: ${member.email}, Provider: $provider")
        return MemberPrincipal.create(member, oidcUser.attributes, oidcUser.idToken, oidcUser.userInfo)
    }

    private fun registerNewMember(userRequest: OidcUserRequest, oAuth2UserInfo: OAuth2UserInfo): Member {
        val provider = getAuthProvider(userRequest.clientRegistration.registrationId)

        val member = Member.createSocialMember(
            email = oAuth2UserInfo.email!!,
            name = oAuth2UserInfo.name ?: "사용자",
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
            else -> {
                val unsupportedEx = UnsupportedSocialProviderException(registrationId)
                throw OAuth2AuthenticationException(OAuth2Error(unsupportedEx.resultCode, unsupportedEx.message, null), unsupportedEx)
            }
        }
    }
}
