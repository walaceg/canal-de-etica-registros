package com.baseplus.modules.registros.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baseplus.modules.registros.domain.TipoFato;

public interface TipoFatoRepository extends JpaRepository<TipoFato, Long> {

    Optional<TipoFato> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}
