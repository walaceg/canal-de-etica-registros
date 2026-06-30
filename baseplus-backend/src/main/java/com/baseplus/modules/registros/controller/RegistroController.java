package com.baseplus.modules.registros.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baseplus.modules.registros.domain.OrigemRegistro;
import com.baseplus.modules.registros.domain.StatusRegistro;
import com.baseplus.modules.registros.dto.RegistroDetalheResponse;
import com.baseplus.modules.registros.dto.RegistroResumoResponse;
import com.baseplus.modules.registros.service.RegistroConsultaService;
import com.baseplus.shared.dto.ApiResponse;
import com.baseplus.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/registros")
@Tag(name = "Registros BackOffice", description = "Consulta interna de registros do Canal de Etica.")
public class RegistroController {

    private final RegistroConsultaService registroConsultaService;

    public RegistroController(RegistroConsultaService registroConsultaService) {
        this.registroConsultaService = registroConsultaService;
    }

    @Operation(
            summary = "Listar registros",
            description = "Lista registros recebidos com paginacao e filtros opcionais. Requer usuario autenticado com permissao REGISTROS_VIEW.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Registros carregados.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "content": [{
                                          "id": "0d0b8d7a-0c8e-4a23-8f80-3a13e36b996a",
                                          "protocolo": "CE-2026-000001",
                                          "tipoFatoNome": "Conduta",
                                          "status": "RECEBIDO",
                                          "origem": "API_PUBLICA",
                                          "nome": "Pessoa Relatora",
                                          "email": "relatora@example.com",
                                          "telefone": "+55 11 99999-9999",
                                          "criadoEm": "2026-06-30T10:30:00",
                                          "quantidadeAnexos": 2
                                        }],
                                        "page": 0,
                                        "size": 20,
                                        "totalElements": 1,
                                        "totalPages": 1
                                      },
                                      "message": "Registros carregados.",
                                      "errors": []
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Usuario nao autenticado."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuario sem permissao."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Nao utilizado na listagem.")
    })
    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('REGISTROS_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<RegistroResumoResponse>>> listar(
            @Parameter(description = "Filtro parcial por protocolo.") @RequestParam(required = false) String protocolo,
            @Parameter(description = "Filtro pelo id do tipo de fato.") @RequestParam(required = false) Long tipoFatoId,
            @Parameter(description = "Filtro por status.") @RequestParam(required = false) StatusRegistro status,
            @Parameter(description = "Filtro por origem.") @RequestParam(required = false) OrigemRegistro origem,
            @Parameter(description = "Data inicial de criacao em ISO-8601.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @Parameter(description = "Data final de criacao em ISO-8601.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @Parameter(description = "Paginacao. Padrao: page=0, size=20, sort=criadoEm,desc.")
            @PageableDefault(size = 20, sort = {"criadoEm"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<RegistroResumoResponse> response = registroConsultaService.listar(
                protocolo,
                tipoFatoId,
                status,
                origem,
                dataInicio,
                dataFim,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Registros carregados."));
    }

    @Operation(
            summary = "Detalhar registro",
            description = "Retorna os detalhes de um registro recebido. Requer usuario autenticado com permissao REGISTROS_DETAIL.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Registro carregado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "id": "0d0b8d7a-0c8e-4a23-8f80-3a13e36b996a",
                                        "protocolo": "CE-2026-000001",
                                        "nome": "Pessoa Relatora",
                                        "email": "relatora@example.com",
                                        "telefone": "+55 11 99999-9999",
                                        "tipoFatoId": 1,
                                        "tipoFatoNome": "Conduta",
                                        "status": "RECEBIDO",
                                        "origem": "API_PUBLICA",
                                        "relato": "Relato recebido pela API publica.",
                                        "criadoEm": "2026-06-30T10:30:00",
                                        "atualizadoEm": "2026-06-30T10:30:00",
                                        "anexos": []
                                      },
                                      "message": "Registro carregado.",
                                      "errors": []
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Usuario nao autenticado."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Usuario sem permissao."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Registro nao encontrado.")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('REGISTROS_DETAIL')")
    public ResponseEntity<ApiResponse<RegistroDetalheResponse>> buscar(@PathVariable UUID id) {
        RegistroDetalheResponse response = registroConsultaService.buscar(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Registro carregado."));
    }
}
