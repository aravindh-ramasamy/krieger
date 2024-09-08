package com.example.Krieger.config.jwt;

import com.example.Krieger.config.services.AuthenticationService;
import com.example.Krieger.exception.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    @Autowired
    public JwtRequestFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // This method filters requests and process JWT authentication.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            authenticationService.processJwtAuthentication(request); // Authenticate request based on JWT
        } catch (InvalidTokenException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{ \"error\": \"Invalid or missing token\" }");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
