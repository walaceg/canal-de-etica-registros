package com.baseplus.modules.registros.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "registro")
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false, length = 80)
    private String protocolo;

    @Column(updatable = false, length = 160)
    private String nome;

    @Column(updatable = false, length = 160)
    private String email;

    @Column(updatable = false, length = 40)
    private String telefone;

    @Column(nullable = false, updatable = false, columnDefinition = "text")
    private String relato;

    @Column(nullable = false, updatable = false, columnDefinition = "text")
    private String fato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_fato_id", updatable = false)
    private TipoFato tipoFato;

    @Column(updatable = false, length = 180)
    private String tipoFatoNome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatusRegistro status = StatusRegistro.RECEBIDO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 40)
    private OrigemRegistro origem = OrigemRegistro.API_PUBLICA;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    protected Registro() {
    }

    public Registro(
            String protocolo,
            String nome,
            String email,
            String telefone,
            String relato,
            String fato
    ) {
        this(protocolo, nome, email, telefone, relato, fato, null, null);
    }

    public Registro(
            String protocolo,
            String nome,
            String email,
            String telefone,
            String relato,
            TipoFato tipoFato,
            String tipoFatoNome
    ) {
        this(protocolo, nome, email, telefone, relato, tipoFatoNome, tipoFato, tipoFatoNome);
    }

    private Registro(
            String protocolo,
            String nome,
            String email,
            String telefone,
            String relato,
            String fato,
            TipoFato tipoFato,
            String tipoFatoNome
    ) {
        this.protocolo = protocolo;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.relato = relato;
        this.fato = fato;
        this.tipoFato = tipoFato;
        this.tipoFatoNome = tipoFatoNome;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (status == null) {
            status = StatusRegistro.RECEBIDO;
        }
        if (origem == null) {
            origem = OrigemRegistro.API_PUBLICA;
        }
        if (criadoEm == null) {
            criadoEm = now;
        }
        if (atualizadoEm == null) {
            atualizadoEm = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getRelato() {
        return relato;
    }

    public String getFato() {
        return fato;
    }

    public TipoFato getTipoFato() {
        return tipoFato;
    }

    public String getTipoFatoNome() {
        return tipoFatoNome;
    }

    public StatusRegistro getStatus() {
        return status;
    }

    public void setStatus(StatusRegistro status) {
        this.status = status;
    }

    public OrigemRegistro getOrigem() {
        return origem;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
