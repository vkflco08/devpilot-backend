package com.devpilot.backend.common.controller

import com.devpilot.backend.common.authority.TokenInfo
import com.devpilot.backend.common.dto.BaseResponse
import com.devpilot.backend.common.dto.TokenDtoRequest
import com.devpilot.backend.common.service.SignService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors

@RestController
@RequestMapping("/api/auth")
class AuthenticationController(
    private val signService: SignService,
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
}
