package com.baseplus.modules.registros.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "registro_anexo")
public class RegistroAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registro_id", nullable = false)
    private Registro registro;

    @Column(nullable = false, length = 255)
    private String nomeOriginal;

    @Column(nullable = false, length = 255)
    private String nomeFisico;

    @Column(nullable = false, length = 120)
    private String contentType;

    @Column(nullable = false)
    private Long tamanho;

    @Column(nullable = false, length = 128)
    private String hash;

    @Column(nullable = false, length = 500)
    private String caminho;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    protected RegistroAnexo() {
    }

    public RegistroAnexo(
            Registro registro,
            String nomeOriginal,
            String nomeFisico,
            String contentType,
            Long tamanho,
            String hash,
            String caminho
    ) {
        this.registro = registro;
        this.nomeOriginal = nomeOriginal;
        this.nomeFisico = nomeFisico;
        this.contentType = contentType;
        this.tamanho = tamanho;
        this.hash = hash;
        this.caminho = caminho;
    }

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Registro getRegistro() {
        return registro;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public String getNomeFisico() {
        return nomeFisico;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getTamanho() {
        return tamanho;
    }

    public String getHash() {
        return hash;
    }

    public String getCaminho() {
        return caminho;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
