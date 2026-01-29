package com.servicepro.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ServicoRequest {
    
    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(min = 3, max = 100, message = "Nome do cliente deve ter entre 3 e 100 caracteres")
    private String clienteNome;
    
    private String clienteTelefone;
    
    @Email(message = "Email do cliente deve ser válido")
    private String clienteEmail;
    
    @NotBlank(message = "Endereço é obrigatório")
    private String endereco;
    
    @NotNull(message = "Data do serviço é obrigatória")
    @FutureOrPresent(message = "Data do serviço não pode ser no passado")
    private LocalDateTime dataAgendamento;
    
    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
    private BigDecimal valor;
    
    private String observacoes;
    
    @NotNull(message = "ID do prestador é obrigatório")
    private Long prestadorId;
    
    // ADICIONE ESTES CAMPOS:
    private Double latitude;
    private Double longitude;
    
    // Getters e Setters (ADICIONE OS NOVOS):
    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
    
    public String getClienteTelefone() { return clienteTelefone; }
    public void setClienteTelefone(String clienteTelefone) { this.clienteTelefone = clienteTelefone; }
    
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    
    public LocalDateTime getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(LocalDateTime dataAgendamento) { this.dataAgendamento = dataAgendamento; }
    
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    
    public Long getPrestadorId() { return prestadorId; }
    public void setPrestadorId(Long prestadorId) { this.prestadorId = prestadorId; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}