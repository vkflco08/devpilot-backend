package com.devpilot.backend.common.service

import com.devpilot.backend.common.authority.JwtTokenProvider
import com.devpilot.backend.common.authority.TokenInfo
import com.devpilot.backend.common.dto.CustomUser
import com.devpilot.backend.common.dto.TokenDtoRequest
import com.devpilot.backend.common.entity.MemberRefreshToken
import com.devpilot.backend.common.exception.InvalidInputException
import com.devpilot.backend.common.repository.MemberRefreshTokenRepository
import com.devpilot.backend.member.entity.Member
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class SignService(
    private val memberRefreshTokenRepository: MemberRefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    @Transactional
    fun saveRefreshToken(
        member: Member,
        refreshToken: String,
    ) {
        // 멤버의 기존 리프레시 토큰 조회
        val existingToken = memberRefreshTokenRepository.findByMember(member)

        if (existingToken != null) {
            // 기존 토큰이 존재하면 업데이트
            existingToken.refreshToken = refreshToken
            memberRefreshTokenRepository.save(existingToken) // 업데이트된 토큰 저장
        } else {
            // 기존 토큰이 없으면 새로 저장
            memberRefreshTokenRepository.save(MemberRefreshToken(member, refreshToken))
        }
    }

    fun newAccessToken(tokenDtoRequest: TokenDtoRequest): TokenInfo {
        // Refresh token 검증 및 사용자 정보 추출
        if (!jwtTokenProvider.validateToken(tokenDtoRequest.refreshToken)) {
            throw InvalidInputException("로그인이 만료되었습니다.")
        }

        // 사용자 정보 추출
        val authentication = jwtTokenProvider.getAuthentication(tokenDtoRequest.refreshToken)
        val userId: Long =
            (authentication.principal as CustomUser).userId
                ?: throw InvalidInputException("유저의 아이디가 null일 수 없습니다.")

        // DB에서 저장된 Refresh Token을 확인
        val storedRefreshToken = memberRefreshTokenRepository.findById(userId).get().refreshToken

        // 전달받은 refresh token이 저장된 refresh token과 일치하는지 확인
        if (tokenDtoRequest.refreshToken == storedRefreshToken) {
            // 새로운 Access token 생성
            val newAccessToken = jwtTokenProvider.createAccessToken(authentication)

            return TokenInfo("Bearer", newAccessToken, storedRefreshToken)
        } else {
            throw InvalidInputException("다시 로그인해주세요")
        }
    }
}
