package com.baseplus.modules.registros.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class RegistroAnexoValidatorTest {

    private RegistroAnexoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RegistroAnexoValidator(new RegistroAnexoPolicy());
    }

    @Test
    void shouldAcceptPdfImageVideoAndAudio() {
        List<String> errors = new ArrayList<>();

        List<RegistroAnexoValidator.ValidatedAnexo> anexos = validator.validate(new MultipartFile[] {
                file("documento.pdf", "application/pdf", 1024L),
                file("imagem.png", "image/png", 1024L),
                file("video.mp4", "video/mp4", 1024L),
                file("audio.mp3", "audio/mpeg", 1024L)
        }, errors);

        assertThat(errors).isEmpty();
        assertThat(anexos)
                .extracting(RegistroAnexoValidator.ValidatedAnexo::tipo)
                .containsExactly(
                        RegistroAnexoTipo.DOCUMENTO,
                        RegistroAnexoTipo.IMAGEM,
                        RegistroAnexoTipo.VIDEO,
                        RegistroAnexoTipo.AUDIO
                );
    }

    @Test
    void shouldRejectExecutableAttachment() {
        List<String> errors = validate(file("script.exe", "application/octet-stream", 1024L));

        assertThat(errors).contains("Tipo de anexo nao permitido: executaveis e scripts sao bloqueados.");
    }

    @Test
    void shouldRejectCompressedAttachment() {
        List<String> errors = validate(file("evidencias.zip", "application/zip", 1024L));

        assertThat(errors).contains("Tipo de anexo nao permitido: arquivos compactados nao sao aceitos nesta versao.");
    }

    @Test
    void shouldRejectAttachmentAboveCategoryLimit() {
        List<String> errors = validate(file("video.mp4", "video/mp4", 101L * 1024L * 1024L));

        assertThat(errors).contains("Anexo do tipo Video excede o tamanho maximo permitido.");
    }

    @Test
    void shouldRejectMoreThanTenAttachments() {
        MultipartFile[] anexos = new MultipartFile[11];
        for (int index = 0; index < anexos.length; index++) {
            anexos[index] = file("documento-" + index + ".pdf", "application/pdf", 1024L);
        }

        List<String> errors = new ArrayList<>();
        validator.validate(anexos, errors);

        assertThat(errors).contains("Registro permite no maximo 10 anexos.");
    }

    @Test
    void shouldRejectTotalAboveTwoHundredMb() {
        List<String> errors = validate(new MultipartFile[] {
                file("video-1.mp4", "video/mp4", 80L * 1024L * 1024L),
                file("video-2.mp4", "video/mp4", 80L * 1024L * 1024L),
                file("video-3.mp4", "video/mp4", 50L * 1024L * 1024L)
        });

        assertThat(errors).contains("Tamanho total dos anexos excede 200 MB por registro.");
    }

    private List<String> validate(MultipartFile file) {
        return validate(new MultipartFile[] {file});
    }

    private List<String> validate(MultipartFile[] files) {
        List<String> errors = new ArrayList<>();
        validator.validate(files, errors);
        return errors;
    }

    private MultipartFile file(String filename, String contentType, long size) {
        return new SizedMultipartFile("anexos[]", filename, contentType, size);
    }

    private record SizedMultipartFile(
            String name,
            String originalFilename,
            String contentType,
            long size
    ) implements MultipartFile {

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return size <= 0;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public byte[] getBytes() {
            return new byte[] {1};
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(getBytes());
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
            throw new UnsupportedOperationException("Nao utilizado neste teste.");
        }
    }
}
