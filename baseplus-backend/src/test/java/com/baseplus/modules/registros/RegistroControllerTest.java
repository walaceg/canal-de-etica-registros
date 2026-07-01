package com.baseplus.modules.registros;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.modules.registros.domain.Registro;
import com.baseplus.modules.registros.domain.RegistroAnexo;
import com.baseplus.modules.registros.domain.TipoFato;
import com.baseplus.modules.registros.repository.RegistroAnexoRepository;
import com.baseplus.modules.registros.repository.RegistroRepository;
import com.baseplus.modules.registros.repository.TipoFatoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RegistroRepository registroRepository;

    @Autowired
    private RegistroAnexoRepository registroAnexoRepository;

    @Autowired
    private TipoFatoRepository tipoFatoRepository;

    private final List<Path> createdAnexoFiles = new ArrayList<>();

    @BeforeEach
    void setUp() {
        registroAnexoRepository.deleteAll();
        registroRepository.deleteAll();
    }

    @AfterEach
    void tearDown() throws IOException {
        for (Path file : createdAnexoFiles) {
            Files.deleteIfExists(file);
        }
        createdAnexoFiles.clear();
    }

    @Test
    void shouldListRegistrosForAuthenticatedUser() throws Exception {
        String token = loginAndGetToken();
        Registro registro = criarRegistro("CE-LISTAR-001", criarTipoFato("Conduta Listagem"));
        criarAnexo(registro, "evidencia-lista.pdf");

        mockMvc.perform(get("/api/registros")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].protocolo").value("CE-LISTAR-001"))
                .andExpect(jsonPath("$.data.content[0].fato").value("Conduta Listagem"))
                .andExpect(jsonPath("$.data.content[0].tipoFatoNome").value("Conduta Listagem"))
                .andExpect(jsonPath("$.data.content[0].status").value("RECEBIDO"))
                .andExpect(jsonPath("$.data.content[0].origem").value("API_PUBLICA"))
                .andExpect(jsonPath("$.data.content[0].nome").value("Pessoa Relatora"))
                .andExpect(jsonPath("$.data.content[0].email").value("relatora@example.com"))
                .andExpect(jsonPath("$.data.content[0].telefone").value("11999999999"))
                .andExpect(jsonPath("$.data.content[0].criadoEm").value(notNullValue()))
                .andExpect(jsonPath("$.data.content[0].quantidadeAnexos").value(1))
                .andExpect(jsonPath("$.data.content[0].relato").doesNotExist())
                .andExpect(jsonPath("$.message").value("Registros carregados."))
                .andExpect(jsonPath("$.errors").value(empty()));
    }

    @Test
    void shouldBlockRegistrosWithoutToken() throws Exception {
        mockMvc.perform(get("/api/registros"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldFilterRegistrosByProtocolo() throws Exception {
        String token = loginAndGetToken();
        TipoFato tipoFato = criarTipoFato("Conduta Protocolo");
        criarRegistro("CE-FILTRO-AAA", tipoFato);
        criarRegistro("CE-FILTRO-BBB", tipoFato);

        mockMvc.perform(get("/api/registros")
                        .header("Authorization", "Bearer " + token)
                        .param("protocolo", "AAA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].protocolo").value("CE-FILTRO-AAA"));
    }

    @Test
    void shouldFilterRegistrosByTipoFato() throws Exception {
        String token = loginAndGetToken();
        TipoFato tipoA = criarTipoFato("Conduta Tipo A");
        TipoFato tipoB = criarTipoFato("Conduta Tipo B");
        criarRegistro("CE-TIPO-A", tipoA);
        criarRegistro("CE-TIPO-B", tipoB);

        mockMvc.perform(get("/api/registros")
                        .header("Authorization", "Bearer " + token)
                        .param("tipoFatoId", tipoB.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].protocolo").value("CE-TIPO-B"))
                .andExpect(jsonPath("$.data.content[0].tipoFatoNome").value("Conduta Tipo B"));
    }

    @Test
    void shouldFilterRegistrosByStatus() throws Exception {
        String token = loginAndGetToken();
        criarRegistro("CE-STATUS-001", criarTipoFato("Conduta Status"));

        mockMvc.perform(get("/api/registros")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "RECEBIDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("RECEBIDO"));
    }

    @Test
    void shouldFilterRegistrosByPeriodo() throws Exception {
        String token = loginAndGetToken();
        criarRegistro("CE-PERIODO-001", criarTipoFato("Conduta Periodo"));
        String dataInicio = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        mockMvc.perform(get("/api/registros")
                        .header("Authorization", "Bearer " + token)
                        .param("dataInicio", dataInicio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").value(empty()));
    }

    @Test
    void shouldGetExistingRegistroDetail() throws Exception {
        String token = loginAndGetToken();
        Registro registro = criarRegistro("CE-DETALHE-001", criarTipoFato("Conduta Detalhe"));
        criarAnexo(registro, "evidencia-detalhe.pdf");

        mockMvc.perform(get("/api/registros/{id}", registro.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(registro.getId().toString()))
                .andExpect(jsonPath("$.data.protocolo").value("CE-DETALHE-001"))
                .andExpect(jsonPath("$.data.nome").value("Pessoa Relatora"))
                .andExpect(jsonPath("$.data.email").value("relatora@example.com"))
                .andExpect(jsonPath("$.data.telefone").value("11999999999"))
                .andExpect(jsonPath("$.data.fato").value("Conduta Detalhe"))
                .andExpect(jsonPath("$.data.tipoFatoId").value(registro.getTipoFato().getId()))
                .andExpect(jsonPath("$.data.tipoFatoNome").value("Conduta Detalhe"))
                .andExpect(jsonPath("$.data.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.data.origem").value("API_PUBLICA"))
                .andExpect(jsonPath("$.data.relato").value("Relato detalhado para consulta interna."))
                .andExpect(jsonPath("$.data.criadoEm").value(notNullValue()))
                .andExpect(jsonPath("$.data.atualizadoEm").value(notNullValue()))
                .andExpect(jsonPath("$.data.anexos[0].id").value(notNullValue()))
                .andExpect(jsonPath("$.data.anexos[0].nomeOriginal").value("evidencia-detalhe.pdf"))
                .andExpect(jsonPath("$.data.anexos[0].contentType").value("application/pdf"))
                .andExpect(jsonPath("$.data.anexos[0].tamanho").value(512))
                .andExpect(jsonPath("$.data.anexos[0].criadoEm").value(notNullValue()))
                .andExpect(jsonPath("$.message").value("Registro carregado."));
    }

    @Test
    void shouldReturnNotFoundForNonexistentRegistroDetail() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/registros/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Registro nao encontrado."));
    }

    @Test
    void shouldNotExposeInternalAttachmentFieldsOnDetail() throws Exception {
        String token = loginAndGetToken();
        Registro registro = criarRegistro("CE-ANEXO-INTERNO", criarTipoFato("Conduta Anexo Interno"));
        criarAnexo(registro, "evidencia-interna.pdf");

        mockMvc.perform(get("/api/registros/{id}", registro.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.anexos[0].nomeOriginal").value("evidencia-interna.pdf"))
                .andExpect(jsonPath("$.data.anexos[0].hash").doesNotExist())
                .andExpect(jsonPath("$.data.anexos[0].nomeFisico").doesNotExist())
                .andExpect(jsonPath("$.data.anexos[0].caminho").doesNotExist())
                .andExpect(jsonPath("$.data.anexos[0].caminhoFisico").doesNotExist());
    }

    @Test
    void shouldDownloadRegistroAnexoForAuthenticatedUser() throws Exception {
        String token = loginAndGetToken();
        Registro registro = criarRegistro("CE-DOWNLOAD-001", criarTipoFato("Conduta Download"));
        RegistroAnexo anexo = criarAnexo(registro, "evidencia-download.pdf");

        mockMvc.perform(get("/api/registros/{registroId}/anexos/{anexoId}", registro.getId(), anexo.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("evidencia-download.pdf")))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, not(containsString("hash-interno"))))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, not(containsString(anexo.getCaminho()))))
                .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, 512L));
    }

    @Test
    void shouldBlockRegistroAnexoDownloadWithoutToken() throws Exception {
        Registro registro = criarRegistro("CE-DOWNLOAD-SEM-TOKEN", criarTipoFato("Conduta Sem Token"));
        RegistroAnexo anexo = criarAnexo(registro, "evidencia-sem-token.pdf");

        mockMvc.perform(get("/api/registros/{registroId}/anexos/{anexoId}", registro.getId(), anexo.getId()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Acesso nao autorizado."));
    }

    @Test
    void shouldReturnNotFoundForNonexistentRegistroAnexo() throws Exception {
        String token = loginAndGetToken();
        Registro registro = criarRegistro("CE-DOWNLOAD-INEXISTENTE", criarTipoFato("Conduta Inexistente"));

        mockMvc.perform(get("/api/registros/{registroId}/anexos/{anexoId}", registro.getId(), UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Anexo nao encontrado."));
    }

    @Test
    void shouldReturnNotFoundWhenAnexoBelongsToAnotherRegistro() throws Exception {
        String token = loginAndGetToken();
        TipoFato tipoFato = criarTipoFato("Conduta Outro Registro");
        Registro registro = criarRegistro("CE-DOWNLOAD-REGISTRO-A", tipoFato);
        Registro outroRegistro = criarRegistro("CE-DOWNLOAD-REGISTRO-B", tipoFato);
        RegistroAnexo anexo = criarAnexo(outroRegistro, "evidencia-outro-registro.pdf");

        mockMvc.perform(get("/api/registros/{registroId}/anexos/{anexoId}", registro.getId(), anexo.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Anexo nao encontrado."));
    }

    private TipoFato criarTipoFato(String nome) {
        return tipoFatoRepository.saveAndFlush(new TipoFato(nome, true, 999));
    }

    private Registro criarRegistro(String protocolo, TipoFato tipoFato) {
        Registro registro = new Registro(
                protocolo,
                "Pessoa Relatora",
                "relatora@example.com",
                "11999999999",
                "Relato detalhado para consulta interna.",
                tipoFato,
                tipoFato.getNome()
        );
        return registroRepository.saveAndFlush(registro);
    }

    private RegistroAnexo criarAnexo(Registro registro, String nomeOriginal) throws IOException {
        String nomeFisico = UUID.randomUUID() + ".pdf";
        Path directory = Path.of("uploads", "registros", "anexos").toAbsolutePath().normalize();
        Files.createDirectories(directory);
        Path file = directory.resolve(nomeFisico).normalize();
        Files.write(file, new byte[512]);
        createdAnexoFiles.add(file);

        return registroAnexoRepository.saveAndFlush(new RegistroAnexo(
                registro,
                nomeOriginal,
                nomeFisico,
                "application/pdf",
                512L,
                "hash-interno-nao-exposto",
                "/uploads/registros/anexos/" + nomeFisico
        ));
    }

    private String loginAndGetToken() throws Exception {
        String content = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@baseplus.com",
                                  "password": "Baseplus@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(content);
        return json.path("data").path("token").asText();
    }
}
