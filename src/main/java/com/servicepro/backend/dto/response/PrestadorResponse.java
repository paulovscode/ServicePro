// PrestadorResponse.java
package com.servicepro.backend.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrestadorResponse {
    private Long id;
    private String nomeCompleto;
    private String cpfCnpj;
    private String email;
    private String telefone;
    private String fotoPerfil;
    private LocalDateTime dataCriacao;
    private Boolean ativo;
    private Boolean emailVerificado;
}