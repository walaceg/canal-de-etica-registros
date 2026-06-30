package com.baseplus.modules.registros.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.baseplus.modules.registros.domain.ApiClient;
import com.baseplus.modules.registros.repository.ApiClientRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ApiKeyAuthenticationFilterTest {

    private static final String PUBLIC_REGISTROS_PATH = "/api/public/v1/registros/verificacao";
    private static final String HEADER_NAME = "X-API-Key";
    private static final String VALID_API_KEY = "teste-chave-api-valida";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiClientRepository apiClientRepository;

    @BeforeEach
    void setUp() {
        apiClientRepository.deleteAll();
    }

    @Test
    void shouldRejectRequestWithoutApiKey() throws Exception {
        mockMvc.perform(get(PUBLIC_REGISTROS_PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldRejectRequestWithInvalidApiKey() throws Exception {
        saveApiClient("Cliente valido", VALID_API_KEY, true);

        mockMvc.perform(get(PUBLIC_REGISTROS_PATH).header(HEADER_NAME, "chave-invalida"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors[0]").value("API key obrigatoria ou invalida."));
    }

    @Test
    void shouldRejectInactiveApiClient() throws Exception {
        saveApiClient("Cliente inativo", VALID_API_KEY, false);

        mockMvc.perform(get(PUBLIC_REGISTROS_PATH).header(HEADER_NAME, VALID_API_KEY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldAuthenticateRequestWithValidApiKey() throws Exception {
        saveApiClient("Cliente valido", VALID_API_KEY, true);

        mockMvc.perform(get(PUBLIC_REGISTROS_PATH).header(HEADER_NAME, VALID_API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateLastUsageWhenApiKeyIsValid() throws Exception {
        ApiClient apiClient = saveApiClient("Cliente valido", VALID_API_KEY, true);
        assertThat(apiClient.getUltimoUsoEm()).isNull();

        mockMvc.perform(get(PUBLIC_REGISTROS_PATH).header(HEADER_NAME, VALID_API_KEY))
                .andExpect(status().isNotFound());

        ApiClient updated = apiClientRepository.findById(apiClient.getId()).orElseThrow();
        assertThat(updated.getUltimoUsoEm()).isNotNull();
    }

    private ApiClient saveApiClient(String nome, String apiKey, boolean ativo) {
        return apiClientRepository.saveAndFlush(new ApiClient(
                nome + "-" + UUID.randomUUID(),
                ApiKeyHash.sha256(apiKey),
                ativo
        ));
    }
}
