package com.baseplus.modules.registros.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.baseplus.modules.registros.domain.OrigemRegistro;
import com.baseplus.modules.registros.domain.StatusRegistro;

public record RegistroResumoResponse(
        UUID id,
        String protocolo,
        String tipoFatoNome,
        StatusRegistro status,
        OrigemRegistro origem,
        String nome,
        String email,
        String telefone,
        LocalDateTime criadoEm,
        long quantidadeAnexos
) {
}
