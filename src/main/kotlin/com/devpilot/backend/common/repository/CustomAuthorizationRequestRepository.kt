package com.devpilot.backend.common.repository

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

class CustomAuthorizationRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    companion object {
        const val SESSION_ATTR_NAME = "SPRING_SECURITY_OAUTH2_AUTHORIZATION_REQUEST"
        // 연동 관련 state 정보를 저장할 새로운 세션 속성 이름
        const val SPRING_SECURITY_OAUTH2_AUTHORIZATION_REQUEST_BINDING_STATE = "SPRING_SECURITY_OAUTH2_AUTHORIZATION_REQUEST_BINDING_STATE"
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        // OAuth2LoginAuthenticationFilter가 콜백 시 세션에서 OAuth2AuthorizationRequest를 로드할 때 사용됩니다.
        val session = request.getSession(false)
        println("DEBUG: loadAuthorizationRequest - Session ID: ${session?.id}")
        if (session != null) {
            println("DEBUG: loadAuthorizationRequest - Session attributes: ${session.attributeNames.toList().joinToString(", ")}")
        }
        return session?.getAttribute(SESSION_ATTR_NAME) as OAuth2AuthorizationRequest?
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        println("🔐 saveAuthorizationRequest 호출됨")

        val session = request.getSession()
        println("Session ID (save): ${session.id}") // 세션 ID 로그 추가

        if (authorizationRequest == null) {
            session.removeAttribute(SESSION_ATTR_NAME)
            // Note: BINDING_STATE는 CustomOidcUserService에서 제거하므로 여기서는 제거하지 않습니다.
            println("Session attributes after null request cleanup: ${session.attributeNames.toList().joinToString(", ")}")
            return
        }

        // Spring Security가 생성한 기본 state (CSRF 방어용)
        val originalSpringSecurityState = authorizationRequest.state
        // 클라이언트로부터 전달된 "bind:" 정보가 담긴 state 파라미터
        val customParamState = request.getParameter("state")

        println("DEBUG: Original SS State = $originalSpringSecurityState")
        println("DEBUG: Custom Param State = $customParamState")

        // 1. 스프링 시큐리티의 OAuth2AuthorizationRequest 객체는 원래 state 그대로 세션에 저장
        session.setAttribute(SESSION_ATTR_NAME, authorizationRequest)
        println("DEBUG: After setting ${SESSION_ATTR_NAME}, attributes: ${session.attributeNames.toList().joinToString(", ")}")

        // 2. 만약 customParamState가 "bind:"로 시작한다면, 이 정보를 별도 세션에 저장
        //    이때, 키는 originalSpringSecurityState를 사용합니다.
        if (customParamState != null && customParamState.startsWith("bind:")) {
            println("📦 클라이언트 전달 state 파라미터 (bind): $customParamState")
            println("✅ 세션에 연동 요청 플래그 설정")
            println("Session attributes after save (detailed): ${session.attributeNames.toList().joinToString(", ")}")
        } else {
            println("📦 클라이언트 전달 state 파라미터 (일반): $customParamState")
            println("Session attributes after save (no binding state): ${session.attributeNames.toList().joinToString(", ")}")
        }

        println("🚀 IdP로 리다이렉트될 때 사용될 state는 '${originalSpringSecurityState}'입니다. (이 값이 Google로 보내집니다)")
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest? {
        val session = request.getSession(false) ?: return null
        println("DEBUG: removeAuthorizationRequest - Session ID: ${session.id}")
        println("DEBUG: removeAuthorizationRequest - Session attributes before removal: ${session.attributeNames.toList().joinToString(", ")}")

        val authorizationRequest = session.getAttribute(SESSION_ATTR_NAME) as OAuth2AuthorizationRequest?

        // 세션에서 OAuth2AuthorizationRequest 제거 (Spring Security가 요구하는 부분)
        session.removeAttribute(SESSION_ATTR_NAME)

        // 연동 요청 플래그도 함께 제거 (선택 사항, CustomOidcUserService에서 제거하는 것이 더 명확)
        session.removeAttribute(SPRING_SECURITY_OAUTH2_AUTHORIZATION_REQUEST_BINDING_STATE)

        println("DEBUG: removeAuthorizationRequest - Session attributes after removal: ${session.attributeNames.toList().joinToString(", ")}")

        return authorizationRequest
    }
}
