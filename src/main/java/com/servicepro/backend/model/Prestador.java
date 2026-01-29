package com.servicepro.backend.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "prestadores", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "cpf_cnpj"),
    @UniqueConstraint(columnNames = "telefone")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prestador {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Nome completo é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;
    
    @NotBlank(message = "CPF/CNPJ é obrigatório")
    @Size(min = 11, max = 18, message = "CPF/CNPJ deve ter entre 11 e 18 caracteres")
    @Column(name = "cpf_cnpj", nullable = false, unique = true)
    private String cpfCnpj;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$",
            message = "Telefone inválido. Use formato: (11) 99999-9999 ou 11999999999")
    @Column(nullable = false, unique = true)
    private String telefone;
    
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    @Column(nullable = false)
    private String senha;
    
    @Column(name = "foto_perfil")
    private String fotoPerfil;
    
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;
    
    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
    
    @Column(name = "ativo")
    private Boolean ativo = true;
    
    @Column(name = "email_verificado")
    private Boolean emailVerificado = false;
    
    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Servico> servicos;
    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}