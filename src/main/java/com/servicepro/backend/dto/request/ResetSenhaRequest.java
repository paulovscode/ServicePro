package com.servicepro.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ResetSenhaRequest {
    
    @NotBlank(message = "Token é obrigatório")
    private String token;
    
    @NotBlank(message = "Nova senha é obrigatória")
    private String novaSenha;
    
    @NotBlank(message = "Confirmação da senha é obrigatória")
    private String confirmacaoSenha;
    
    // Getters e Setters MANUAIS
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getNovaSenha() {
        return novaSenha;
    }
    
    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }
    
    public String getConfirmacaoSenha() {
        return confirmacaoSenha;
    }
    
    public void setConfirmacaoSenha(String confirmacaoSenha) {
        this.confirmacaoSenha = confirmacaoSenha;
    }
}