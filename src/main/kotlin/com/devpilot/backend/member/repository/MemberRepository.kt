package com.devpilot.backend.member.repository

import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.entity.MemberRole
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByLoginId(loginId: String): Member?
}

interface MemberRoleRepository : JpaRepository<MemberRole, Long>
