package com.servicepro.backend.repository;

import com.servicepro.backend.model.Prestador;
import com.servicepro.backend.model.TokenRecuperacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenRecuperacaoRepository extends JpaRepository<TokenRecuperacao, Long> {
    
    Optional<TokenRecuperacao> findByToken(String token);
    
    Optional<TokenRecuperacao> findByTokenAndUtilizadoFalseAndDataExpiracaoAfter(String token, LocalDateTime now);
    
    Optional<TokenRecuperacao> findByPrestadorAndUtilizadoFalseAndDataExpiracaoAfter(Prestador prestador, LocalDateTime now);
    
    @Modifying
    @Query("UPDATE TokenRecuperacao t SET t.utilizado = true WHERE t.prestador = :prestador AND t.utilizado = false")
    void invalidarTokensAnteriores(@Param("prestador") Prestador prestador);
    
    @Modifying
    @Query("DELETE FROM TokenRecuperacao t WHERE t.dataExpiracao < :dataLimite")
    void deletarExpirados(@Param("dataLimite") LocalDateTime dataLimite);
}