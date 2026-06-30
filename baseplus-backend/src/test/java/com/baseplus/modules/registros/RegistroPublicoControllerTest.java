package com.baseplus.modules.registros;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.baseplus.modules.registros.domain.ApiClient;
import com.baseplus.modules.registros.domain.Registro;
import com.baseplus.modules.registros.domain.TipoFato;
import com.baseplus.modules.registros.repository.ApiClientRepository;
import com.baseplus.modules.registros.repository.RegistroAnexoRepository;
import com.baseplus.modules.registros.repository.RegistroRepository;
import com.baseplus.modules.registros.repository.TipoFatoRepository;
import com.baseplus.modules.registros.security.ApiKeyHash;

@SpringBootTest
@AutoConfigureMockMvc
class RegistroPublicoControllerTest {

    private static final String ENDPOINT = "/api/public/v1/registros";
    private static final String HEADER_NAME = "X-API-Key";
    private static final String VALID_API_KEY = "teste-chave-publica-registros";

    @TempDir
    static Path uploadDirectory;

    @DynamicPropertySource
    static void configureUploadDirectory(DynamicPropertyRegistry registry) {
        registry.add("baseplus.upload.directory", uploadDirectory::toString);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiClientRepository apiClientRepository;

    @Autowired
    private TipoFatoRepository tipoFatoRepository;

    @Autowired
    private RegistroRepository registroRepository;

    @Autowired
    private RegistroAnexoRepository registroAnexoRepository;

    @BeforeEach
    void setUp() {
        registroAnexoRepository.deleteAll();
        registroRepository.deleteAll();
        apiClientRepository.deleteAll();
        tipoFatoRepository.deleteAll();
    }

    @Test
    void shouldCreateRegistroWithSuccess() throws Exception {
        saveApiClient(VALID_API_KEY, true);
        TipoFato tipoFato = saveTipoFato("Conduta", true);

        mockMvc.perform(validRequest("CE-2026-000001", tipoFato.getId())
                        .file(png("anexos[]", "evidencia.png"))
                        .header(HEADER_NAME, VALID_API_KEY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(notNullValue()))
                .andExpect(jsonPath("$.data.protocolo").value("CE-2026-000001"))
                .andExpect(jsonPath("$.data.criadoEm").value(notNullValue()))
                .andExpect(jsonPath("$.data.quantidadeAnexos").value(1))
                .andExpect(jsonPath("$.message").value("Registro recebido com sucesso."))
                .andExpect(jsonPath("$.errors").value(empty()));

        Registro registro = registroRepository.findByProtocolo("CE-2026-000001").orElseThrow();
        assertThat(registro.getTipoFatoNome()).isEqualTo(tipoFato.getNome());
        assertThat(registroAnexoRepository.findByRegistro_IdOrderByCriadoEmAsc(registro.getId())).hasSize(1);
    }

    @Test
    void shouldRejectDuplicatedProtocol() throws Exception {
        saveApiClient(VALID_API_KEY, true);
        TipoFato tipoFato = saveTipoFato("Conduta", true);
        registroRepository.save(new Registro(
                "CE-2026-000002",
                null,
                null,
                null,
                "Relato ja existente",
                tipoFato,
                tipoFato.getNome()
        ));

        mockMvc.perform(validRequest("CE-2026-000002", tipoFato.getId())
                        .header(HEADER_NAME, VALID_API_KEY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Protocolo duplicado."));
    }

    @Test
    void shouldRejectUnknownTipoFato() throws Exception {
        saveApiClient(VALID_API_KEY, true);

        mockMvc.perform(validRequest("CE-2026-000003", 999999L)
                        .header(HEADER_NAME, VALID_API_KEY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tipo de fato nao encontrado."));
    }

    @Test
    void shouldRejectInactiveTipoFato() throws Exception {
        saveApiClient(VALID_API_KEY, true);
        TipoFato tipoFato = saveTipoFato("Conduta inativa", false);

        mockMvc.perform(validRequest("CE-2026-000004", tipoFato.getId())
                        .header(HEADER_NAME, VALID_API_KEY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tipo de fato inativo."));
    }

    @Test
    void shouldRejectRequestWithoutApiKey() throws Exception {
        TipoFato tipoFato = saveTipoFato("Conduta", true);

        mockMvc.perform(validRequest("CE-2026-000005", tipoFato.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldRejectRequestWithInvalidApiKey() throws Exception {
        saveApiClient(VALID_API_KEY, true);
        TipoFato tipoFato = saveTipoFato("Conduta", true);

        mockMvc.perform(validRequest("CE-2026-000006", tipoFato.getId())
                        .header(HEADER_NAME, "chave-invalida"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldCreateRegistroWithoutAttachments() throws Exception {
        saveApiClient(VALID_API_KEY, true);
        TipoFato tipoFato = saveTipoFato("Assedio moral", true);

        mockMvc.perform(validRequest("CE-2026-000007", tipoFato.getId())
                        .header(HEADER_NAME, VALID_API_KEY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantidadeAnexos").value(0));

        Registro registro = registroRepository.findByProtocolo("CE-2026-000007").orElseThrow();
        assertThat(registroAnexoRepository.findByRegistro_IdOrderByCriadoEmAsc(registro.getId())).isEmpty();
    }

    @Test
    void shouldCreateRegistroWithMultipleAttachments() throws Exception {
        saveApiClient(VALID_API_KEY, true);
        TipoFato tipoFato = saveTipoFato("Corrupcao", true);

        mockMvc.perform(validRequest("CE-2026-000008", tipoFato.getId())
                        .file(png("anexos[]", "evidencia-1.png"))
                        .file(jpeg("anexos[]", "evidencia-2.jpg"))
                        .header(HEADER_NAME, VALID_API_KEY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantidadeAnexos").value(2));

        Registro registro = registroRepository.findByProtocolo("CE-2026-000008").orElseThrow();
        var anexos = registroAnexoRepository.findByRegistro_IdOrderByCriadoEmAsc(registro.getId());
        assertThat(anexos).hasSize(2);
        assertThat(anexos).allSatisfy(anexo -> {
            assertThat(anexo.getCaminho()).startsWith("/uploads/registros/anexos/");
            assertThat(anexo.getHash()).hasSize(64);
            Path storedPath = uploadDirectory.resolve(anexo.getCaminho().substring("/uploads/".length()));
            assertThat(Files.exists(storedPath)).isTrue();
        });
    }

    @Test
    void shouldRejectInvalidUpload() throws Exception {
        saveApiClient(VALID_API_KEY, true);
        TipoFato tipoFato = saveTipoFato("Conduta", true);
        long storedFilesBeforeRequest = countStoredRegistroFiles();
        MockMultipartFile invalidFile = new MockMultipartFile(
                "anexos[]",
                "evidencia.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "conteudo".getBytes()
        );

        mockMvc.perform(validRequest("CE-2026-000009", tipoFato.getId())
                        .file(png("anexos[]", "evidencia-valida.png"))
                        .file(invalidFile)
                        .header(HEADER_NAME, VALID_API_KEY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Arquivo invalido."));

        assertThat(registroRepository.findByProtocolo("CE-2026-000009")).isEmpty();
        assertThat(countStoredRegistroFiles()).isEqualTo(storedFilesBeforeRequest);
    }

    private org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder validRequest(String protocolo, Long tipoFatoId) {
        org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder builder = multipart(ENDPOINT);
        builder.param("protocolo", protocolo);
        builder.param("tipoFatoId", tipoFatoId.toString());
        builder.param("relato", "Relato inicial recebido pela API publica.");
        builder.param("nome", "Pessoa Relatora");
        builder.param("email", "relatora@example.com");
        builder.param("telefone", "+55 11 99999-9999");
        return builder;
    }

    private ApiClient saveApiClient(String apiKey, boolean ativo) {
        return apiClientRepository.saveAndFlush(new ApiClient(
                "Cliente publico " + UUID.randomUUID(),
                ApiKeyHash.sha256(apiKey),
                ativo
        ));
    }

    private TipoFato saveTipoFato(String nome, boolean ativo) {
        return tipoFatoRepository.saveAndFlush(new TipoFato(nome + " " + UUID.randomUUID(), ativo, 1));
    }

    private MockMultipartFile png(String name, String filename) {
        return new MockMultipartFile(
                name,
                filename,
                MediaType.IMAGE_PNG_VALUE,
                new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        );
    }

    private MockMultipartFile jpeg(String name, String filename) {
        return new MockMultipartFile(
                name,
                filename,
                MediaType.IMAGE_JPEG_VALUE,
                new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0}
        );
    }

    private long countStoredRegistroFiles() throws Exception {
        Path registrosDirectory = uploadDirectory.resolve("registros");
        if (!Files.exists(registrosDirectory)) {
            return 0L;
        }

        try (Stream<Path> paths = Files.walk(registrosDirectory)) {
            return paths.filter(Files::isRegularFile).count();
        }
    }
}
