package com.baseplus.modules.registros.service;

public enum RegistroAnexoTipo {
    DOCUMENTO("Documento", 20L * 1024L * 1024L),
    IMAGEM("Imagem", 10L * 1024L * 1024L),
    VIDEO("Video", 100L * 1024L * 1024L),
    AUDIO("Audio", 30L * 1024L * 1024L);

    private final String label;
    private final long maxSizeBytes;

    RegistroAnexoTipo(String label, long maxSizeBytes) {
        this.label = label;
        this.maxSizeBytes = maxSizeBytes;
    }

    public String label() {
        return label;
    }

    public long maxSizeBytes() {
        return maxSizeBytes;
    }
}
