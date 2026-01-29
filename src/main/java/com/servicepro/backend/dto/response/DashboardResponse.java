package com.servicepro.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    
    // Informações básicas
    private Long prestadorId;
    private String prestadorNome;
    private String periodo;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    
    // Estatísticas principais
    private Long totalServicos;
    private Long servicosAgendados;
    private Long servicosConfirmados;
    private Long servicosEmAndamento;
    private Long servicosConcluidos;
    private Long servicosCancelados;
    
    // Faturamento
    private BigDecimal faturamentoTotal;
    private BigDecimal faturamentoPeriodo;
    private BigDecimal faturamentoMedio;
    
    // Métricas adicionais
    private BigDecimal taxaConclusao; // porcentagem
    private BigDecimal taxaCancelamento; // porcentagem
    private Long clientesUnicos;
    
    // Evolução (últimos 6 meses)
    private Map<String, BigDecimal> evolucaoFaturamento;
    private Map<String, Long> evolucaoServicos;
    
    // Serviços por status (para gráficos)
    private Map<String, Long> distribuicaoStatus;
    
    // Top clientes
    private Map<String, Long> topClientes;
    
    // Horários mais movimentados
    private Map<String, Long> horariosPico;
}