package com.devpilot.backend.common.authority

import com.devpilot.backend.common.ratelimit.RateLimitFilter
import com.devpilot.backend.common.repository.CustomAuthorizationRequestRepository
import com.devpilot.backend.member.service.CustomOidcUserService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val rateLimitFilter: RateLimitFilter,
    @Value("\${cors.allowed.origins}") private val allowedOrigins: List<String>,
    private val customOidcUserService: CustomOidcUserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val oAuth2FailureHandler: OAuth2FailureHandler,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
    private val objectMapper: ObjectMapper
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .oauth2Login { oauth2 ->
                oauth2
                    .authorizationEndpoint { authEndpoint ->
                        authEndpoint.authorizationRequestRepository(authorizationRequestRepository())
                    }
                    .userInfoEndpoint { userInfo ->
                        userInfo.oidcUserService(customOidcUserService)
                    }
                    .successHandler(oAuth2SuccessHandler)
                    .failureHandler(oAuth2FailureHandler)
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        "/api/member/signup",
                        "/api/member/login",
                        "/api/auth/refresh",
                    ).anonymous()
                    .requestMatchers(
                        "/api/task/**",
                        "/api/project/**",
                        "/api/member/**",
                        "/api/auth/bind/google",
                        "/api/agent/**"
                    ).hasRole("MEMBER")
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/oauth2/**",
                        "/error"
                    ).permitAll()
            }.exceptionHandling {
                it.authenticationEntryPoint(customAuthenticationEntryPoint())
                it.accessDeniedHandler(customAccessDeniedHandler)
            }
            .addFilterBefore(
                rateLimitFilter,
                SecurityContextHolderAwareRequestFilter::class.java,
            ) // RateLimitFilter 추가
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java,
            ).cors { it.configurationSource(corsConfigurationSource()) }

        return http.build()
    }

    @Bean
    fun customAuthenticationEntryPoint(): AuthenticationEntryPoint = CustomAuthenticationEntryPoint(objectMapper)

    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = allowedOrigins // application.yml에서 읽어온 값을 설정
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun authorizationRequestRepository(): AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
        return CustomAuthorizationRequestRepository()
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }
}
