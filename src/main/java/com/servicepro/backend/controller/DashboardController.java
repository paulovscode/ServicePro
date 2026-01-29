package com.servicepro.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicepro.backend.dto.response.ApiResponse;
import com.servicepro.backend.dto.response.DashboardResponse;
import com.servicepro.backend.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * Dashboard completo por período
     * GET /api/dashboard/{prestadorId}/{periodo}
     */
    @GetMapping("/{prestadorId}/{periodo}")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @PathVariable Long prestadorId,
            @PathVariable String periodo) {
        
        try {
            // Validar período
            String[] periodosValidos = {
                "hoje", "ontem", "semana", "mes", "trimestre", 
                "semestre", "ano", "ultimos_7_dias", "ultimos_30_dias",
                "ultimos_90_dias", "ultimos_365_dias"
            };
            
            boolean periodoValido = false;
            for (String p : periodosValidos) {
                if (p.equalsIgnoreCase(periodo)) {
                    periodoValido = true;
                    break;
                }
            }
            
            if (!periodoValido) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Período inválido. Use: " + String.join(", ", periodosValidos), 400)
                );
            }
            
            DashboardResponse dashboard = dashboardService.gerarDashboard(prestadorId, periodo.toUpperCase());
            
            return ResponseEntity.ok(
                ApiResponse.success(dashboard, "Dashboard gerado com sucesso")
            );
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro interno ao gerar dashboard", 500)
            );
        }
    }
    
    /**
     * Dashboard do mês atual (default)
     * GET /api/dashboard/{prestadorId}
     */
    @GetMapping("/{prestadorId}")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardMes(@PathVariable Long prestadorId) {
        return getDashboard(prestadorId, "MES");
    }
    
    /**
     * Resumo rápido para cards no app
     * GET /api/dashboard/{prestadorId}/resumo
     */
    @GetMapping("/{prestadorId}/resumo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResumo(@PathVariable Long prestadorId) {
        try {
            Map<String, Object> resumo = dashboardService.gerarResumoRapido(prestadorId);
            
            return ResponseEntity.ok(
                ApiResponse.success(resumo, "Resumo gerado com sucesso")
            );
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro interno ao gerar resumo", 500)
            );
        }
    }
    
    /**
     * Métricas específicas por status
     * GET /api/dashboard/{prestadorId}/status/{status}
     */
    @GetMapping("/{prestadorId}/status/{status}")
    public ResponseEntity<ApiResponse<Long>> getMetricaStatus(
            @PathVariable Long prestadorId,
            @PathVariable String status) {
        
        try {
            // Esta métrica seria implementada no DashboardService
            // Por enquanto, retornamos placeholder
            Long quantidade = 0L;
            
            return ResponseEntity.ok(
                ApiResponse.success(quantidade, "Métrica retornada")
            );
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro interno", 500)
            );
        }
    }
    
    /**
     * Evolução do faturamento (últimos 6 meses)
     * GET /api/dashboard/{prestadorId}/evolucao
     */
    @GetMapping("/{prestadorId}/evolucao")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEvolucao(@PathVariable Long prestadorId) {
        try {
            // Esta métrica já está incluída no DashboardService.gerarDashboard()
            // Poderíamos criar um método específico se necessário
            
            Map<String, Object> evolucao = Map.of(
                "mensagem", "Use /api/dashboard/{prestadorId}/MES para dados completos",
                "detalhe", "A evolução já está incluída no dashboard completo"
            );
            
            return ResponseEntity.ok(
                ApiResponse.success(evolucao, "Informação")
            );
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro interno", 500)
            );
        }
    }
}
