package com.example.Krieger.config;

import com.example.Krieger.config.services.AuthenticationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final ObjectProvider<AuthenticationService> authenticationServiceProvider;
    private final ObjectProvider<AuthenticationEntryPoint> entryPointProvider; // <-- use interface

    public WebSecurityConfig(ObjectProvider<AuthenticationService> authenticationServiceProvider,
                             ObjectProvider<AuthenticationEntryPoint> entryPointProvider) {
        this.authenticationServiceProvider = authenticationServiceProvider;
        this.entryPointProvider = entryPointProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("X-Total-Count", "Link"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource) throws Exception {

        // Fallback to 401 if no custom AuthenticationEntryPoint bean is present (e.g., tests)
        AuthenticationEntryPoint authEntryPoint = entryPointProvider.getIfAvailable(
                () -> new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
        );

        http
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/authenticate", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint));

        // Add JWT filter only if AuthenticationService is provided (common case outside tests)
        AuthenticationService authService = authenticationServiceProvider.getIfAvailable();
        if (authService != null && authService.jwtRequestFilter() != null) {
            http.addFilterBefore(authService.jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }
}
