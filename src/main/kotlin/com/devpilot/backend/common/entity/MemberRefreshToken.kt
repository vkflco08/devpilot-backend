package com.devpilot.backend.common.entity

import BaseEntity
import com.devpilot.backend.member.entity.Member
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import lombok.Getter
import lombok.Setter

@Entity
@Getter
@Setter
class MemberRefreshToken(
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    val member: Member?,

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    var refreshToken: String,
) : BaseEntity() {
    @Id
    var memberId: Long? = null
}
