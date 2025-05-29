package com.devpilot.backend.member.oauth

class KakaoOAuth2UserInfo(private val attributes: Map<String, Any>) : OAuth2UserInfo {
    private val kakaoAccount = attributes["kakao_account"] as? Map<*, *>
    private val profile = kakaoAccount?.get("profile") as? Map<*, *>

    override val id: String = attributes["id"].toString()
    override val name: String? = profile?.get("nickname") as? String
    override val email: String? = kakaoAccount?.get("email") as? String
    override val imageUrl: String? = profile?.get("profile_image_url") as? String
}
