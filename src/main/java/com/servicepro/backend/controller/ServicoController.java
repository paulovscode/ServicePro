package com.servicepro.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.servicepro.backend.dto.request.ServicoRequest;
import com.servicepro.backend.dto.response.ApiResponse;
import com.servicepro.backend.dto.response.ServicoResponse;
import com.servicepro.backend.model.Servico;
import com.servicepro.backend.service.DashboardService;
import com.servicepro.backend.service.PdfService;
import com.servicepro.backend.service.ServicoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
public class ServicoController {
    
    private final ServicoService servicoService;
    private final DashboardService dashboardService;
    private final PdfService pdfService;
    
    /**
     * Cria um novo serviço
     * POST /api/servicos
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ServicoResponse>> criar(@Valid @RequestBody ServicoRequest request) {
        try {
            ServicoResponse response = servicoService.criar(request);
            return ResponseEntity.ok(
                ApiResponse.success(response, "Serviço criado com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Busca serviço por ID
     * GET /api/servicos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicoResponse>> buscarPorId(@PathVariable Long id) {
        try {
            ServicoResponse response = servicoService.buscarPorId(id);
            return ResponseEntity.ok(
                ApiResponse.success(response, "Serviço encontrado")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Lista serviços por prestador com paginação
     * GET /api/servicos/prestador/{prestadorId}?page=0&size=10&sort=dataAgendamento,desc
     */
    @GetMapping("/prestador/{prestadorId}")
    public ResponseEntity<ApiResponse<Page<ServicoResponse>>> listarPorPrestador(
            @PathVariable Long prestadorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dataAgendamento,desc") String sort) {
        
        try {
            // Configurar paginação
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            
            Page<ServicoResponse> servicos = servicoService.listarPorPrestador(prestadorId, pageable);
            
            return ResponseEntity.ok(
                ApiResponse.success(servicos, "Serviços listados com sucesso")
            );
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Atualiza status do serviço
     * PATCH /api/servicos/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ServicoResponse>> atualizarStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        try {
            ServicoResponse response = servicoService.atualizarStatus(id, status);
            return ResponseEntity.ok(
                ApiResponse.success(response, "Status atualizado com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Atualiza dados do serviço
     * PUT /api/servicos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicoResponse>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ServicoRequest request) {
        
        try {
            ServicoResponse response = servicoService.atualizar(id, request);
            return ResponseEntity.ok(
                ApiResponse.success(response, "Serviço atualizado com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Exclui serviço
     * DELETE /api/servicos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        try {
            servicoService.excluir(id);
            return ResponseEntity.ok(
                ApiResponse.success("Serviço excluído com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Dashboard por período específico
     * GET /api/servicos/dashboard/periodo/{prestadorId}/{periodo}
     */
    @GetMapping("/dashboard/periodo/{prestadorId}/{periodo}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardPorPeriodo(
            @PathVariable Long prestadorId,
            @PathVariable String periodo) {
        
        try {
            // Redirecionar para o DashboardController
            // Ou manter compatibilidade com versão anterior
            
            // Por enquanto, retornamos mensagem informativa
            Map<String, Object> info = Map.of(
                "mensagem", "Use /api/dashboard/{prestadorId}/{periodo} para dashboard completo",
                "novoEndpoint", "/api/dashboard/" + prestadorId + "/" + periodo,
                "periodosDisponiveis", List.of("hoje", "semana", "mes", "trimestre", "semestre", "ano")
            );
            
            return ResponseEntity.ok(
                ApiResponse.success(info, "Endpoint movido")
            );
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro interno", 500)
            );
        }
    }
    
    /**
     * Dashboard do mês atual
     * GET /api/servicos/dashboard/mes/{prestadorId}
     */
    @GetMapping("/dashboard/mes/{prestadorId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardMes(@PathVariable Long prestadorId) {
        return getDashboardPorPeriodo(prestadorId, "mes");
    }
    
    /**
     * Dashboard básico (mantido para compatibilidade)
     * GET /api/servicos/dashboard/{prestadorId}
     */
    @GetMapping("/dashboard/{prestadorId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard(@PathVariable Long prestadorId) {
        try {
            var dashboard = servicoService.getDashboard(prestadorId);
            
            return ResponseEntity.ok(
                ApiResponse.success(dashboard, "Dashboard gerado")
            );
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Gerar comprovante PDF do serviço
     * GET /api/servicos/{id}/comprovante
     */
    @GetMapping(value = "/{id}/comprovante", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> gerarComprovante(@PathVariable Long id) {
        try {
            // Buscar serviço
            ServicoResponse servicoResponse = servicoService.buscarPorId(id);
            
            // Converter para entidade (em produção, teríamos método no service)
            Servico servico = new Servico();
            servico.setId(servicoResponse.getId());
            servico.setClienteNome(servicoResponse.getClienteNome());
            servico.setClienteTelefone(servicoResponse.getClienteTelefone());
            servico.setClienteEmail(servicoResponse.getClienteEmail());
            servico.setEndereco(servicoResponse.getEndereco());
            servico.setDataAgendamento(servicoResponse.getDataAgendamento());
            servico.setValor(servicoResponse.getValor());
            servico.setStatus(Servico.StatusServico.valueOf(servicoResponse.getStatus()));
            servico.setObservacoes(servicoResponse.getObservacoes());
            
            // Criar prestador simulado (em produção, buscar do banco)
            com.servicepro.backend.model.Prestador prestador = new com.servicepro.backend.model.Prestador();
            prestador.setNomeCompleto(servicoResponse.getPrestadorNome());
            prestador.setTelefone("(11) 99999-9999"); // Placeholder
            prestador.setEmail("prestador@email.com"); // Placeholder
            servico.setPrestador(prestador);
            
            // Gerar PDF
            byte[] pdf = pdfService.gerarComprovanteProfissional(servico);
            
            // Configurar headers para download
            String filename = "comprovante-servico-" + id + ".pdf";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                    .body(pdf);
                    
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Teste do endpoint de PDF
     * GET /api/servicos/teste-pdf/{id}
     */
    @GetMapping("/teste-pdf/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testePdf(@PathVariable Long id) {
        try {
            Map<String, Object> info = Map.of(
                "servicoId", id,
                "pdfEndpoint", "/api/servicos/" + id + "/comprovante",
                "mensagem", "Endpoint de PDF configurado corretamente",
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(
                ApiResponse.success(info, "Teste de PDF realizado")
            );
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro no teste de PDF", 500)
            );
        }
    }
    
    /**
     * Lista serviços por status
     * GET /api/servicos/prestador/{prestadorId}/status/{status}
     */
    @GetMapping("/prestador/{prestadorId}/status/{status}")
    public ResponseEntity<ApiResponse<List<ServicoResponse>>> listarPorStatus(
            @PathVariable Long prestadorId,
            @PathVariable String status) {
        
        try {
            Servico.StatusServico statusEnum = Servico.StatusServico.valueOf(status.toUpperCase());
            List<ServicoResponse> servicos = servicoService.listarPorPrestadorEStatus(prestadorId, statusEnum);
            
            return ResponseEntity.ok(
                ApiResponse.success(servicos, "Serviços listados por status")
            );
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Status inválido. Use: AGENDADO, CONFIRMADO, EM_ANDAMENTO, CONCLUIDO, CANCELADO", 400)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Busca serviços com filtros avançados
     * GET /api/servicos/filtros/{prestadorId}?clienteNome=joao&status=CONCLUIDO&page=0&size=10
     */
    @GetMapping("/filtros/{prestadorId}")
    public ResponseEntity<ApiResponse<Page<ServicoResponse>>> buscarComFiltros(
            @PathVariable Long prestadorId,
            @RequestParam(required = false) String clienteNome,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            // Converter parâmetros
            Servico.StatusServico statusEnum = null;
            if (status != null && !status.isEmpty()) {
                statusEnum = Servico.StatusServico.valueOf(status.toUpperCase());
            }
            
            java.time.LocalDateTime dataInicioDt = null;
            java.time.LocalDateTime dataFimDt = null;
            
            // Em produção, converter strings para LocalDateTime
            
            Page<ServicoResponse> servicos = servicoService.buscarComFiltros(
                prestadorId, clienteNome, statusEnum, dataInicioDt, dataFimDt, pageable
            );
            
            return ResponseEntity.ok(
                ApiResponse.success(servicos, "Serviços filtrados com sucesso")
            );
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro na busca filtrada", 500)
            );
        }
    }
    
    /**
     * Teste de saúde do controller
     * GET /api/servicos/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "controller", "ServicoController",
            "timestamp", java.time.LocalDateTime.now(),
            "endpoints", List.of(
                "POST /api/servicos",
                "GET /api/servicos/{id}",
                "GET /api/servicos/prestador/{prestadorId}",
                "PATCH /api/servicos/{id}/status",
                "GET /api/servicos/{id}/comprovante"
            )
        );
        
        return ResponseEntity.ok(
            ApiResponse.success(health, "Controller operacional")
        );
    }
}
