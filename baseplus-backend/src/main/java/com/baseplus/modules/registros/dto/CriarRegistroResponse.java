package com.baseplus.modules.registros.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CriarRegistroResponse(
        UUID id,
        String protocolo,
        LocalDateTime criadoEm,
        int quantidadeAnexos
) {
}
