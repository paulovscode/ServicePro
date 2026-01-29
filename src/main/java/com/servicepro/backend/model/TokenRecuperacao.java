package com.servicepro.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_recuperacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRecuperacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;
    
    @Column(name = "data_expiracao", nullable = false)
    private LocalDateTime dataExpiracao;
    
    @Column(name = "utilizado")
    private Boolean utilizado = false;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
    
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(dataExpiracao);
    }
    
    public boolean isValido() {
        return !utilizado && !isExpirado();
    }
}