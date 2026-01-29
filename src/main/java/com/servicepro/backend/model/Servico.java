package com.servicepro.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "servicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servico {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(min = 3, max = 100, message = "Nome do cliente deve ter entre 3 e 100 caracteres")
    @Column(name = "cliente_nome", nullable = false)
    private String clienteNome;
    
    @Column(name = "cliente_telefone")
    private String clienteTelefone;
    
    @Email(message = "Email do cliente deve ser válido")
    @Column(name = "cliente_email")
    private String clienteEmail;
    
    @NotBlank(message = "Endereço é obrigatório")
    @Column(nullable = false, length = 500)
    private String endereco;
    
    @NotNull(message = "Data do serviço é obrigatória")
    @Column(name = "data_agendamento", nullable = false)
    private LocalDateTime dataAgendamento;
    
    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;
    
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusServico status = StatusServico.AGENDADO;
    
    @Column(columnDefinition = "TEXT")
    private String observacoes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    public enum StatusServico {
        AGENDADO("Agendado"),
        CONFIRMADO("Confirmado"),
        EM_ANDAMENTO("Em Andamento"),
        CONCLUIDO("Concluído"),
        CANCELADO("Cancelado");
        
        private final String descricao;
        
        StatusServico(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
    
    // Métodos auxiliares
    public void concluir() {
        this.status = StatusServico.CONCLUIDO;
        this.dataConclusao = LocalDateTime.now();
    }
    
    public void cancelar() {
        this.status = StatusServico.CANCELADO;
    }
    
    public void iniciar() {
        this.status = StatusServico.EM_ANDAMENTO;
    }
}