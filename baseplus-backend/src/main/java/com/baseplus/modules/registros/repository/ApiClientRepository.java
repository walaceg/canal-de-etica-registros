package com.baseplus.modules.registros.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baseplus.modules.registros.domain.ApiClient;

public interface ApiClientRepository extends JpaRepository<ApiClient, UUID> {

    Optional<ApiClient> findByApiKeyHash(String apiKeyHash);

    boolean existsByNomeIgnoreCase(String nome);
}
