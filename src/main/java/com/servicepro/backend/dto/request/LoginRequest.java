package com.servicepro.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "Email ou telefone é obrigatório")
    private String emailOuTelefone;
    
    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}