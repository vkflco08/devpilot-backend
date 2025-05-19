package com.devpilot.backend.common.repository

import com.devpilot.backend.common.entity.MemberRefreshToken
import com.devpilot.backend.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRefreshTokenRepository : JpaRepository<MemberRefreshToken, Long> {
    fun findByMember(member: Member): MemberRefreshToken?
}
