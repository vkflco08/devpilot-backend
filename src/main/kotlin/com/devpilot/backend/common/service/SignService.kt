package com.devpilot.backend.common.service

import com.devpilot.backend.common.authority.JwtTokenProvider
import com.devpilot.backend.common.authority.REFRESH_EXPIRATION_MILLISECONDS
import com.devpilot.backend.common.authority.TokenInfo
import com.devpilot.backend.common.dto.CustomSecurityUserDetails
import com.devpilot.backend.common.dto.TokenDtoRequest
import com.devpilot.backend.common.entity.MemberRefreshToken
import com.devpilot.backend.common.exception.exceptions.TokenValidationException
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.common.repository.MemberRefreshTokenRepository
import com.devpilot.backend.member.entity.Member
import com.devpilot.backend.member.repository.MemberRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import kotlinx.serialization.MissingFieldException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.util.*

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

    fun newAccessToken(requestRefreshToken: String?, response: HttpServletResponse): TokenInfo {
        val refreshToken = requestRefreshToken
            if(refreshToken.isNullOrBlank()) {
                throw TokenValidationException(
                    resultCode = "REFRESH_TOKEN_VALIDATION",
                    message = "refreshToken은 null일 수 없습니다.",
                    httpStatus = HttpStatus.BAD_REQUEST
                )
            }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw TokenValidationException(
                resultCode = "REFRESH_TOKEN_INVALID",
                message = "유효하지 않거나 만료된 Refresh Token입니다.",
                httpStatus = HttpStatus.UNAUTHORIZED
            )
        }

        val authentication = jwtTokenProvider.getAuthentication(refreshToken)
        val userId = (authentication.principal as CustomSecurityUserDetails).userId
            ?: throw TokenValidationException(
                resultCode = "USER_ID_MISSING_IN_TOKEN",
                message = "토큰에서 사용자 ID를 찾을 수 없습니다.",
                httpStatus = HttpStatus.UNAUTHORIZED
            )

        // MemberRefreshToken 엔티티를 사용하는 경우
        val storedRefreshTokenObj = memberRefreshTokenRepository.findById(userId)
            .orElseThrow {
                TokenValidationException(
                    resultCode = "REFRESH_TOKEN_NOT_FOUND_IN_DB",
                    message = "데이터베이스에 해당 Refresh Token 정보가 없습니다. 다시 로그인해주세요.",
                    httpStatus = HttpStatus.UNAUTHORIZED
                )
            }
        val storedRefreshToken = storedRefreshTokenObj.refreshToken

        if (refreshToken != storedRefreshToken) {
            throw TokenValidationException(
                resultCode = "REFRESH_TOKEN_MISMATCH",
                message = "유효하지 않은 Refresh Token입니다. 다시 로그인해주세요.",
                httpStatus = HttpStatus.UNAUTHORIZED
            )
        }

        val newAccessToken = jwtTokenProvider.createAccessToken(authentication)

        // ✨ Refresh Token 갱신 로직: 만료 임박 또는 재발급 정책에 따라
        val newRefreshToken: String
        val refreshTokenClaims = jwtTokenProvider.getClaims(refreshToken)
        val oneDayInMillis = 1000 * 60 * 60 * 24L // 1일

        // 기존 Refresh Token이 만료 임박(예: 1일 이내)했거나, 재사용 감지 후 무조건 새 토큰 발급 정책 등
        // `isTokenExpired`는 이미 validateToken에서 체크되므로, 여기서는 만료 임박만 체크해도 됩니다.
        if (refreshTokenClaims.expiration.time < Date().time + oneDayInMillis) {
            newRefreshToken = jwtTokenProvider.createRefreshToken(authentication)
            // DB에 새 Refresh Token 저장
            storedRefreshTokenObj.refreshToken = newRefreshToken
            // memberRepository.save(member) // 만약 member 엔티티에 직접 refreshToken을 가지고 있다면
            memberRefreshTokenRepository.save(storedRefreshTokenObj) // MemberRefreshToken 엔티티 업데이트
        } else {
            newRefreshToken = refreshToken // 기존 Refresh Token 재사용
        }

        val cookie = Cookie("task-pilot-refreshToken", newRefreshToken).apply { // ✨ `newRefreshToken` 사용
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = (REFRESH_EXPIRATION_MILLISECONDS / 1000).toInt()
        }
        response.addCookie(cookie)

        // ✨ TokenInfo DTO에 newRefreshToken 포함하여 반환
        return TokenInfo("Bearer", newAccessToken, newRefreshToken)
    }

    fun isAccessTokenExpired(token: String): Boolean {
        return jwtTokenProvider.isTokenExpired(token)
    }
}
