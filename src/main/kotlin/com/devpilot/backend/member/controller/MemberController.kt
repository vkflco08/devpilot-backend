package com.devpilot.backend.member.controller

import com.devpilot.backend.common.authority.TokenInfo
import com.devpilot.backend.common.dto.BaseResponse
import com.devpilot.backend.common.dto.CustomUser
import com.devpilot.backend.common.exception.exceptions.UserNotFoundException
import com.devpilot.backend.member.dto.LoginDto
import com.devpilot.backend.member.dto.MemberDtoRequest
import com.devpilot.backend.member.dto.MemberDtoResponse
import com.devpilot.backend.member.dto.MemberProfileDtoRequest
import com.devpilot.backend.member.service.MemberService
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/member")
class MemberController(
    private val memberService: MemberService,
) {
    /**
     * 회원가입
     */
    @PostMapping("/signup")
    fun signUp(
        @RequestBody @Valid memberDtoRequest: MemberDtoRequest,
    ): BaseResponse<Unit> {
        val resultMsg: String = memberService.signUp(memberDtoRequest)
        return BaseResponse.success(message = resultMsg)
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    fun login(
        @RequestBody @Valid loginDto: LoginDto,
    ): BaseResponse<TokenInfo> {
        val tokenInfo = memberService.login(loginDto)
        return BaseResponse.success(data = tokenInfo)
    }

    /**
     * 내 정보 보기
     */
    @GetMapping("/info")
    fun searchMyInfo(): BaseResponse<MemberDtoResponse> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
                ?: throw UserNotFoundException()
        val response = memberService.searchMyInfo(userId)
        return BaseResponse.success(data = response)
    }

    /**
     * 내 정보 수정
     */
    @PutMapping("/info_edit")
    fun saveMyInfo(
        @RequestBody @Valid memberProfileDtoRequest: MemberProfileDtoRequest,
    ): BaseResponse<MemberDtoResponse> {
        // 사용자 정보 처리
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
                ?: throw UserNotFoundException()

        val resultMsg = memberService.saveMyInfo(memberProfileDtoRequest, userId)
        return BaseResponse.success(data = resultMsg)
    }

    /**
     * 로그아웃
     */
    @DeleteMapping("/logout")
    fun logout(): BaseResponse<Unit> {
        val userId =
            (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
                ?: throw UserNotFoundException()

        val resultMsg: String = memberService.deleteRefToken(userId)
        return BaseResponse.success(message = resultMsg)
    }
}
