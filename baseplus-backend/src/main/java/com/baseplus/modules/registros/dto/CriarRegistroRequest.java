package com.baseplus.modules.registros.dto;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public class CriarRegistroRequest {

    @Schema(description = "Protocolo gerado externamente.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String protocolo;

    @Schema(description = "Fato informado em texto livre pelo formulario externo.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fato;

    @Schema(description = "Relato completo informado pelo denunciante.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String relato;

    private String nome;
    private String email;
    private String telefone;
    private MultipartFile[] anexos;

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getFato() {
        return fato;
    }

    public void setFato(String fato) {
        this.fato = fato;
    }

    public String getRelato() {
        return relato;
    }

    public void setRelato(String relato) {
        this.relato = relato;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public MultipartFile[] getAnexos() {
        return anexos;
    }

    public void setAnexos(MultipartFile[] anexos) {
        this.anexos = anexos;
    }
}
