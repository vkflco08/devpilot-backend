package com.devpilot.backend.member.service

import com.devpilot.backend.common.exception.exceptions.InvalidBindingRequestException
import com.devpilot.backend.common.exception.exceptions.SocialAccountAlreadyLinkedException
import com.devpilot.backend.common.exception.exceptions.UnsupportedSocialProviderException
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
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

import com.devpilot.backend.common.repository.CustomAuthorizationRequestRepository.Companion.SPRING_SECURITY_OAUTH2_AUTHORIZATION_REQUEST_BINDING_STATE

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

        // 연동 요청인지 확인합니다.
        // 방법 1: Google로부터 반환된 state 값이 "bind:"로 시작하는지 확인 (컨트롤러에서 그렇게 보냈다면)
        // 방법 2: 세션에 저장된 연동 플래그를 확인
        // 지금 컨트롤러에서 Google의 `state` 파라미터에 `bind:UUID`를 통째로 넣고 있으므로, 방법 1을 사용합니다.
        // 그리고 `CustomAuthorizationRequestRepository`에서 `SPRING_SECURITY_OAUTH2_AUTHORIZATION_REQUEST_BINDING_STATE`
        // 키로 `true` 플래그를 저장했으므로, 이 플래그도 함께 확인하는 것이 좋습니다.

        var isBindingRequest = false
        var extractedBindingState: String? = null // 이 변수에 `bind:UUID` 문자열이 저장될 것

        // 1. 세션에 연동 요청 플래그가 있는지 확인
        val bindingFlag = request.session.getAttribute(SPRING_SECURITY_OAUTH2_AUTHORIZATION_REQUEST_BINDING_STATE) as? Boolean
        if (bindingFlag == true) {
            isBindingRequest = true
            // 플래그를 제거하여 다음 요청에 영향을 주지 않도록 합니다.
            request.session.removeAttribute(SPRING_SECURITY_OAUTH2_AUTHORIZATION_REQUEST_BINDING_STATE)
            println("DEBUG: Removed binding request flag from session.")
        }

        // 2. IdP로부터 반환된 'state' 파라미터 자체가 "bind:"로 시작하는지 확인
        //    (컨트롤러에서 그렇게 구성하여 Google에 보냈기 때문)
        if (returnedSpringSecurityState != null && returnedSpringSecurityState.startsWith(BIND_STATE_PREFIX)) {
            isBindingRequest = true
            extractedBindingState = returnedSpringSecurityState // 이 값이 "bind:UUID"
        }

        println("추출된 연동용 state (bind:UUID): ${extractedBindingState}") // "bind:UUID"가 여기 찍혀야 합니다.
        println("연동 요청 여부: ${isBindingRequest}")

        // extractedBindingState가 null이 아니고, "bind:" 접두사로 시작하며, 연동 요청으로 판단되면
        // bindSocialAccount 호출, 아니면 loginOrRegister 호출
        // bindSocialAccount에는 "bind:UUID" 문자열이 state로 전달됩니다.
        return if (isBindingRequest && extractedBindingState != null && extractedBindingState.startsWith(BIND_STATE_PREFIX)) {
            // 이제 extractedBindingState는 "bind:UUID" 값을 가집니다.
            bindSocialAccount(extractedBindingState, oauth2UserInfo, provider, oidcUser, request)
        } else {
            loginOrRegister(oauth2UserInfo, provider, userRequest, oidcUser)
        }
    }

    private fun bindSocialAccount(
        state: String,
        userInfo: OAuth2UserInfo,
        provider: AuthProvider,
        oidcUser: OidcUser,
        request: HttpServletRequest
    ): OidcUser {
        val userId = request.session.getAttribute(state) as? Long
            ?: throw OAuth2AuthenticationException(
                OAuth2Error(InvalidBindingRequestException().resultCode, InvalidBindingRequestException().message, null),
                InvalidBindingRequestException()
            )

        println("DEBUG: bindSocialAccount에서 세션으로부터 가져온 userId: $userId")

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
