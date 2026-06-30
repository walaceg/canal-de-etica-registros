package com.baseplus.modules.registros.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baseplus.modules.registros.domain.RegistroAnexo;

public interface RegistroAnexoRepository extends JpaRepository<RegistroAnexo, UUID> {

    List<RegistroAnexo> findByRegistro_IdOrderByCriadoEmAsc(UUID registroId);
}
