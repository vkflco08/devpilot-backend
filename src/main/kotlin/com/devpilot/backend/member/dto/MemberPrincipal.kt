package com.devpilot.backend.member.dto

import com.devpilot.backend.member.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser

class MemberPrincipal(
    private val member: Member,
    private val authorities: Collection<GrantedAuthority> = listOf(),
    private val attributes: Map<String, Any> = mapOf(),
    private val idToken: OidcIdToken? = null,
    private val userInfo: OidcUserInfo? = null
) : OidcUser {

    override fun getName(): String = member.id.toString()

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getClaims(): Map<String, Any> = attributes // 보통 claims = attributes 로 처리 가능

    override fun getIdToken(): OidcIdToken = idToken
        ?: throw IllegalStateException("ID Token is required for OIDC user")

    override fun getUserInfo(): OidcUserInfo? = userInfo

    fun getMember(): Member = member

    companion object {
        fun create(
            member: Member,
            attributes: Map<String, Any>,
            idToken: OidcIdToken,
            userInfo: OidcUserInfo?,
        ): MemberPrincipal {
            return MemberPrincipal(
                member = member,
                authorities = listOf(), // 필요하면 ROLE_ 추가
                attributes = attributes,
                idToken = idToken,
                userInfo = userInfo
            )
        }
    }
}