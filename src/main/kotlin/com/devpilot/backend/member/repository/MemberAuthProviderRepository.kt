package com.devpilot.backend.member.repository

import com.devpilot.backend.member.entity.MemberAuthProvider
import com.devpilot.backend.member.enum.AuthProvider
import org.springframework.data.jpa.repository.JpaRepository

interface MemberAuthProviderRepository : JpaRepository<MemberAuthProvider, Long> {
    fun findByProviderAndProviderId(provider: AuthProvider, providerId: String): MemberAuthProvider?
    fun findByMemberIdAndProvider(memberId: Long, provider: AuthProvider): MemberAuthProvider?
}
