package com.servicepro.backend.repository;

import com.servicepro.backend.model.Prestador;
import com.servicepro.backend.model.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {
    
    // Buscas básicas
    List<Servico> findByPrestador(Prestador prestador);
    Page<Servico> findByPrestador(Prestador prestador, Pageable pageable);
    
    List<Servico> findByPrestadorAndStatus(Prestador prestador, Servico.StatusServico status);
    
    // Buscas por período
    List<Servico> findByDataAgendamentoBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Servico> findByPrestadorAndDataAgendamentoBetween(Prestador prestador, LocalDateTime inicio, LocalDateTime fim);
    
    // Buscas com múltiplos status
    List<Servico> findByPrestadorAndStatusIn(Prestador prestador, List<Servico.StatusServico> status);
    
    // Métricas para dashboard
    @Query("SELECT COUNT(s) FROM Servico s WHERE s.prestador = :prestador")
    Long countByPrestador(@Param("prestador") Prestador prestador);
    
    @Query("SELECT COUNT(s) FROM Servico s WHERE s.prestador = :prestador AND s.status = :status")
    Long countByPrestadorAndStatus(@Param("prestador") Prestador prestador, 
                                @Param("status") Servico.StatusServico status);
    
    @Query("SELECT COALESCE(SUM(s.valor), 0) FROM Servico s WHERE s.prestador = :prestador AND s.status = 'CONCLUIDO'")
    BigDecimal calcularFaturamentoTotal(@Param("prestador") Prestador prestador);
    
    @Query("SELECT COALESCE(SUM(s.valor), 0) FROM Servico s WHERE s.prestador = :prestador AND s.status = 'CONCLUIDO' AND s.dataConclusao BETWEEN :inicio AND :fim")
    BigDecimal calcularFaturamentoPeriodo(@Param("prestador") Prestador prestador,
                                        @Param("inicio") LocalDateTime inicio,
                                        @Param("fim") LocalDateTime fim);
    
    // Métricas avançadas
    @Query("SELECT COUNT(DISTINCT s.clienteNome) FROM Servico s WHERE s.prestador = :prestador")
    Long countClientesUnicos(@Param("prestador") Prestador prestador);
    
    @Query("SELECT s.clienteNome, COUNT(s) as total FROM Servico s WHERE s.prestador = :prestador GROUP BY s.clienteNome ORDER BY total DESC")
    List<Object[]> findTopClientes(@Param("prestador") Prestador prestador, Pageable pageable);
    
    @Query("SELECT FUNCTION('HOUR', s.dataAgendamento), COUNT(s) FROM Servico s WHERE s.prestador = :prestador GROUP BY FUNCTION('HOUR', s.dataAgendamento) ORDER BY COUNT(s) DESC")
    List<Object[]> findHorariosPico(@Param("prestador") Prestador prestador);
    
    // Evolução mensal
    @Query("SELECT FUNCTION('DATE_FORMAT', s.dataConclusao, '%Y-%m'), SUM(s.valor) " +
        "FROM Servico s WHERE s.prestador = :prestador AND s.status = 'CONCLUIDO' " +
        "AND s.dataConclusao >= :dataInicio " +
        "GROUP BY FUNCTION('DATE_FORMAT', s.dataConclusao, '%Y-%m') " +
        "ORDER BY FUNCTION('DATE_FORMAT', s.dataConclusao, '%Y-%m')")
    List<Object[]> findEvolucaoFaturamento(@Param("prestador") Prestador prestador,
                                        @Param("dataInicio") LocalDateTime dataInicio);
    
    // Busca com filtros complexos
    @Query("SELECT s FROM Servico s WHERE " +
        "s.prestador = :prestador AND " +
        "(:clienteNome IS NULL OR s.clienteNome LIKE %:clienteNome%) AND " +
        "(:status IS NULL OR s.status = :status) AND " +
        "(:dataInicio IS NULL OR s.dataAgendamento >= :dataInicio) AND " +
        "(:dataFim IS NULL OR s.dataAgendamento <= :dataFim)")
    Page<Servico> findWithFilters(@Param("prestador") Prestador prestador,
                                @Param("clienteNome") String clienteNome,
                                @Param("status") Servico.StatusServico status,
                                @Param("dataInicio") LocalDateTime dataInicio,
                                @Param("dataFim") LocalDateTime dataFim,
                                Pageable pageable);
}