package com.devpilot.backend.member.oauth

interface OAuth2UserInfo {
    val id: String
    val name: String?
    val email: String?
    val imageUrl: String?
}