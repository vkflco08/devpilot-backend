package com.devpilot.backend.common.authority

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.*
import java.io.*
import jakarta.servlet.http.Cookie

class HttpCookieOAuth2AuthorizationRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    companion object {
        private const val COOKIE_NAME = "OAUTH2_AUTH_REQUEST"
        private const val COOKIE_EXPIRE_SECONDS = 180
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val cookie = request.cookies?.find { it.name == COOKIE_NAME }
        if (cookie == null) {
            println("🔍 [load] 쿠키 없음 - 인증 요청을 찾을 수 없음")
            return null
        }

        val authRequest = deserialize(cookie.value)
        println("✅ [load] 쿠키에서 OAuth2AuthorizationRequest 복원 성공 - state=${authRequest?.state}")
        return authRequest
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (authorizationRequest == null) {
            println("🗑️ [save] null authorizationRequest - 쿠키 삭제 요청")
            removeAuthorizationRequest(request, response)
            return
        }

        val serialized = serialize(authorizationRequest)
        val cookie = Cookie(COOKIE_NAME, serialized).apply {
            path = "/"
            isHttpOnly = true
            maxAge = COOKIE_EXPIRE_SECONDS
        }

        println("💾 [save] OAuth2AuthorizationRequest 쿠키에 저장 - state=${authorizationRequest.state}")
        response.addCookie(cookie)
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest? {
        val authRequest = loadAuthorizationRequest(request)
        val cookie = Cookie(COOKIE_NAME, "").apply {
            path = "/"
            maxAge = 0
        }
        println("❌ [remove] OAuth2AuthorizationRequest 쿠키 제거")
        response.addCookie(cookie)
        return authRequest
    }

    private fun serialize(obj: OAuth2AuthorizationRequest): String {
        return try {
            val byteStream = ByteArrayOutputStream()
            ObjectOutputStream(byteStream).use { it.writeObject(obj) }
            Base64.getUrlEncoder().encodeToString(byteStream.toByteArray()).also {
                println("🔐 [serialize] OAuth2AuthorizationRequest 직렬화 완료")
            }
        } catch (e: Exception) {
            println("⚠️ [serialize] 직렬화 실패: ${e.message}")
            throw e
        }
    }

    private fun deserialize(value: String): OAuth2AuthorizationRequest? {
        return try {
            val bytes = Base64.getUrlDecoder().decode(value)
            val inputStream = ByteArrayInputStream(bytes)
            ObjectInputStream(inputStream).use { it.readObject() as OAuth2AuthorizationRequest }.also {
                println("🔓 [deserialize] OAuth2AuthorizationRequest 복원 완료")
            }
        } catch (e: Exception) {
            println("❗ [deserialize] 복원 실패: ${e.message}")
            null
        }
    }
}