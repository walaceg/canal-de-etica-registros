package com.baseplus.modules.registros.security;

import org.springframework.security.core.AuthenticationException;

public class ApiKeyAuthenticationException extends AuthenticationException {

    public ApiKeyAuthenticationException() {
        super("API key invalida.");
    }
}
