package com.devpilot.backend.member.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.enum.AuthProvider
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDateTime

data class MemberDtoRequest(
    var id: Long?,
    @field:NotBlank
    @JsonProperty("loginId")
    private val _loginId: String?,
    @field:NotBlank
    @field:Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#\$%^&*])[a-zA-Z0-9!@#\$%^&*]{8,20}\$",
        message = "영문, 숫자, 특수문자를 포함한 8~20자리로 입력해주세요",
    )
    @JsonProperty("password")
    private val _password: String?,
    @field:NotBlank
    @JsonProperty("name")
    private val _name: String,
    @field:NotBlank
    @field:Email
    @JsonProperty("email")
    private val _email: String,
    @field:NotBlank
    @JsonProperty("role")
    private val _role: String,
    @field:NotBlank
    @field:Pattern(
        regexp = "^01[016789]-\\d{3,4}-\\d{4}\$",
        message = "전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678"
    )
    @JsonProperty("phoneNumber")
    private val _phoneNumber: String,
    @field:NotBlank
    @JsonProperty("department")
    private val _department: String,
    @field:NotBlank
    @JsonProperty("description")
    private val _description: String,
) {
    val loginId: String
        get() = _loginId!!
    val password: String
        get() = _password!!
    val name: String
        get() = _name
    val email: String
        get() = _email
    val role: String
        get() = _role
    val phoneNumber: String
        get() = _phoneNumber
    val department: String
        get() = _department
    val description: String
        get() = _description

    fun toEntity(): Member = Member(id, loginId, password, name, email, role, phoneNumber, department, description)
}

data class LoginDto(
    @field:NotBlank
    @JsonProperty("loginId")
    private val _loginId: String?,
    @field:NotBlank
    @JsonProperty("password")
    private val _password: String?,
) {
    val loginId: String
        get() = _loginId!!
    val password: String
        get() = _password!!
}

data class MemberDtoResponse(
    val id: Long,
    val loginId: String,
    val name: String,
    val email: String,
    val createdDate: LocalDateTime?,
    val role: String,
    val phoneNumber: String,
    val department: String,
    val description: String,
    val providers: List<AuthProvider>,
)

data class MemberProfileDtoRequest(
    @field:NotBlank(message = "이름은 필수 항목입니다.")
    @JsonProperty("name")
    var name: String,
//    var profileImage: String? = null,
    // 프로필 이미지 URL (선택)
    @field:NotBlank
    @JsonProperty("role")
    var role: String,
    @field:NotBlank
    @field:Pattern(
        regexp = "^01[016789]-\\d{3,4}-\\d{4}\$",
        message = "전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678"
    )
    @JsonProperty("phoneNumber")
    var phoneNumber: String,
    @field:NotBlank
    @JsonProperty("department")
    var department: String,
    @field:NotBlank
    @JsonProperty("description")
    var description: String,
) {
    fun toEntity(existingMember: Member): Member =
        existingMember.apply {
            this.name = this@MemberProfileDtoRequest.name
//            this.profileImage = this@MemberProfileDtoRequest.profileImage
            this.role = this@MemberProfileDtoRequest.role
            this.phoneNumber = this@MemberProfileDtoRequest.phoneNumber
            this.department = this@MemberProfileDtoRequest.department
            this.description = this@MemberProfileDtoRequest.description
        }
}
