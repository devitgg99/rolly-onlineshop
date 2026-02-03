package com.example.rolly_shop_api.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val authenticationProvider: AuthenticationProvider,
    private val customAuthEntryPoint: CustomAuthEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
) {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFitler: JwtAuthenticationFitler
    ): DefaultSecurityFilterChain = http
        .csrf { it.disable() }
        .formLogin { it.disable() }
        .cors { it.configurationSource(corsConfigurationSource()) }
        .authorizeHttpRequests {
            it.requestMatchers(
                // Auth & Swagger
                "/api/v1/auth/**", "/api/v1/refresh", "/error",
                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**",
                // Public file/image endpoints
                "/api/v1/file/**", "/api/v1/files/**", "/api/v1/images/**",
                // Public catalog endpoints (GET only)
                "/api/v1/products/**", "/api/v1/brands/**", "/api/v1/categories/**",
                "/api/v1/reviews/product/**"
            ).permitAll()
                // Admin endpoints handled by @PreAuthorize
                .anyRequest().fullyAuthenticated()
        }
        .exceptionHandling {
            it.authenticationEntryPoint(customAuthEntryPoint)      // 401 - No token
            it.accessDeniedHandler(customAccessDeniedHandler)      // 403 - Wrong role
        }
        .sessionManagement {
            it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthenticationFitler, UsernamePasswordAuthenticationFilter::class.java)
        .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            // Allow all origins for maximum compatibility
            allowedOrigins = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
            allowedHeaders = listOf("*")
            exposedHeaders = listOf("Authorization", "Content-Type", "X-Requested-With")
            // Note: allowCredentials must be false when using allowedOrigins = "*"
            allowCredentials = false
            maxAge = 3600L
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}