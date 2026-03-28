package com.ispilo.config;

import com.ispilo.security.AppSecurityFilter;
import com.ispilo.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppSecurityFilter appSecurityFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Public endpoints that do not require authentication or app headers.
     * Keep this in sync with AppSecurityFilter.PUBLIC_ENDPOINTS.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/", "/health", "/error",
            "/app/version", "/api/app/version", "/api/v1/app/version", "/api/v2/app/version",
            "/app/public-key", "/api/app/public-key", "/api/v1/app/public-key", "/api/v2/app/public-key",
            "/registerApp", "/api/registerApp", "/api/v1/registerApp", "/api/v2/registerApp",
            "/api/auth/**", "/api/v1/auth/**",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
            "/ws/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Stateless API: disable CSRF, enable CORS
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        // Order matters: app-level checks first, then JWT auth, then UsernamePasswordAuthenticationFilter
        http.addFilterBefore(appSecurityFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}