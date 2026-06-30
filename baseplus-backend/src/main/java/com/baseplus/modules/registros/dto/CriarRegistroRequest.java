package com.baseplus.modules.registros.dto;

import org.springframework.web.multipart.MultipartFile;

public class CriarRegistroRequest {

    private String protocolo;
    private Long tipoFatoId;
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

    public Long getTipoFatoId() {
        return tipoFatoId;
    }

    public void setTipoFatoId(Long tipoFatoId) {
        this.tipoFatoId = tipoFatoId;
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
