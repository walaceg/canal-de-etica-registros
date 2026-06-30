package com.baseplus.modules.registros.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baseplus.modules.registros.dto.CriarRegistroRequest;
import com.baseplus.modules.registros.dto.CriarRegistroResponse;
import com.baseplus.modules.registros.service.RegistroPublicoService;
import com.baseplus.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/public/v1/registros")
@Tag(name = "Registros publicos", description = "Recebimento publico de registros do Canal de Etica.")
public class RegistroPublicoController {

    private final RegistroPublicoService registroPublicoService;

    public RegistroPublicoController(RegistroPublicoService registroPublicoService) {
        this.registroPublicoService = registroPublicoService;
    }

    @Operation(
            summary = "Receber registro publico",
            description = "Recebe um registro do Canal de Etica via multipart/form-data. Requer API Key no header X-API-Key.",
            security = @SecurityRequirement(name = "API Key")
    )
    @Parameter(name = "X-API-Key", description = "API Key do cliente autorizado.", required = true, in = ParameterIn.HEADER)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Formulario multipart com protocolo, tipoFatoId, relato e anexos opcionais.",
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = CriarRegistroRequest.class),
                    examples = @ExampleObject(
                            name = "Registro com anexo",
                            value = """
                                    {
                                      "protocolo": "CE-2026-000001",
                                      "tipoFatoId": 1,
                                      "relato": "Relato recebido pela API publica.",
                                      "nome": "Pessoa Relatora",
                                      "email": "relatora@example.com",
                                      "telefone": "+55 11 99999-9999",
                                      "anexos[]": ["evidencia.png"]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Registro recebido com sucesso. Em execucao a API retorna HTTP 201 Created.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "id": "0d0b8d7a-0c8e-4a23-8f80-3a13e36b996a",
                                        "protocolo": "CE-2026-000001",
                                        "criadoEm": "2026-06-30T10:30:00",
                                        "quantidadeAnexos": 1
                                      },
                                      "message": "Registro recebido com sucesso.",
                                      "errors": []
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Dados invalidos, tipo inativo ou upload invalido.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "data": null,
                                      "message": "Dados invalidos.",
                                      "errors": ["Protocolo e obrigatorio."]
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "API Key ausente, invalida ou inativa.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "data": null,
                                      "message": "Acesso nao autorizado.",
                                      "errors": ["API key obrigatoria ou invalida."]
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Protocolo duplicado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "data": null,
                                      "message": "Protocolo duplicado.",
                                      "errors": ["Ja existe um registro com este protocolo."]
                                    }
                                    """)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = """
                                    {
                                      "success": false,
                                      "data": null,
                                      "message": "Erro interno do servidor.",
                                      "errors": ["Ocorreu um erro inesperado."]
                                    }
                                    """)
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CriarRegistroResponse>> criar(
            @ModelAttribute CriarRegistroRequest request,
            @Parameter(description = "Arquivos anexos opcionais.", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            @RequestParam(value = "anexos[]", required = false) MultipartFile[] anexos
    ) {
        if (anexos != null && anexos.length > 0) {
            request.setAnexos(anexos);
        }

        CriarRegistroResponse response = registroPublicoService.criar(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registro recebido com sucesso."));
    }
}
