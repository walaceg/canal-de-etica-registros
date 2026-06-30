package com.baseplus.modules.registros.security;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.modules.registros.domain.ApiClient;
import com.baseplus.modules.registros.repository.ApiClientRepository;

@Service
public class ApiKeyAuthenticationService {

    private final ApiClientRepository apiClientRepository;

    public ApiKeyAuthenticationService(ApiClientRepository apiClientRepository) {
        this.apiClientRepository = apiClientRepository;
    }

    @Transactional
    public ApiClient authenticate(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiKeyAuthenticationException();
        }

        ApiClient apiClient = apiClientRepository.findByApiKeyHash(ApiKeyHash.sha256(apiKey.trim()))
                .filter(client -> Boolean.TRUE.equals(client.getAtivo()))
                .orElseThrow(ApiKeyAuthenticationException::new);

        apiClient.setUltimoUsoEm(LocalDateTime.now());
        return apiClient;
    }
}
