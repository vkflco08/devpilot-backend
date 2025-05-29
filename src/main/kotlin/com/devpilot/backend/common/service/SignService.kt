package com.devpilot.backend.common.service

import com.devpilot.backend.common.authority.JwtTokenProvider
import com.devpilot.backend.common.authority.TokenInfo
import com.devpilot.backend.common.dto.CustomSecurityUserDetails
import com.devpilot.backend.common.dto.TokenDtoRequest
import com.devpilot.backend.common.entity.MemberRefreshToken
import com.devpilot.backend.common.exception.InvalidInputException
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.common.repository.MemberRefreshTokenRepository
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.repository.MemberRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class SignService(
    private val memberRefreshTokenRepository: MemberRefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberRepository: MemberRepository,
) {
    @Transactional
    fun saveRefreshToken(member: Member, refreshToken: String) {
        val managedMember = member.id?.let {
            memberRepository.findById(it)
                .orElseThrow { IllegalArgumentException("Invalid member") }
        } ?: throw UserNotFoundException()

        // 기존 토큰 조회
        val existingToken = memberRefreshTokenRepository.findByMember(managedMember)

        if (existingToken != null) {
            // 토큰 값만 수정
            existingToken.refreshToken = refreshToken
        } else {
            // 새 토큰 생성
            val newToken = MemberRefreshToken(
                member = managedMember,
                refreshToken = refreshToken
            )
            memberRefreshTokenRepository.save(newToken)
        }
    }

    fun newAccessToken(request: TokenDtoRequest, response: HttpServletResponse): TokenInfo {
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw InvalidInputException("로그인이 만료되었습니다.")
        }

        val authentication = jwtTokenProvider.getAuthentication(request.refreshToken)
        val userId = (authentication.principal as CustomSecurityUserDetails).userId
            ?: throw InvalidInputException("유저의 아이디가 null일 수 없습니다.")

        val storedTokenOpt = memberRefreshTokenRepository.findById(userId)
        val storedRefreshToken = storedTokenOpt.orElseThrow {
            InvalidInputException("유효하지 않은 사용자입니다.")
        }.refreshToken

        if (request.refreshToken != storedRefreshToken) {
            throw InvalidInputException("다시 로그인해주세요")
        }

        val newAccessToken = jwtTokenProvider.createAccessToken(authentication)

        val cookie = Cookie("refreshToken", storedRefreshToken).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 60 * 60 * 24 * 7
        }
        response.addCookie(cookie)

        return TokenInfo("Bearer", newAccessToken)
    }
}
