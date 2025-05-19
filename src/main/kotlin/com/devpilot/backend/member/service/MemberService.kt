package com.devpilot.backend.member.service

import com.devpilot.backend.common.authority.JwtTokenProvider
import com.devpilot.backend.common.authority.TokenInfo
import com.devpilot.backend.common.exception.InvalidInputException
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.common.repository.MemberRefreshTokenRepository
import com.devpilot.backend.common.service.SignService
import com.devpilot.backend.member.dto.LoginDto
import com.devpilot.backend.member.dto.MemberDtoRequest
import com.devpilot.backend.member.dto.MemberDtoResponse
import com.devpilot.backend.member.dto.MemberProfileDtoRequest
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.entity.MemberRole
import com.devpilot.backend.member.repository.MemberRepository
import com.devpilot.backend.member.repository.MemberRoleRepository
import com.memo.memo.common.status.ROLE
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

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
            throw InvalidInputException("loginId", "이미 등록된 ID 입니다.")
        }
        member = memberDtoRequest.toEntity()
        memberRepository.save(member)

        val memberRole = MemberRole(null, ROLE.MEMBER, member)
        memberRoleRepository.save(memberRole)

        return "회원가입이 완료되었습니다."
    }

    /**
     * 로그인 -> 토큰 발행
     */
    @Transactional
    fun login(loginDto: LoginDto): TokenInfo {
        val authenticationToken = UsernamePasswordAuthenticationToken(loginDto.loginId, loginDto.password)
        val authentication = authenticationManagerBuilder.`object`.authenticate(authenticationToken)

        // 권한 출력
        println("User Authorities: ${authentication.authorities}")

        val accessToken = jwtTokenProvider.createAccessToken(authentication)
        val refreshToken = jwtTokenProvider.createRefreshToken(authentication)

        val member = memberRepository.findByLoginId(loginDto.loginId)

        // Refresh Token 저장
        if (member != null) {
            signService.saveRefreshToken(member, refreshToken)
            return TokenInfo("Bearer", accessToken, refreshToken)
        } else {
            throw UserNotFoundException()
        }
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
    ): MemberDtoResponse {
        val existingMember: Member =
            memberRepository.findByIdOrNull(memberProfileDtoRequest.id)
                ?: throw UserNotFoundException()

        memberProfileDtoRequest.toEntity(existingMember)

        // 변경 감지로 업데이트 수행
        return existingMember.toDto()
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
