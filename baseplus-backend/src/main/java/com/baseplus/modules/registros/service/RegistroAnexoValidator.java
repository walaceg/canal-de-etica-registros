package com.baseplus.modules.registros.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.baseplus.modules.registros.service.RegistroAnexoPolicy.AnexoRule;

@Component
public class RegistroAnexoValidator {

    private static final int ANEXO_NOME_MAX_LENGTH = 255;
    private static final int ANEXO_CONTENT_TYPE_MAX_LENGTH = 120;

    private final RegistroAnexoPolicy policy;

    public RegistroAnexoValidator(RegistroAnexoPolicy policy) {
        this.policy = policy;
    }

    public List<ValidatedAnexo> validate(MultipartFile[] anexos, List<String> errors) {
        List<ValidatedAnexo> validatedAnexos = new ArrayList<>();
        if (anexos == null || anexos.length == 0) {
            return validatedAnexos;
        }

        if (anexos.length > RegistroAnexoPolicy.MAX_QUANTIDADE_ANEXOS) {
            errors.add("Registro permite no maximo 10 anexos.");
        }

        long totalBytes = 0L;
        for (MultipartFile anexo : anexos) {
            if (anexo == null || anexo.isEmpty()) {
                errors.add("Anexo invalido.");
                continue;
            }

            String nomeOriginal = normalizeOriginalFilename(anexo.getOriginalFilename());
            String contentType = normalizeContentType(anexo.getContentType());
            totalBytes += anexo.getSize();

            if (nomeOriginal.length() > ANEXO_NOME_MAX_LENGTH) {
                errors.add("Nome original do anexo deve ter no maximo 255 caracteres.");
            }
            if (contentType.isBlank() || contentType.length() > ANEXO_CONTENT_TYPE_MAX_LENGTH) {
                errors.add("Tipo de conteudo do anexo e invalido.");
            }
            if (policy.isBlocked(nomeOriginal)) {
                errors.add("Tipo de anexo nao permitido: executaveis e scripts sao bloqueados.");
                continue;
            }
            if (policy.isCompressed(nomeOriginal)) {
                errors.add("Tipo de anexo nao permitido: arquivos compactados nao sao aceitos nesta versao.");
                continue;
            }

            AnexoRule rule = policy.findRule(nomeOriginal).orElse(null);
            if (rule == null) {
                errors.add("Extensao de anexo nao permitida.");
                continue;
            }
            if (!policy.isContentTypeAllowed(rule, contentType)) {
                errors.add("Tipo de conteudo do anexo nao corresponde a extensao informada.");
            }
            if (anexo.getSize() > rule.tipo().maxSizeBytes()) {
                errors.add("Anexo do tipo " + rule.tipo().label() + " excede o tamanho maximo permitido.");
            }

            validatedAnexos.add(new ValidatedAnexo(anexo, nomeOriginal, contentType, rule.extension(), rule.tipo()));
        }

        if (totalBytes > RegistroAnexoPolicy.MAX_TOTAL_BYTES) {
            errors.add("Tamanho total dos anexos excede 200 MB por registro.");
        }

        return validatedAnexos;
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

    public record ValidatedAnexo(
            MultipartFile file,
            String nomeOriginal,
            String contentType,
            String extension,
            RegistroAnexoTipo tipo
    ) {
    }
}
