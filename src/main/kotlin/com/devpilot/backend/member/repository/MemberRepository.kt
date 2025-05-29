package com.devpilot.backend.member.repository

import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.entity.MemberRole
import com.devpilot.backend.member.enum.AuthProvider
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByLoginId(loginId: String): Member?

    // 소셜 로그인용 메서드 추가
    fun findByEmail(email: String): Member?
    fun findByEmailAndProvider(email: String, provider: AuthProvider): Member?
    fun findByProviderAndProviderId(provider: AuthProvider, providerId: String): Member?

    // 중복 체크용 메서드
    fun existsByLoginId(loginId: String): Boolean
    fun existsByEmailAndProvider(email: String, provider: AuthProvider): Boolean

}

interface MemberRoleRepository : JpaRepository<MemberRole, Long>
