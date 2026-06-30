package com.baseplus.modules.registros.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistroAnexoResponse(
        UUID id,
        String nomeOriginal,
        String contentType,
        Long tamanho,
        LocalDateTime criadoEm
) {
}
