package com.servicepro.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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

import com.servicepro.backend.dto.request.PrestadorRequest;
import com.servicepro.backend.dto.response.ApiResponse;
import com.servicepro.backend.dto.response.PrestadorResponse;
import com.servicepro.backend.service.AuthService;
import com.servicepro.backend.service.PrestadorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prestadores")
@RequiredArgsConstructor
public class PrestadorController {
    
    private final PrestadorService prestadorService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Cria um novo prestador
     * POST /api/prestadores
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PrestadorResponse>> criar(@Valid @RequestBody PrestadorRequest request) {
        try {
            PrestadorResponse response = prestadorService.criar(request);
            return ResponseEntity.ok(
                ApiResponse.success(response, "Prestador cadastrado com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Busca prestador por ID
     * GET /api/prestadores/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrestadorResponse>> buscarPorId(@PathVariable Long id) {
        try {
            PrestadorResponse response = prestadorService.buscarPorId(id);
            return ResponseEntity.ok(
                ApiResponse.success(response, "Prestador encontrado")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Lista todos os prestadores com paginação
     * GET /api/prestadores?page=0&size=10&sort=nome,asc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PrestadorResponse>>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nomeCompleto,asc") String sort) {
        
        try {
            // Configurar paginação
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            
            // Em uma versão completa, teríamos um método paginado no service
            // Por enquanto, vamos adaptar o método existente
            List<PrestadorResponse> todos = prestadorService.listarTodos();
            
            // Conversão manual para Page (simplificado)
            // Em produção, implementar repository com paginação
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), todos.size());
            
            Page<PrestadorResponse> pageResponse = new org.springframework.data.domain.PageImpl<>(
                todos.subList(start, end), pageable, todos.size()
            );
            
            return ResponseEntity.ok(
                ApiResponse.success(pageResponse, "Prestadores listados com sucesso")
            );
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro ao listar prestadores", 500)
            );
        }
    }
    
    /**
     * Atualiza dados do prestador
     * PUT /api/prestadores/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PrestadorResponse>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PrestadorRequest request) {
        
        try {
            PrestadorResponse response = prestadorService.atualizar(id, request);
            return ResponseEntity.ok(
                ApiResponse.success(response, "Prestador atualizado com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Altera senha do prestador
     * PATCH /api/prestadores/{id}/senha
     */
    @PatchMapping("/{id}/senha")
    public ResponseEntity<ApiResponse<Void>> alterarSenha(
            @PathVariable Long id,
            @RequestParam String senhaAtual,
            @RequestParam String novaSenha) {
        
        try {
            prestadorService.alterarSenha(id, senhaAtual, novaSenha);
            return ResponseEntity.ok(
                ApiResponse.success("Senha alterada com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Desativa um prestador (exclusão lógica)
     * DELETE /api/prestadores/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> desativar(@PathVariable Long id) {
        try {
            prestadorService.desativar(id);
            return ResponseEntity.ok(
                ApiResponse.success("Prestador desativado com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Ativa um prestador
     * PATCH /api/prestadores/{id}/ativar
     */
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<ApiResponse<Void>> ativar(@PathVariable Long id) {
        try {
            prestadorService.ativar(id);
            return ResponseEntity.ok(
                ApiResponse.success("Prestador ativado com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Login do prestador (alternativo ao AuthController)
     * POST /api/prestadores/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<PrestadorResponse>> login(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String telefone,
            @RequestParam String senha) {
        
        try {
            // Validar entrada
            if ((email == null || email.isEmpty()) && (telefone == null || telefone.isEmpty())) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Informe email ou telefone", 400)
                );
            }
            
            String emailOuTelefone = email != null && !email.isEmpty() ? email : telefone;
            
            var prestadorOpt = prestadorService.buscarPorEmailOuTelefone(emailOuTelefone, senha);
            
            if (prestadorOpt.isEmpty()) {
                return ResponseEntity.status(401).body(
                    ApiResponse.error("Credenciais inválidas", 401)
                );
            }
            
            return ResponseEntity.ok(
                ApiResponse.success(prestadorOpt.get(), "Login realizado com sucesso")
            );
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Busca prestadores por nome (autocomplete)
     * GET /api/prestadores/buscar?nome=joao
     */
    @GetMapping("/buscar")
    public ResponseEntity<ApiResponse<List<PrestadorResponse>>> buscarPorNome(
            @RequestParam String nome) {
        
        try {
            // Em uma versão completa, teríamos este método no service
            // Por enquanto, filtramos manualmente
            List<PrestadorResponse> todos = prestadorService.listarTodos();
            List<PrestadorResponse> filtrados = todos.stream()
                .filter(p -> p.getNomeCompleto().toLowerCase().contains(nome.toLowerCase()))
                .toList();
            
            return ResponseEntity.ok(
                ApiResponse.success(filtrados, "Prestadores encontrados")
            );
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro na busca", 500)
            );
        }
    }
    
    /**
     * Estatísticas dos prestadores
     * GET /api/prestadores/estatisticas
     */
    @GetMapping("/estatisticas")
    public ResponseEntity<ApiResponse<Map<String, Object>>> estatisticas() {
        try {
            List<PrestadorResponse> todos = prestadorService.listarTodos();
            
            long total = todos.size();
            long ativos = todos.stream().filter(PrestadorResponse::getAtivo).count();
            long inativos = total - ativos;
            
            Map<String, Object> estatisticas = Map.of(
                "totalPrestadores", total,
                "prestadoresAtivos", ativos,
                "prestadoresInativos", inativos,
                "taxaAtivos", total > 0 ? (ativos * 100.0 / total) : 0
            );
            
            return ResponseEntity.ok(
                ApiResponse.success(estatisticas, "Estatísticas geradas")
            );
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro ao gerar estatísticas", 500)
            );
        }
    }
    
    /**
     * Verifica disponibilidade de email
     * GET /api/prestadores/verificar-email/{email}
     */
    @GetMapping("/verificar-email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> verificarEmail(@PathVariable String email) {
        try {
            // Em produção, verificar no banco de dados
            // Por enquanto, simulamos que está disponível
            boolean disponivel = true;
            
            return ResponseEntity.ok(
                ApiResponse.success(disponivel, "Verificação concluída")
            );
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro na verificação", 500)
            );
        }
    }
    
    /**
     * Teste de saúde do controller
     * GET /api/prestadores/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "controller", "PrestadorController",
            "timestamp", java.time.LocalDateTime.now(),
            "endpoints", List.of(
                "POST /api/prestadores",
                "GET /api/prestadores/{id}",
                "GET /api/prestadores",
                "PUT /api/prestadores/{id}",
                "DELETE /api/prestadores/{id}",
                "POST /api/prestadores/login"
            )
        );
        
        return ResponseEntity.ok(
            ApiResponse.success(health, "Controller operacional")
        );
    }
}