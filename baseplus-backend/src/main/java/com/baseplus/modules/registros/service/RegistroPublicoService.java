package com.baseplus.modules.registros.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.core.storage.FileStorageService;
import com.baseplus.core.storage.StoredFile;
import com.baseplus.modules.registros.domain.Registro;
import com.baseplus.modules.registros.domain.RegistroAnexo;
import com.baseplus.modules.registros.domain.TipoFato;
import com.baseplus.modules.registros.dto.CriarRegistroRequest;
import com.baseplus.modules.registros.dto.CriarRegistroResponse;
import com.baseplus.modules.registros.repository.RegistroAnexoRepository;
import com.baseplus.modules.registros.repository.RegistroRepository;
import com.baseplus.modules.registros.repository.TipoFatoRepository;

@Service
public class RegistroPublicoService {

    private static final int PROTOCOLO_MAX_LENGTH = 80;
    private static final int NOME_MAX_LENGTH = 160;
    private static final int EMAIL_MAX_LENGTH = 160;
    private static final int TELEFONE_MAX_LENGTH = 40;
    private static final int ANEXO_NOME_MAX_LENGTH = 255;
    private static final int ANEXO_CONTENT_TYPE_MAX_LENGTH = 120;
    private static final long MAX_ANEXO_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final String ANEXO_UPLOAD_DIR = "registros/anexos";
    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    private final RegistroRepository registroRepository;
    private final TipoFatoRepository tipoFatoRepository;
    private final RegistroAnexoRepository registroAnexoRepository;
    private final FileStorageService fileStorageService;

