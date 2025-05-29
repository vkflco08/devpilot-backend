package com.devpilot.backend.common.service

import com.devpilot.backend.common.dto.CustomSecurityUserDetails
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.repository.MemberRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        memberRepository.findByLoginId(username)
            ?.let { createUserDetails(it) } ?: throw UsernameNotFoundException("해당 유저는 없습니다.")

    private fun createUserDetails(member: Member): UserDetails =
        CustomSecurityUserDetails(
            member.id,
            member.loginId.toString(),
            passwordEncoder.encode(member.password),
            member.memberRole!!.map { SimpleGrantedAuthority("ROLE_${it.role}") },
        )

    fun loadUserById(userId: Long): UserDetails {
        val member = memberRepository.findById(userId)
            .orElseThrow { UsernameNotFoundException("사용자를 찾을 수 없습니다.") }

        return CustomSecurityUserDetails(
            userId = member.id,
            userName = member.email,
            password = "", // JWT 기반이라 비밀번호는 필요 없음
            authorities = listOf(SimpleGrantedAuthority("ROLE_MEMBER"))
        )
    }
}
