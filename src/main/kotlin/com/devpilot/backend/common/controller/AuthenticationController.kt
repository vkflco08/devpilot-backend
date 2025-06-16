package com.devpilot.backend.common.controller

import com.devpilot.backend.common.authority.TokenInfo
import com.devpilot.backend.common.dto.BaseResponse
import com.devpilot.backend.common.dto.CustomSecurityUserDetails
import com.devpilot.backend.common.dto.TokenDtoRequest
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.common.service.SignService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.beans.factory.annotation.Value
import java.util.*
import java.util.stream.Collectors

@RestController
@RequestMapping("/api/auth")
class AuthenticationController(
    private val signService: SignService,
    @Value("\${spring.security.oauth2.client.registration.google.redirect-uri}")
    private val googleRedirectUri: String
) {


    @PostMapping("/refresh")
    fun refreshAccessToken(
        @RequestBody tokenRequestDto: TokenDtoRequest,
        response: HttpServletResponse,
    ): BaseResponse<TokenInfo> {
        val newAccessToken = signService.newAccessToken(tokenRequestDto, response)
        return BaseResponse.success(data = newAccessToken)
    }

    @GetMapping("/roles")
    fun getUserRoles(authentication: Authentication): Set<String> =
        authentication.authorities
            .stream()
            .map { obj: GrantedAuthority -> obj.authority }
            .collect(Collectors.toSet())

    @GetMapping("/validate")
    fun validateToken(
        @RequestHeader("Authorization") authorization: String
    ): BaseResponse<Boolean> {
        val token = authorization.removePrefix("Bearer ").trim()
        val isExpired = signService.isAccessTokenExpired(token)
        return BaseResponse.success(data = !isExpired, message = if (!isExpired) "유효한 토큰입니다." else "만료된 토큰입니다.")
    }

    @GetMapping("/bind/google")
    fun bindSocialAccount(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): BaseResponse<String> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomSecurityUserDetails).userId
            ?: throw UserNotFoundException()
        println("🔗 계정 연동 요청: userId = $userId, provider = google")

        // state를 위한 랜덤 값 생성
        val stateToken = "bind:" + UUID.randomUUID().toString()

        // 세션에 userId <-> state 매핑 저장 (예: request.session.setAttribute("bind:<UUID>", userId))
        request.session.setAttribute(stateToken, userId)
        println("🧩 저장된 state = $stateToken → userId = $userId")

        val redirectUri = UriComponentsBuilder
            .fromUriString(googleRedirectUri)
            .queryParam("state", stateToken)
            .build().toUriString()

        return BaseResponse.success(data = redirectUri)
    }
}