    public RegistroPublicoService(
            RegistroRepository registroRepository,
            TipoFatoRepository tipoFatoRepository,
            RegistroAnexoRepository registroAnexoRepository,
            FileStorageService fileStorageService
    ) {
        this.registroRepository = registroRepository;
        this.tipoFatoRepository = tipoFatoRepository;
        this.registroAnexoRepository = registroAnexoRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public CriarRegistroResponse criar(CriarRegistroRequest request) {
        ValidatedRegistroInput input = validateRequest(request);

        TipoFato tipoFato = tipoFatoRepository.findById(input.tipoFatoId())
                .orElseThrow(() -> new BusinessException(
                        "Tipo de fato nao encontrado.",
                        HttpStatus.NOT_FOUND,
                        List.of("Tipo de fato informado nao existe.")
                ));

        if (!Boolean.TRUE.equals(tipoFato.getAtivo())) {
            throw new BusinessException(
                    "Tipo de fato inativo.",
                    HttpStatus.BAD_REQUEST,
                    List.of("Tipo de fato informado nao esta ativo.")
            );
        }

        if (registroRepository.existsByProtocolo(input.protocolo())) {
            throw protocoloDuplicado();
        }

        Registro registro = new Registro(
                input.protocolo(),
                input.nome(),
                input.email(),
                input.telefone(),
                input.relato(),
                tipoFato,
                tipoFato.getNome()
        );

        try {
            registro = registroRepository.saveAndFlush(registro);
        } catch (DataIntegrityViolationException exception) {
            throw protocoloDuplicado();
        }

        List<String> uploadedUrls = new ArrayList<>();
        int quantidadeAnexos;
        try {
            quantidadeAnexos = salvarAnexos(registro, input.anexos(), uploadedUrls);
        } catch (RuntimeException exception) {
            cleanupUploadedFiles(uploadedUrls);
            throw exception;
        }

        return new CriarRegistroResponse(
                registro.getId(),
                registro.getProtocolo(),
                registro.getCriadoEm(),
                quantidadeAnexos
        );
    }

    private ValidatedRegistroInput validateRequest(CriarRegistroRequest request) {
        if (request == null) {
            throw dadosInvalidos(List.of("Dados do registro sao obrigatorios."));
        }

        List<String> errors = new ArrayList<>();
        String protocolo = requiredTrimmed(request.getProtocolo(), "Protocolo e obrigatorio.", errors);
        String relato = requiredTrimmed(request.getRelato(), "Relato e obrigatorio.", errors);
        Long tipoFatoId = request.getTipoFatoId();
        if (tipoFatoId == null) {
            errors.add("Tipo de fato e obrigatorio.");
        }

        validateMaxLength(protocolo, PROTOCOLO_MAX_LENGTH, "Protocolo deve ter no maximo 80 caracteres.", errors);

        String nome = optionalTrimmed(request.getNome());
        validateMaxLength(nome, NOME_MAX_LENGTH, "Nome deve ter no maximo 160 caracteres.", errors);

        String email = optionalTrimmed(request.getEmail());
        validateMaxLength(email, EMAIL_MAX_LENGTH, "Email deve ter no maximo 160 caracteres.", errors);
        if (email != null && !email.matches(EMAIL_PATTERN)) {
            errors.add("Email invalido.");
        }

        String telefone = optionalTrimmed(request.getTelefone());
        validateMaxLength(telefone, TELEFONE_MAX_LENGTH, "Telefone deve ter no maximo 40 caracteres.", errors);

        MultipartFile[] anexos = normalizeAnexos(request.getAnexos());
        validateAnexos(anexos, errors);

        if (!errors.isEmpty()) {
            throw dadosInvalidos(errors);
        }

        return new ValidatedRegistroInput(protocolo, tipoFatoId, relato, nome, email, telefone, anexos);
    }

    private int salvarAnexos(Registro registro, MultipartFile[] anexos, List<String> uploadedUrls) {
        int quantidadeAnexos = 0;
        for (MultipartFile anexo : anexos) {
            byte[] content = readBytes(anexo);
            String hash = sha256(content);
            StoredFile storedFile = fileStorageService.saveImage(anexo, ANEXO_UPLOAD_DIR, MAX_ANEXO_SIZE_BYTES);
            String caminho = storedFile.url();
            uploadedUrls.add(caminho);
            RegistroAnexo registroAnexo = new RegistroAnexo(
                    registro,
                    normalizeOriginalFilename(anexo.getOriginalFilename()),
                    extractFilename(caminho),
                    normalizeContentType(anexo.getContentType()),
                    anexo.getSize(),
                    hash,
                    caminho
            );
            registroAnexoRepository.save(registroAnexo);
            quantidadeAnexos++;
        }
        return quantidadeAnexos;
    }

    private void cleanupUploadedFiles(List<String> uploadedUrls) {
        for (String uploadedUrl : uploadedUrls) {
            try {
                fileStorageService.deleteByUrl(uploadedUrl);
            } catch (RuntimeException ignored) {
                // Preserva o erro original da criacao do registro.
            }
        }
    }

    private void validateAnexos(MultipartFile[] anexos, List<String> errors) {
        for (MultipartFile anexo : anexos) {
            if (anexo == null || anexo.isEmpty()) {
                errors.add("Anexo invalido.");
                continue;
            }

            String nomeOriginal = normalizeOriginalFilename(anexo.getOriginalFilename());
            if (nomeOriginal.length() > ANEXO_NOME_MAX_LENGTH) {
                errors.add("Nome original do anexo deve ter no maximo 255 caracteres.");
            }

            String contentType = normalizeContentType(anexo.getContentType());
            if (contentType.isBlank() || contentType.length() > ANEXO_CONTENT_TYPE_MAX_LENGTH) {
                errors.add("Tipo de conteudo do anexo e invalido.");
            }

            if (anexo.getSize() > MAX_ANEXO_SIZE_BYTES) {
                errors.add("Anexo excede o tamanho maximo permitido.");
            }
        }
    }

    private MultipartFile[] normalizeAnexos(MultipartFile[] anexos) {
        if (anexos == null || anexos.length == 0) {
            return new MultipartFile[0];
        }
        return anexos;
    }

    private String requiredTrimmed(String value, String error, List<String> errors) {
        String normalized = optionalTrimmed(value);
        if (normalized == null) {
            errors.add(error);
        }
        return normalized;
    }

    private String optionalTrimmed(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void validateMaxLength(String value, int maxLength, String error, List<String> errors) {
        if (value != null && value.length() > maxLength) {
            errors.add(error);
        }
    }

    private BusinessException dadosInvalidos(List<String> errors) {
        return new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, errors);
    }

    private BusinessException protocoloDuplicado() {
        return new BusinessException(
                "Protocolo duplicado.",
                HttpStatus.CONFLICT,
                List.of("Ja existe um registro com este protocolo.")
        );
    }

    private String normalizeOriginalFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "anexo";
        }
        return filename.trim();
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
    }

    private String extractFilename(String caminho) {
        int index = caminho == null ? -1 : caminho.lastIndexOf('/');
        if (index < 0 || index == caminho.length() - 1) {
            return caminho;
        }
        return caminho.substring(index + 1);
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, List.of("Nao foi possivel ler o anexo."));
        }
    }

    private String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponivel.", exception);
        }
    }

    private record ValidatedRegistroInput(
            String protocolo,
            Long tipoFatoId,
            String relato,
            String nome,
            String email,
            String telefone,
            MultipartFile[] anexos
    ) {
    }
}
