package com.servicepro.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicepro.backend.dto.request.ServicoRequest;
import com.servicepro.backend.dto.response.ServicoResponse;
import com.servicepro.backend.model.Prestador;
import com.servicepro.backend.model.Servico;
import com.servicepro.backend.repository.PrestadorRepository;
import com.servicepro.backend.repository.ServicoRepository;
import com.servicepro.backend.util.DateUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicoService {
    
    private final ServicoRepository servicoRepository;
    private final PrestadorRepository prestadorRepository;
    
    /**
     * Cria um novo serviço
     */
    @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        // Buscar prestador
        Prestador prestador = prestadorRepository.findById(request.getPrestadorId())
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado com ID: " + request.getPrestadorId()));
        
        // Validar data (deve ser futura)
        if (request.getDataAgendamento().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Data do serviço deve ser futura");
        }
        
        // Criar serviço
        Servico servico = new Servico();
        servico.setClienteNome(request.getClienteNome());
        servico.setClienteTelefone(request.getClienteTelefone());
        servico.setClienteEmail(request.getClienteEmail());
        servico.setEndereco(request.getEndereco());
        servico.setDataAgendamento(request.getDataAgendamento());
        servico.setValor(request.getValor());
        servico.setObservacoes(request.getObservacoes());
        servico.setPrestador(prestador);
        servico.setLatitude(request.getLatitude());
        servico.setLongitude(request.getLongitude());
        
        // Salvar
        Servico servicoSalvo = servicoRepository.save(servico);
        
        return toResponse(servicoSalvo);
    }
    
    /**
     * Busca serviço por ID
     */
    public ServicoResponse buscarPorId(Long id) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado com ID: " + id));
        
        return toResponse(servico);
    }
    
    /**
     * Lista serviços por prestador com paginação
     */
    public Page<ServicoResponse> listarPorPrestador(Long prestadorId, Pageable pageable) {
        Prestador prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado com ID: " + prestadorId));
        
        return servicoRepository.findByPrestador(prestador, pageable)
                .map(this::toResponse);
    }
    
    /**
     * Lista serviços por prestador e status
     */
    public List<ServicoResponse> listarPorPrestadorEStatus(Long prestadorId, Servico.StatusServico status) {
        Prestador prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado com ID: " + prestadorId));
        
        return servicoRepository.findByPrestadorAndStatus(prestador, status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getDashboard(Long prestadorId) {
    // Método simplificado
    Prestador prestador = prestadorRepository.findById(prestadorId)
            .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));
    
    Long totalServicos = servicoRepository.countByPrestador(prestador);
    BigDecimal faturamentoTotal = servicoRepository.calcularFaturamentoTotal(prestador);
    
    return Map.of(
        "totalServicos", totalServicos,
        "faturamentoTotal", faturamentoTotal,
        "prestadorId", prestadorId,
        "prestadorNome", prestador.getNomeCompleto()
    );
}
    
    /**
     * Atualiza dados do serviço
     */
    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado com ID: " + id));
        
        // Validar data (deve ser futura)
        if (request.getDataAgendamento().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Data do serviço deve ser futura");
        }
        
        // Atualizar campos
        servico.setClienteNome(request.getClienteNome());
        servico.setClienteTelefone(request.getClienteTelefone());
        servico.setClienteEmail(request.getClienteEmail());
        servico.setEndereco(request.getEndereco());
        servico.setDataAgendamento(request.getDataAgendamento());
        servico.setValor(request.getValor());
        servico.setObservacoes(request.getObservacoes());
        servico.setLatitude(request.getLatitude());
        servico.setLongitude(request.getLongitude());
        
        Servico servicoAtualizado = servicoRepository.save(servico);
        
        return toResponse(servicoAtualizado);
    }
    
    /**
     * Atualiza status do serviço
     */
    @Transactional
    public ServicoResponse atualizarStatus(Long id, String statusStr) {
        Servico servico = servicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado com ID: " + id));
        
        try {
            Servico.StatusServico novoStatus = Servico.StatusServico.valueOf(statusStr.toUpperCase());
            
            // Validar transição de status
            validarTransicaoStatus(servico.getStatus(), novoStatus);
            
            servico.setStatus(novoStatus);
            
            // Se concluído, setar data de conclusão
            if (novoStatus == Servico.StatusServico.CONCLUIDO) {
                servico.setDataConclusao(LocalDateTime.now());
            }
            
            Servico servicoAtualizado = servicoRepository.save(servico);
            
            return toResponse(servicoAtualizado);
            
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Status inválido. Use: AGENDADO, CONFIRMADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO");
        }
    }
    
    /**
     * Valida transição de status
     */
    private void validarTransicaoStatus(Servico.StatusServico statusAtual, Servico.StatusServico novoStatus) {
        // Regras de transição
        if (statusAtual == Servico.StatusServico.CANCELADO) {
            throw new RuntimeException("Serviço cancelado não pode ter status alterado");
        }
        
        if (statusAtual == Servico.StatusServico.CONCLUIDO && novoStatus != Servico.StatusServico.CONCLUIDO) {
            throw new RuntimeException("Serviço concluído não pode ter status alterado");
        }
    }
    
    /**
     * Exclui serviço
     */
    @Transactional
    public void excluir(Long id) {
        if (!servicoRepository.existsById(id)) {
            throw new RuntimeException("Serviço não encontrado com ID: " + id);
        }
        
        servicoRepository.deleteById(id);
    }
    
    /**
     * Busca serviços com filtros avançados
     */
    public Page<ServicoResponse> buscarComFiltros(Long prestadorId, String clienteNome, 
                                                Servico.StatusServico status, 
                                                LocalDateTime dataInicio, LocalDateTime dataFim,
                                                Pageable pageable) {
        Prestador prestador = prestadorRepository.findById(prestadorId)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado com ID: " + prestadorId));
        
        return servicoRepository.findWithFilters(prestador, clienteNome, status, dataInicio, dataFim, pageable)
                .map(this::toResponse);
    }
    
    /**
     * Converte entidade para DTO
     */
    private ServicoResponse toResponse(Servico servico) {
        String valorFormatado = String.format("R$ %.2f", servico.getValor());
        String dataFormatada = DateUtils.formatDateTime(servico.getDataAgendamento());
        
        return new ServicoResponse(
            servico.getId(),
            servico.getClienteNome(),
            servico.getClienteTelefone(),
            servico.getClienteEmail(),
            servico.getEndereco(),
            servico.getDataAgendamento(),
            servico.getDataConclusao(),
            servico.getValor(),
            servico.getStatus().name(),
            servico.getStatus().getDescricao(),
            servico.getObservacoes(),
            servico.getPrestador().getId(),
            servico.getPrestador().getNomeCompleto(),
            servico.getDataCriacao(),
            servico.getLatitude(),
            servico.getLongitude(),
            dataFormatada,
            valorFormatado
        );
    }
}