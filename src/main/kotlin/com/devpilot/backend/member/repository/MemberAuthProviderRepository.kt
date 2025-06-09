package com.devpilot.backend.member.repository

import com.devpilot.backend.member.entity.MemberAuthProvider
import com.devpilot.backend.member.enum.AuthProvider
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MemberAuthProviderRepository : JpaRepository<MemberAuthProvider, Long> {
    @Query("""
        SELECT m FROM MemberAuthProvider m 
        WHERE m.member.email = :email AND m.provider = :provider
    """)
    fun findByEmailAndProvider(email: String, provider: AuthProvider): MemberAuthProvider?
}
