package com.devpilot.backend.member.oauth

class GoogleOAuth2UserInfo(private val attributes: Map<String, Any>) : OAuth2UserInfo {
    override val id: String = attributes["sub"] as String
    override val name: String? = attributes["name"] as? String
    override val email: String? = attributes["email"] as? String
    override val imageUrl: String? = attributes["picture"] as? String
}
