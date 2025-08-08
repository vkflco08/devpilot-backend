@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.devpilot.backend.common.authority

import com.devpilot.backend.common.dto.CustomSecurityUserDetails
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.Date

const val ACCESS_EXPIRATION_MILLISECONDS: Long = 1000 * 60 * 60 // 1시간
const val REFRESH_EXPIRATION_MILLISECONDS: Long = 1000 * 60 * 60 * 24 * 7 // 7일

@Component
class JwtTokenProvider {
    @Value("\${jwt.secret}")
    lateinit var secretKey: String

    private val logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)
    private val key by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)) }

    /**
     * Access Token 생성
     */
    fun createAccessToken(authentication: Authentication): String {
        val authorities: String =
            authentication
                .authorities
                .joinToString(",", transform = GrantedAuthority::getAuthority)

        val now = Date()
        val accessExpiration = Date(now.time + ACCESS_EXPIRATION_MILLISECONDS)

        // Access Token
        return Jwts
            .builder()
            .setSubject(authentication.name)
            .claim("auth", authorities)
            .claim("userId", (authentication.principal as CustomSecurityUserDetails).userId)
            .setIssuedAt(now)
            .setExpiration(accessExpiration)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * Refresh token 생성
     */
    fun createRefreshToken(authentication: Authentication): String {
        val authorities: String =
            authentication
                .authorities
                .joinToString(",", transform = GrantedAuthority::getAuthority)
        val now = Date()
        val refreshExpiration = Date(now.time + REFRESH_EXPIRATION_MILLISECONDS)

        return Jwts
            .builder()
            .setSubject(authentication.name)
            .claim("auth", authorities)
            .claim("userId", (authentication.principal as CustomSecurityUserDetails).userId)
            .setIssuedAt(now)
            .setExpiration(refreshExpiration)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * Token 정보 추출
     */
    fun getAuthentication(token: String): Authentication {
        val claims: Claims = getClaims(token)

        // claims에 있는 auth를 가져온다
        val auth = claims["auth"] ?: throw RuntimeException("잘못된 토큰입니다.")
        val userId = claims["userId"] ?: throw RuntimeException("잘못된 토큰입니다.")

        // 권한 정보 추출
        val authorities: Collection<GrantedAuthority> =
            (auth as String)
                .split(",")
                .map { SimpleGrantedAuthority(it) }

        val principal: UserDetails = CustomSecurityUserDetails(userId.toString().toLong(), claims.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, "", authorities)
    }

    /**
     * Token 검증
     */
    fun validateToken(token: String): Boolean {
        try {
            getClaims(token)
            return true
        } catch (e: Exception) {
            when (e) {
                is SecurityException -> logger.error("Invalid JWT signature.")
                is MalformedJwtException -> logger.error("Invalid JWT token.")
                is ExpiredJwtException -> logger.error("Expired JWT token.")
                is UnsupportedJwtException -> logger.error("Unsupported JWT token.")
                is IllegalArgumentException -> logger.error("JWT claims string is empty.")
                else -> logger.error("JWT token validation failed.")
            }
            logger.error("Error validating JWT token: ${e.message}")
        }
        return false
    }

    fun getClaims(token: String): Claims =
        Jwts
            .parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

    /**
     * 토큰 만료 확인
     */
    fun isTokenExpired(token: String): Boolean {
        val claims = getClaims(token)
        return claims.expiration.before(Date())
    }

    /**
     * userId 가져오기
     */
    fun getUserIdFromToken(token: String): Long {
        val claims = Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token)
            .body

        return claims["userId"].toString().toLong()
    }
}
