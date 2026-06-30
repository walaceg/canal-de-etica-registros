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

@RestController
@RequestMapping("/api/public/v1/registros")
public class RegistroPublicoController {

    private final RegistroPublicoService registroPublicoService;

    public RegistroPublicoController(RegistroPublicoService registroPublicoService) {
        this.registroPublicoService = registroPublicoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CriarRegistroResponse>> criar(
            @ModelAttribute CriarRegistroRequest request,
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
