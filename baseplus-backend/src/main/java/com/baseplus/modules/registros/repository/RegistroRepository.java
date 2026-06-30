package com.baseplus.modules.registros.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baseplus.modules.registros.domain.Registro;

public interface RegistroRepository extends JpaRepository<Registro, UUID> {

    Optional<Registro> findByProtocolo(String protocolo);

    boolean existsByProtocolo(String protocolo);
}
