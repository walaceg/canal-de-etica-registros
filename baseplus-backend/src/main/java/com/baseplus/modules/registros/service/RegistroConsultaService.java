package com.baseplus.modules.registros.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.core.storage.FileStorageService;
import com.baseplus.modules.registros.domain.OrigemRegistro;
import com.baseplus.modules.registros.domain.Registro;
import com.baseplus.modules.registros.domain.RegistroAnexo;
import com.baseplus.modules.registros.domain.StatusRegistro;
import com.baseplus.modules.registros.dto.RegistroAnexoResponse;
import com.baseplus.modules.registros.dto.RegistroDetalheResponse;
import com.baseplus.modules.registros.dto.RegistroResumoResponse;
import com.baseplus.modules.registros.repository.RegistroAnexoRepository;
import com.baseplus.modules.registros.repository.RegistroRepository;
import com.baseplus.shared.dto.PageResponse;

import jakarta.persistence.criteria.Predicate;

@Service
public class RegistroConsultaService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "criadoEm");

    private final RegistroRepository registroRepository;
    private final RegistroAnexoRepository registroAnexoRepository;
    private final FileStorageService fileStorageService;

    public RegistroConsultaService(
            RegistroRepository registroRepository,
            RegistroAnexoRepository registroAnexoRepository,
            FileStorageService fileStorageService
    ) {
        this.registroRepository = registroRepository;
        this.registroAnexoRepository = registroAnexoRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional(readOnly = true)
    public PageResponse<RegistroResumoResponse> listar(
            String protocolo,
            Long tipoFatoId,
            StatusRegistro status,
            OrigemRegistro origem,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Pageable pageable
    ) {
        Pageable resolvedPageable = resolvePageable(pageable);
        Page<Registro> registros = registroRepository.findAll(
                specification(protocolo, tipoFatoId, status, origem, dataInicio, dataFim),
                resolvedPageable
        );
        List<RegistroResumoResponse> content = registros.stream()
                .map(this::toResumoResponse)
                .toList();
        return PageResponse.from(registros, content);
    }

    @Transactional(readOnly = true)
    public RegistroDetalheResponse buscar(UUID id) {
        Registro registro = registroRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Registro nao encontrado.",
                        HttpStatus.NOT_FOUND,
                        List.of("Registro nao encontrado.")
                ));
        List<RegistroAnexoResponse> anexos = registroAnexoRepository.findByRegistro_IdOrderByCriadoEmAsc(id).stream()
                .map(this::toAnexoResponse)
                .toList();
        return toDetalheResponse(registro, anexos);
    }

    @Transactional(readOnly = true)
    public RegistroAnexoArquivo buscarArquivoAnexo(UUID registroId, UUID anexoId) {
        RegistroAnexo anexo = registroAnexoRepository.findByIdAndRegistro_Id(anexoId, registroId)
                .orElseThrow(this::anexoNaoEncontrado);
        Resource resource = fileStorageService.loadByUrl(anexo.getCaminho());
        return new RegistroAnexoArquivo(
                resource,
                anexo.getNomeOriginal(),
                anexo.getContentType(),
                anexo.getTamanho()
        );
    }

    private Pageable resolvePageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, 20, DEFAULT_SORT);
        }
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        }
        return pageable;
    }

    private Specification<Registro> specification(
            String protocolo,
            Long tipoFatoId,
            StatusRegistro status,
            OrigemRegistro origem,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (StringUtils.hasText(protocolo)) {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("protocolo")),
                                "%" + protocolo.trim().toLowerCase() + "%"
                        )
                );
            }
            if (tipoFatoId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("tipoFato").get("id"), tipoFatoId));
            }
            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
            }
            if (origem != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("origem"), origem));
            }
            if (dataInicio != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("criadoEm"), dataInicio));
            }
            if (dataFim != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("criadoEm"), dataFim));
            }

            return predicate;
        };
    }

    private RegistroResumoResponse toResumoResponse(Registro registro) {
        return new RegistroResumoResponse(
                registro.getId(),
                registro.getProtocolo(),
                registro.getTipoFatoNome(),
                registro.getStatus(),
                registro.getOrigem(),
                registro.getNome(),
                registro.getEmail(),
                registro.getTelefone(),
                registro.getCriadoEm(),
                registroAnexoRepository.countByRegistro_Id(registro.getId())
        );
    }

    private RegistroDetalheResponse toDetalheResponse(Registro registro, List<RegistroAnexoResponse> anexos) {
        return new RegistroDetalheResponse(
                registro.getId(),
                registro.getProtocolo(),
                registro.getNome(),
                registro.getEmail(),
                registro.getTelefone(),
                registro.getTipoFato().getId(),
                registro.getTipoFatoNome(),
                registro.getStatus(),
                registro.getOrigem(),
                registro.getRelato(),
                registro.getCriadoEm(),
                registro.getAtualizadoEm(),
                anexos
        );
    }

    private RegistroAnexoResponse toAnexoResponse(RegistroAnexo anexo) {
        return new RegistroAnexoResponse(
                anexo.getId(),
                anexo.getNomeOriginal(),
                anexo.getContentType(),
                anexo.getTamanho(),
                anexo.getCriadoEm()
        );
    }

    private BusinessException anexoNaoEncontrado() {
        return new BusinessException("Anexo nao encontrado.", HttpStatus.NOT_FOUND, List.of("Anexo nao encontrado."));
    }

    public record RegistroAnexoArquivo(
            Resource resource,
            String nomeOriginal,
            String contentType,
            Long tamanho
    ) {
    }
}
