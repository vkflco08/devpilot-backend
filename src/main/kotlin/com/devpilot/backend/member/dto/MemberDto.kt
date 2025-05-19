package com.devpilot.backend.member.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.devpilot.backend.member.entity.Member
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
    private val _name: String?,
    @field:NotBlank
    @field:Email
    @JsonProperty("email")
    private val _email: String?,
) {
    val loginId: String
        get() = _loginId!!
    val password: String
        get() = _password!!
    val name: String
        get() = _name!!
    val email: String
        get() = _email!!

    fun toEntity(): Member = Member(id, loginId, password, name, email)
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
)

data class MemberProfileDtoRequest(
    var id: Long? = null,
    @field:NotBlank(message = "이름은 필수 항목입니다.")
    var name: String,
    @field:NotBlank(message = "이메일은 필수 항목입니다.")
    @field:Email(message = "유효하지 않은 이메일 형식입니다.")
    var email: String,
    var profileImage: String? = null,
    // 프로필 이미지 URL (선택)
) {
    fun toEntity(existingMember: Member): Member =
        existingMember.apply {
            this.name = this@MemberProfileDtoRequest.name
            this.email = this@MemberProfileDtoRequest.email
            this.profileImage = this@MemberProfileDtoRequest.profileImage
        }
}
