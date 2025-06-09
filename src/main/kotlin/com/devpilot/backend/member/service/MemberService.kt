package com.devpilot.backend.member.service

import com.devpilot.backend.common.authority.JwtTokenProvider
import com.devpilot.backend.common.authority.TokenInfo
import com.devpilot.backend.common.exception.exceptions.DuplicateLoginIdException
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.common.repository.MemberRefreshTokenRepository
import com.devpilot.backend.common.service.SignService
import com.devpilot.backend.member.dto.LoginDto
import com.devpilot.backend.member.dto.MemberDtoRequest
import com.devpilot.backend.member.dto.MemberDtoResponse
import com.devpilot.backend.member.dto.MemberProfileDtoRequest
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.entity.MemberAuthProvider
import com.devpilot.backend.member.entity.MemberRole
import com.devpilot.backend.member.enum.AuthProvider
import com.devpilot.backend.member.repository.MemberRepository
import com.devpilot.backend.member.repository.MemberRoleRepository
import com.memo.memo.common.status.ROLE
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.stereotype.Service

@Transactional
@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val memberRoleRepository: MemberRoleRepository,
    private val authenticationManagerBuilder: AuthenticationManagerBuilder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val signService: SignService,
    private val memberRefreshTokenRepository: MemberRefreshTokenRepository,
) {
    /**
     * 회원가입
     */
    @Transactional
    fun signUp(memberDtoRequest: MemberDtoRequest): String {
        var member = memberRepository.findByLoginId(memberDtoRequest.loginId)
        if (member != null) {
            throw DuplicateLoginIdException()
        }
        member = memberDtoRequest.toEntity()

        val newProvider = MemberAuthProvider(
            member = member,
            provider = AuthProvider.LOCAL,
            providerId = null
        )

        member.addAuthProviderIfNotExists(newProvider)
        memberRepository.save(member)

        val memberRole = MemberRole(null, ROLE.MEMBER, member)
        memberRoleRepository.save(memberRole)

        return "회원가입이 완료되었습니다."
    }

    /**
     * 로그인 -> 토큰 발행
     */
    @Transactional
    fun login(
        loginDto: LoginDto,
        response: HttpServletResponse,
        ): TokenInfo {
        val authenticationToken = UsernamePasswordAuthenticationToken(loginDto.loginId, loginDto.password)
        val authentication = authenticationManagerBuilder.`object`.authenticate(authenticationToken)

        // 권한 출력
        println("User Authorities: ${authentication.authorities}")

        val accessToken = jwtTokenProvider.createAccessToken(authentication)
        val refreshToken = jwtTokenProvider.createRefreshToken(authentication)

        val member = memberRepository.findByLoginId(loginDto.loginId)
            ?: throw UserNotFoundException()
        signService.saveRefreshToken(member, refreshToken)

        val cookie = Cookie("task-manager-refreshToken", refreshToken).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 60 * 60 * 24 * 7 // 7일
        }
        response.addCookie(cookie)

        return TokenInfo("Bearer", accessToken)
    }

    /**
     * 내정보 조회
     */
    fun searchMyInfo(userId: Long?): MemberDtoResponse {
        val member: Member =
            memberRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException()

        return member.toDto()
    }

    /**
     * 내 정보 수정
     */
    @Transactional
    fun saveMyInfo(
        memberProfileDtoRequest: MemberProfileDtoRequest,
        userId: Long?
    ): MemberDtoResponse {
        val existingMember: Member =
            memberRepository.findByIdOrNull(userId)
                ?: throw UserNotFoundException()

        val updatedMember = memberProfileDtoRequest.toEntity(existingMember)
        val savedMember = memberRepository.save(updatedMember)

        return MemberDtoResponse(
            id = savedMember.id!!,
            loginId = savedMember.loginId.toString(),
            name = savedMember.name,
            email = savedMember.email,
            role = savedMember.role,
            createdDate = savedMember.createdDate,
            description = savedMember.description,
            department = savedMember.department,
            phoneNumber = savedMember.phoneNumber,
            providers = savedMember.authProviders.map { it.provider }
        )
    }

    /**
     * 로그아웃
     */
    @Transactional
    fun deleteRefToken(loginId: Long): String {
        memberRefreshTokenRepository.deleteById(loginId)
        return "로그아웃 되었습니다."
    }
}
