package com.baseplus.modules.registros.service;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class RegistroAnexoPolicy {

    public static final int MAX_QUANTIDADE_ANEXOS = 10;
    public static final long MAX_TOTAL_BYTES = 200L * 1024L * 1024L;

    private static final Set<String> BLOQUEADOS = Set.of(
            ".exe", ".bat", ".cmd", ".com", ".msi", ".dll", ".js", ".jar", ".ps1", ".vbs", ".scr", ".sh"
    );
    private static final Set<String> COMPACTADOS = Set.of(".zip", ".rar", ".7z", ".tar", ".gz");

    private static final Map<String, AnexoRule> RULES = Map.ofEntries(
            rule(".pdf", RegistroAnexoTipo.DOCUMENTO, "application/pdf"),
            rule(".doc", RegistroAnexoTipo.DOCUMENTO, "application/msword"),
            rule(".docx", RegistroAnexoTipo.DOCUMENTO, "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            rule(".xls", RegistroAnexoTipo.DOCUMENTO, "application/vnd.ms-excel"),
            rule(".xlsx", RegistroAnexoTipo.DOCUMENTO, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            rule(".ppt", RegistroAnexoTipo.DOCUMENTO, "application/vnd.ms-powerpoint"),
            rule(".pptx", RegistroAnexoTipo.DOCUMENTO, "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
            rule(".txt", RegistroAnexoTipo.DOCUMENTO, "text/plain"),
            rule(".csv", RegistroAnexoTipo.DOCUMENTO, "text/csv", "application/csv", "application/vnd.ms-excel"),
            rule(".png", RegistroAnexoTipo.IMAGEM, "image/png"),
            rule(".jpg", RegistroAnexoTipo.IMAGEM, "image/jpeg", "image/jpg"),
            rule(".jpeg", RegistroAnexoTipo.IMAGEM, "image/jpeg", "image/jpg"),
            rule(".gif", RegistroAnexoTipo.IMAGEM, "image/gif"),
            rule(".bmp", RegistroAnexoTipo.IMAGEM, "image/bmp", "image/x-ms-bmp"),
            rule(".webp", RegistroAnexoTipo.IMAGEM, "image/webp"),
            rule(".ico", RegistroAnexoTipo.IMAGEM, "image/x-icon", "image/vnd.microsoft.icon"),
            rule(".mp4", RegistroAnexoTipo.VIDEO, "video/mp4"),
            rule(".mov", RegistroAnexoTipo.VIDEO, "video/quicktime"),
            rule(".avi", RegistroAnexoTipo.VIDEO, "video/x-msvideo", "video/avi"),
            rule(".mkv", RegistroAnexoTipo.VIDEO, "video/x-matroska"),
            rule(".webm", RegistroAnexoTipo.VIDEO, "video/webm"),
            rule(".mp3", RegistroAnexoTipo.AUDIO, "audio/mpeg", "audio/mp3"),
            rule(".wav", RegistroAnexoTipo.AUDIO, "audio/wav", "audio/x-wav"),
            rule(".m4a", RegistroAnexoTipo.AUDIO, "audio/mp4", "audio/x-m4a"),
            rule(".ogg", RegistroAnexoTipo.AUDIO, "audio/ogg", "application/ogg")
    );

    public Optional<AnexoRule> findRule(String filename) {
        String extension = extensionOf(filename);
        return Optional.ofNullable(RULES.get(extension));
    }

    public boolean isBlocked(String filename) {
        return BLOQUEADOS.contains(extensionOf(filename));
    }

    public boolean isCompressed(String filename) {
        return COMPACTADOS.contains(extensionOf(filename));
    }

    public boolean isContentTypeAllowed(AnexoRule rule, String contentType) {
        return rule.contentTypes().contains(normalizeContentType(contentType));
    }

    public String extensionOf(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        String normalized = filename.trim().toLowerCase(Locale.ROOT);
        int index = normalized.lastIndexOf('.');
        if (index < 0 || index == normalized.length() - 1) {
            return "";
        }
        return normalized.substring(index);
    }

    private static Map.Entry<String, AnexoRule> rule(String extension, RegistroAnexoTipo tipo, String... contentTypes) {
        return Map.entry(extension, new AnexoRule(extension, tipo, Set.of(contentTypes)));
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
    }

    public record AnexoRule(String extension, RegistroAnexoTipo tipo, Set<String> contentTypes) {
    }
}
