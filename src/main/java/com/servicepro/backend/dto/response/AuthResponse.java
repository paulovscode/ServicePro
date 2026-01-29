// AuthResponse.java
package com.servicepro.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;

    @Builder.Default
    private String tipoToken = "Bearer";

    private Long prestadorId;
    private String nomeCompleto;
    private String email;
    private String telefone;
    private Long expiraEm;
}