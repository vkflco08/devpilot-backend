package com.devpilot.backend.member.repository

import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.entity.MemberRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByLoginId(loginId: String): Member?

    // 소셜 로그인용 메서드 추가
    fun findByEmail(email: String): Member?

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.authProviders WHERE m.id = :id")
    fun findWithAuthProvidersById(id: Long): Member?
}

interface MemberRoleRepository : JpaRepository<MemberRole, Long>
