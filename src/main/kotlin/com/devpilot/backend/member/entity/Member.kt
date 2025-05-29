package com.devpilot.backend.member.entity

import BaseEntity
import com.devpilot.backend.member.dto.MemberDtoResponse
import com.devpilot.backend.member.enum.AuthProvider
import com.memo.memo.common.status.ROLE
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import lombok.Getter

@Entity
@Getter
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "uk_member_login_id", columnNames = ["loginId"]),
        UniqueConstraint(name = "uk_member_email_provider", columnNames = ["email", "provider"])
    ],
)
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,
    @Column(nullable = false, length = 30, updatable = false)
    val loginId: String? = null,
    @Column(nullable = true, length = 100)
    val password: String? = null,
    @Column(nullable = false, length = 10)
    var name: String,
    @Column(nullable = false, length = 30)
    var email: String,
//    @Column(nullable = true)
//    var profileImage: String? = null,
    @Column(nullable = false, length = 30)
    var role: String,
    @Column(nullable = false, length = 15)
    var phoneNumber: String,
    @Column(nullable = false, length = 30)
    var department: String,
    @Column(nullable = false, length = 255)
    var description: String,

    // 소셜 로그인 관련 필드 추가
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val provider: AuthProvider = AuthProvider.LOCAL,

    // 소셜 로그인 제공자의 사용자 ID
    @Column(nullable = true, length = 100)
    val providerId: String? = null,
) : BaseEntity() {
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member")
    val memberRole: List<MemberRole>? = null

    // 소셜 로그인 사용자인지 확인하는 메서드
    fun isSocialUser(): Boolean = provider != AuthProvider.LOCAL

    // 로그인 가능한 사용자인지 확인 (loginId나 소셜 로그인 정보가 있어야 함)
    fun canLogin(): Boolean = loginId != null || (provider != AuthProvider.LOCAL && providerId != null)

    fun toDto(): MemberDtoResponse =
        MemberDtoResponse(
            id = this.id!!,
            loginId = this.loginId.toString(),
            name = this.name,
            email = this.email,
            createdDate = this.createdDate,
            role = this.role,
            phoneNumber = this.phoneNumber,
            department = this.department,
            description = this.description
        )

    companion object {
        fun createSocialMember(
            email: String,
            name: String,
            provider: AuthProvider,
            providerId: String,
            department: String = "일반",
            phoneNumber: String = "",
            description: String = "소셜 로그인 사용자"
        ): Member {
            return Member(
                loginId = null.toString(),
                password = null.toString(),
                name = name,
                email = email,
                role = "USER",
                phoneNumber = phoneNumber,
                department = department,
                description = description,
                provider = provider,
                providerId = providerId
            )
        }
    }
}

@Entity
class MemberRole(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    val role: ROLE,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = ForeignKey(name = "fk_member_role_member_id"))
    val member: Member,
) : BaseEntity()
