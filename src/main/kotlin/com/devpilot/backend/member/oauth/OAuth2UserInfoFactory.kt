package com.devpilot.backend.member.oauth

import org.springframework.security.oauth2.core.OAuth2AuthenticationException

object OAuth2UserInfoFactory {
    fun getOAuth2UserInfo(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when (registrationId.lowercase()) {
            "google" -> GoogleOAuth2UserInfo(attributes)
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            else -> throw OAuth2AuthenticationException("지원하지 않는 소셜 로그인: $registrationId")
        }
    }
}
