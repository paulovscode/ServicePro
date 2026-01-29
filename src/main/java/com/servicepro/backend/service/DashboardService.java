package com.servicepro.backend.service;

import com.servicepro.backend.dto.response.DashboardResponse;
import com.servicepro.backend.model.Prestador;
import com.servicepro.backend.model.Servico;
import com.servicepro.backend.repository.PrestadorRepository;
import com.servicepro.backend.repository.ServicoRepository;
import com.servicepro.backend.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final ServicoRepository servicoRepository;
    private final PrestadorRepository prestadorRepository;
    
    /**
     * Gera dashboard completo para um prestador
     */
    public DashboardResponse gerarDashboard(Long prestadorId, String periodo) {
        Prestador prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado com ID: " + prestadorId));
        
        // Calcular datas do período
        Map<String, LocalDateTime> datas = DateUtils.calcularPeriodo(periodo);
        LocalDateTime inicio = datas.get("inicio");
        LocalDateTime fim = datas.get("fim");
        
        // Buscar serviços do período
        List<Servico> servicos = servicoRepository
                .findByPrestadorAndDataAgendamentoBetween(prestador, inicio, fim);
        
        // Calcular todas as métricas
        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setPrestadorId(prestadorId);
        dashboard.setPrestadorNome(prestador.getNomeCompleto());
        dashboard.setPeriodo(periodo);
        dashboard.setDataInicio(inicio);
        dashboard.setDataFim(fim);
        
        // Estatísticas básicas
        calcularEstatisticasBasicas(dashboard, servicos);
        
        // Faturamento
        calcularFaturamento(dashboard, prestador, inicio, fim);
        
        // Métricas avançadas
        calcularMetricasAvancadas(dashboard, prestador, inicio);
        
        // Dados para gráficos
        calcularDadosGraficos(dashboard, prestador, inicio);
        
        return dashboard;
    }
    
    /**
     * Calcula estatísticas básicas
     */
    private void calcularEstatisticasBasicas(DashboardResponse dashboard, List<Servico> servicos) {
        long total = servicos.size();
        
        long agendados = servicos.stream()
                .filter(s -> s.getStatus() == Servico.StatusServico.AGENDADO)
                .count();
        
        long confirmados = servicos.stream()
                .filter(s -> s.getStatus() == Servico.StatusServico.CONFIRMADO)
                .count();
        
        long emAndamento = servicos.stream()
                .filter(s -> s.getStatus() == Servico.StatusServico.EM_ANDAMENTO)
                .count();
        
        long concluidos = servicos.stream()
                .filter(s -> s.getStatus() == Servico.StatusServico.CONCLUIDO)
                .count();
        
        long cancelados = servicos.stream()
                .filter(s -> s.getStatus() == Servico.StatusServico.CANCELADO)
                .count();
        
        dashboard.setTotalServicos(total);
        dashboard.setServicosAgendados(agendados);
        dashboard.setServicosConfirmados(confirmados);
        dashboard.setServicosEmAndamento(emAndamento);
        dashboard.setServicosConcluidos(concluidos);
        dashboard.setServicosCancelados(cancelados);
    }
    
    /**
     * Calcula faturamento
     */
    private void calcularFaturamento(DashboardResponse dashboard, Prestador prestador,
                                    LocalDateTime inicio, LocalDateTime fim) {
        // Faturamento total (todos os tempos)
        BigDecimal faturamentoTotal = servicoRepository.calcularFaturamentoTotal(prestador);
        dashboard.setFaturamentoTotal(faturamentoTotal);
        
        // Faturamento do período
        BigDecimal faturamentoPeriodo = servicoRepository.calcularFaturamentoPeriodo(prestador, inicio, fim);
        dashboard.setFaturamentoPeriodo(faturamentoPeriodo);
        
        // Faturamento médio
        Long totalConcluidos = servicoRepository.countByPrestadorAndStatus(prestador, Servico.StatusServico.CONCLUIDO);
        if (totalConcluidos > 0) {
            BigDecimal faturamentoMedio = faturamentoTotal
                    .divide(BigDecimal.valueOf(totalConcluidos), 2, RoundingMode.HALF_UP);
            dashboard.setFaturamentoMedio(faturamentoMedio);
        } else {
            dashboard.setFaturamentoMedio(BigDecimal.ZERO);
        }
    }
    
    /**
     * Calcula métricas avançadas
     */
    private void calcularMetricasAvancadas(DashboardResponse dashboard, Prestador prestador, LocalDateTime inicio) {
        // Taxa de conclusão
        Long totalServicos = servicoRepository.countByPrestador(prestador);
        Long totalConcluidos = servicoRepository.countByPrestadorAndStatus(prestador, Servico.StatusServico.CONCLUIDO);
        
        if (totalServicos > 0) {
            BigDecimal taxaConclusao = BigDecimal.valueOf(totalConcluidos)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalServicos), 2, RoundingMode.HALF_UP);
            dashboard.setTaxaConclusao(taxaConclusao);
            
            // Taxa de cancelamento
            Long totalCancelados = servicoRepository.countByPrestadorAndStatus(prestador, Servico.StatusServico.CANCELADO);
            BigDecimal taxaCancelamento = BigDecimal.valueOf(totalCancelados)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalServicos), 2, RoundingMode.HALF_UP);
            dashboard.setTaxaCancelamento(taxaCancelamento);
        } else {
            dashboard.setTaxaConclusao(BigDecimal.ZERO);
            dashboard.setTaxaCancelamento(BigDecimal.ZERO);
        }
        
        // Clientes únicos
        Long clientesUnicos = servicoRepository.countClientesUnicos(prestador);
        dashboard.setClientesUnicos(clientesUnicos);
    }
    
    /**
     * Calcula dados para gráficos
     */
    private void calcularDadosGraficos(DashboardResponse dashboard, Prestador prestador, LocalDateTime inicio) {
        // Distribuição por status
        Map<String, Long> distribuicaoStatus = new HashMap<>();
        for (Servico.StatusServico status : Servico.StatusServico.values()) {
            Long count = servicoRepository.countByPrestadorAndStatus(prestador, status);
            distribuicaoStatus.put(status.getDescricao(), count);
        }
        dashboard.setDistribuicaoStatus(distribuicaoStatus);
        
        // Top clientes (top 5)
        Pageable top5 = PageRequest.of(0, 5);
        List<Object[]> topClientesData = servicoRepository.findTopClientes(prestador, top5);
        Map<String, Long> topClientes = topClientesData.stream()
                .collect(Collectors.toMap(
                    row -> (String) row[0],
                    row -> (Long) row[1]
                ));
        dashboard.setTopClientes(topClientes);
        
        // Horários de pico
        List<Object[]> horariosPicoData = servicoRepository.findHorariosPico(prestador);
        Map<String, Long> horariosPico = horariosPicoData.stream()
                .collect(Collectors.toMap(
                    row -> String.format("%02d:00", row[0]),
                    row -> (Long) row[1]
                ));
        dashboard.setHorariosPico(horariosPico);
        
        // Evolução dos últimos 6 meses
        LocalDateTime seisMesesAtras = inicio.minusMonths(6);
        List<Object[]> evolucaoData = servicoRepository.findEvolucaoFaturamento(prestador, seisMesesAtras);
        
        Map<String, BigDecimal> evolucaoFaturamento = evolucaoData.stream()
                .collect(Collectors.toMap(
                    row -> (String) row[0],
                    row -> (BigDecimal) row[1]
                ));
        dashboard.setEvolucaoFaturamento(evolucaoFaturamento);
        
        // Para evolução de serviços, podemos fazer uma contagem por mês
        // (Implementação simplificada - na prática precisaria de query específica)
        Map<String, Long> evolucaoServicos = new HashMap<>();
        evolucaoFaturamento.forEach((mes, valor) -> {
            evolucaoServicos.put(mes, 10L); // Placeholder
        });
        dashboard.setEvolucaoServicos(evolucaoServicos);
    }
    
    /**
     * Gera resumo rápido (para cards no app)
     */
    public Map<String, Object> gerarResumoRapido(Long prestadorId) {
        Prestador prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado com ID: " + prestadorId));
        
        Map<String, Object> resumo = new HashMap<>();
        
        // Hoje
        Map<String, LocalDateTime> hoje = DateUtils.calcularPeriodo("HOJE");
        List<Servico> servicosHoje = servicoRepository
                .findByPrestadorAndDataAgendamentoBetween(prestador, hoje.get("inicio"), hoje.get("fim"));
        
        // Este mês
        Map<String, LocalDateTime> esteMes = DateUtils.calcularPeriodo("MES");
        BigDecimal faturamentoMes = servicoRepository
                .calcularFaturamentoPeriodo(prestador, esteMes.get("inicio"), esteMes.get("fim"));
        
        resumo.put("servicosHoje", servicosHoje.size());
        resumo.put("servicosAgendadosHoje", servicosHoje.stream()
                .filter(s -> s.getStatus() == Servico.StatusServico.AGENDADO).count());
        resumo.put("servicosConcluidosHoje", servicosHoje.stream()
                .filter(s -> s.getStatus() == Servico.StatusServico.CONCLUIDO).count());
        resumo.put("faturamentoMes", faturamentoMes);
        resumo.put("totalServicos", servicoRepository.countByPrestador(prestador));
        resumo.put("clientesUnicos", servicoRepository.countClientesUnicos(prestador));
        
        return resumo;
    }
}