package com.baseplus.modules.registros.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.baseplus.modules.registros.domain.OrigemRegistro;
import com.baseplus.modules.registros.domain.StatusRegistro;

public record RegistroDetalheResponse(
        UUID id,
        String protocolo,
        String nome,
        String email,
        String telefone,
        String fato,
        Long tipoFatoId,
        String tipoFatoNome,
        StatusRegistro status,
        OrigemRegistro origem,
        String relato,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        List<RegistroAnexoResponse> anexos
) {
}
