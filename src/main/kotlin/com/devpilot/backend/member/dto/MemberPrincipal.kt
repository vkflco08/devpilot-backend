package com.devpilot.backend.member.dto

import com.devpilot.backend.member.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

data class MemberPrincipal(
    val member: Member,
    private val attributes: Map<String, Any>? = null
) : OAuth2User, UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${member.role}"))

    override fun getPassword(): String? = member.password
    override fun getUsername(): String = member.email
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
    override fun getAttributes(): Map<String, Any>? = attributes
    override fun getName(): String = member.name

    companion object {
        fun create(member: Member, attributes: Map<String, Any>? = null): MemberPrincipal {
            return MemberPrincipal(member, attributes)
        }
    }
}
