package com.baseplus.modules.registros.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.baseplus.modules.registros.domain.ApiClient;
import com.baseplus.shared.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-API-Key";
    private static final String PUBLIC_REGISTROS_PATH = "/api/public/v1/registros/";

    private final ApiKeyAuthenticationService apiKeyAuthenticationService;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthenticationFilter(ApiKeyAuthenticationService apiKeyAuthenticationService, ObjectMapper objectMapper) {
        this.apiKeyAuthenticationService = apiKeyAuthenticationService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        return !path.equals("/api/public/v1/registros") && !path.startsWith(PUBLIC_REGISTROS_PATH);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            ApiClient apiClient = apiKeyAuthenticationService.authenticate(request.getHeader(HEADER_NAME));
            ApiKeyAuthenticationToken authentication = ApiKeyAuthenticationToken.authenticated(apiClient);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (ApiKeyAuthenticationException exception) {
            SecurityContextHolder.clearContext();
            writeUnauthorizedResponse(response);
        }
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        ApiResponse<Void> body = ApiResponse.failure("Acesso nao autorizado.", List.of("API key obrigatoria ou invalida."));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
