package com.devpilot.backend.common.repository

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

class CustomAuthorizationRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    companion object {
        const val SESSION_ATTR_NAME = "SPRING_SECURITY_OAUTH2_AUTHORIZATION_REQUEST"
        // 연동 관련 state 정보를 저장할 새로운 세션 속성 이름
        const val SPRING_SECURITY_OAUTH2_BINDING_DATA = "SPRING_SECURITY_OAUTH2_BINDING_DATA"
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
            session.removeAttribute(SPRING_SECURITY_OAUTH2_BINDING_DATA)
            println("Session attributes after null request cleanup: ${session.attributeNames.toList().joinToString(", ")}")
            return
        }

        // Spring Security가 생성한 기본 state (CSRF 방어용)
        val originalSpringSecurityState = authorizationRequest.state
        val bindingUserIdStr = request.getParameter("binding_user_id")
        val bindingStateToken = request.getParameter("binding_state_token")

        println("DEBUG: Original SS State = $originalSpringSecurityState")
        println("DEBUG: binding_user_id Parameter = $bindingUserIdStr")
        println("DEBUG: binding_state_token Parameter = $bindingStateToken")

        // 1. Spring Security의 OAuth2AuthorizationRequest 객체는 원래 state 그대로 세션에 저장
        session.setAttribute(SESSION_ATTR_NAME, authorizationRequest)
        println("DEBUG: After setting ${SESSION_ATTR_NAME}, attributes: ${session.attributeNames.toList().joinToString(", ")}")

        // 2. 만약 myCustomBindState가 "bind:"로 시작한다면, 연동 요청 플래그를 세션에 저장
        //    이때, 키는 originalSpringSecurityState와 조합합니다.
        if (bindingUserIdStr != null && bindingStateToken != null && bindingStateToken.startsWith("bind:")) {
            val bindingDataKey = SPRING_SECURITY_OAUTH2_BINDING_DATA + "_" + originalSpringSecurityState

            val userId = bindingUserIdStr.toLongOrNull() // userId 문자열을 Long으로 변환

            val bindingMap = mutableMapOf<String, Any>()
            bindingMap["bindState"] = bindingStateToken // "bind:UUID" 값
            if (userId != null) {
                bindingMap["userId"] = userId // 현재 로그인된 사용자의 ID
            } else {
                println("WARN: userId not found in parameter 'binding_user_id' or could not be parsed to Long: $bindingUserIdStr")
            }

            session.setAttribute(bindingDataKey, bindingMap)
            println("📦 세션에 연동 데이터 저장 (key: $bindingDataKey, value: $bindingMap)")
            println("Session attributes after save (detailed): ${session.attributeNames.toList().joinToString(", ")}")
        } else {
            println("📦 커스텀 연동 파라미터가 없거나 유효하지 않습니다. (bindingUserIdStr: $bindingUserIdStr, bindingStateToken: $bindingStateToken)")
            println("Session attributes after save (no binding data stored): ${session.attributeNames.toList().joinToString(", ")}")
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

        println("DEBUG: removeAuthorizationRequest - Session attributes after removal: ${session.attributeNames.toList().joinToString(", ")}")

        return authorizationRequest
    }
}
