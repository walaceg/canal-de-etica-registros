package com.baseplus.modules.registros.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

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
import com.baseplus.modules.registros.dto.CriarRegistroRequest;
import com.baseplus.modules.registros.dto.CriarRegistroResponse;
import com.baseplus.modules.registros.repository.RegistroAnexoRepository;
import com.baseplus.modules.registros.repository.RegistroRepository;
import com.baseplus.modules.registros.service.RegistroAnexoValidator.ValidatedAnexo;

@Service
public class RegistroPublicoService {

    private static final int PROTOCOLO_MAX_LENGTH = 80;
    private static final int NOME_MAX_LENGTH = 160;
    private static final int EMAIL_MAX_LENGTH = 160;
    private static final int TELEFONE_MAX_LENGTH = 40;
    private static final String ANEXO_UPLOAD_DIR = "registros/anexos";
    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    private final RegistroRepository registroRepository;
    private final RegistroAnexoRepository registroAnexoRepository;
    private final FileStorageService fileStorageService;
    private final RegistroAnexoValidator registroAnexoValidator;

    public RegistroPublicoService(
            RegistroRepository registroRepository,
            RegistroAnexoRepository registroAnexoRepository,
            FileStorageService fileStorageService,
            RegistroAnexoValidator registroAnexoValidator
    ) {
        this.registroRepository = registroRepository;
        this.registroAnexoRepository = registroAnexoRepository;
        this.fileStorageService = fileStorageService;
        this.registroAnexoValidator = registroAnexoValidator;
    }

    @Transactional
    public CriarRegistroResponse criar(CriarRegistroRequest request) {
        ValidatedRegistroInput input = validateRequest(request);

        if (registroRepository.existsByProtocolo(input.protocolo())) {
            throw protocoloDuplicado();
        }

        Registro registro = new Registro(
                input.protocolo(),
                input.nome(),
                input.email(),
                input.telefone(),
                input.relato(),
                input.fato()
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
        String fato = requiredTrimmed(request.getFato(), "Fato e obrigatorio.", errors);
        String relato = requiredTrimmed(request.getRelato(), "Relato e obrigatorio.", errors);

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

        List<ValidatedAnexo> anexos = registroAnexoValidator.validate(request.getAnexos(), errors);

        if (!errors.isEmpty()) {
            throw dadosInvalidos(errors);
        }

        return new ValidatedRegistroInput(protocolo, fato, relato, nome, email, telefone, anexos);
    }

    private int salvarAnexos(Registro registro, List<ValidatedAnexo> anexos, List<String> uploadedUrls) {
        int quantidadeAnexos = 0;
        for (ValidatedAnexo anexo : anexos) {
            byte[] content = readBytes(anexo.file());
            String hash = sha256(content);
            StoredFile storedFile = fileStorageService.save(anexo.file(), ANEXO_UPLOAD_DIR, anexo.extension());
            String caminho = storedFile.url();
            uploadedUrls.add(caminho);
            RegistroAnexo registroAnexo = new RegistroAnexo(
                    registro,
                    anexo.nomeOriginal(),
                    extractFilename(caminho),
                    anexo.contentType(),
                    anexo.file().getSize(),
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
            String fato,
            String relato,
            String nome,
            String email,
            String telefone,
            List<ValidatedAnexo> anexos
    ) {
    }
}
