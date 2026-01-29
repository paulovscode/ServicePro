// ServicoResponse.java
package com.servicepro.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicoResponse {
    private Long id;
    private String clienteNome;
    private String clienteTelefone;
    private String clienteEmail;
    private String endereco;
    private LocalDateTime dataAgendamento;
    private LocalDateTime dataConclusao;
    private BigDecimal valor;
    private String status;
    private String statusDescricao;
    private String observacoes;
    private Long prestadorId;
    private String prestadorNome;
    private LocalDateTime dataCriacao;
    private Double latitude;
    private Double longitude;
    
    // Para o app Android
    private String dataAgendamentoFormatada;
    private String valorFormatado;
}