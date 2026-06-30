package com.baseplus.modules.registros.security;

import java.util.List;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.baseplus.modules.registros.domain.ApiClient;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String principal;

    private ApiKeyAuthenticationToken(ApiClient apiClient) {
        super(List.of(new SimpleGrantedAuthority("API_PUBLICA_REGISTROS")));
        this.principal = apiClient.getId().toString();
        setAuthenticated(true);
    }

    public static ApiKeyAuthenticationToken authenticated(ApiClient apiClient) {
        return new ApiKeyAuthenticationToken(apiClient);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
