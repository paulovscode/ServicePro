package com.servicepro.backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicepro.backend.dto.request.RecuperacaoSenhaRequest;
import com.servicepro.backend.dto.request.ResetSenhaRequest;
import com.servicepro.backend.dto.response.ApiResponse;
import com.servicepro.backend.model.Prestador;
import com.servicepro.backend.model.TokenRecuperacao;
import com.servicepro.backend.repository.PrestadorRepository;
import com.servicepro.backend.repository.TokenRecuperacaoRepository;
import com.servicepro.backend.service.PrestadorService;
import com.servicepro.backend.service.RecuperacaoSenhaService;

import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/recuperacao-senha")
@RequiredArgsConstructor
@Slf4j
public class RecuperacaoSenhaController {
    
    private final RecuperacaoSenhaService recuperacaoSenhaService;
    private final PrestadorService prestadorService;
    private final TokenRecuperacaoRepository tokenRepository;
    private final PrestadorRepository prestadorRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    
    /**
     * Solicita recuperação de senha
     * POST /api/recuperacao-senha/solicitar
     */
    @PostMapping("/solicitar")
    public ResponseEntity<ApiResponse<Void>> solicitarRecuperacao(
            @Valid @RequestBody RecuperacaoSenhaRequest request) {
        
        try {
            log.info("📧 Solicitação de recuperação para email: {}", request.getEmail());
            
            // Por segurança, não revelamos se o email existe ou não
            recuperacaoSenhaService.solicitarRecuperacaoSenha(request.getEmail());
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    "Se o email estiver cadastrado, você receberá instruções para redefinir sua senha."
                )
            );
            
        } catch (Exception e) {
            // Log do erro, mas retornamos mensagem genérica por segurança
            log.error("Erro ao solicitar recuperação para {}: {}", request.getEmail(), e.getMessage());
            
            return ResponseEntity.ok(
                ApiResponse.success(
                    "Se o email estiver cadastrado, você receberá instruções para redefinir sua senha."
                )
            );
        }
    }
    
    /**
     * Valida token de recuperação
     * GET /api/recuperacao-senha/validar/{token}
     */
    @GetMapping("/validar/{token}")
    public ResponseEntity<ApiResponse<Boolean>> validarToken(@PathVariable String token) {
        try {
            log.info("🔍 Validando token: {}", token.substring(0, Math.min(10, token.length())) + "...");
            
            boolean tokenValido = recuperacaoSenhaService.validarToken(token);
            
            return ResponseEntity.ok(
                ApiResponse.success(tokenValido, tokenValido ? "Token válido" : "Token inválido")
            );
            
        } catch (Exception e) {
            log.error("Erro ao validar token: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro ao validar token", 500)
            );
        }
    }
    
    /**
     * Reseta senha usando token (MÉTODO PRINCIPAL - USAR ESTE!)
     * POST /api/recuperacao-senha/resetar
     */
    @PostMapping("/resetar")
public ResponseEntity<ApiResponse<Map<String, Object>>> resetarSenha(
        @Valid @RequestBody Map<String, String> request) {
    
    Map<String, Object> debugInfo = new HashMap<>();
    String token = request.get("token");
    String novaSenha = request.get("novaSenha");
    
    try {
        System.out.println("=".repeat(60));
        System.out.println("🔄 DEBUG INICIADO - RESET SENHA");
        System.out.println("🔧 Token recebido: " + token);
        System.out.println("🔧 Nova senha: " + novaSenha);
        
        // 1. Buscar token
        Optional<TokenRecuperacao> tokenOpt = tokenRepository
                .findByTokenAndUtilizadoFalseAndDataExpiracaoAfter(
                    token, LocalDateTime.now());
        
        if (tokenOpt.isEmpty()) {
            System.out.println("❌ Token não encontrado ou inválido");
            debugInfo.put("erro", "Token inválido");
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Token inválido ou expirado", 400, debugInfo)
            );
        }
        
        TokenRecuperacao tokenRecuperacao = tokenOpt.get();
        System.out.println("✅ Token válido encontrado - ID: " + tokenRecuperacao.getId());
        
        // 2. Buscar prestador
        Prestador prestador = tokenRecuperacao.getPrestador();
        System.out.println("✅ Prestador: " + prestador.getEmail());
        System.out.println("🔧 ID do prestador: " + prestador.getId());
        System.out.println("🔧 Senha atual no objeto: " + prestador.getSenha());
        
        // 3. Gerar NOVO hash
        String novoHash = passwordEncoder.encode(novaSenha);
        System.out.println("🔧 Novo hash gerado: " + novoHash);
        
        // 4. Atualizar usando NATIVE QUERY - VAMOS TESTAR 3 FORMAS
        System.out.println("🔧 Executando UPDATE SQL...");
        
        // FORMA 1: Native query com JdbcTemplate
        String updateSql = "UPDATE prestadores SET senha = ?, data_atualizacao = NOW() WHERE id = ?";
        int linhasAfetadas = jdbcTemplate.update(updateSql, novoHash, prestador.getId());
        
        System.out.println("🔧 Linhas afetadas pelo UPDATE: " + linhasAfetadas);
        
        // FORMA 2: Atualizar objeto e salvar
        prestador.setSenha(novoHash);
        prestadorRepository.save(prestador);
        prestadorRepository.flush();
        System.out.println("🔧 Prestador salvo via repository");
        
        // FORMA 3: UPDATE direto com EntityManager
        entityManager.createNativeQuery(
            "UPDATE prestadores SET senha = :senha WHERE id = :id")
            .setParameter("senha", novoHash)
            .setParameter("id", prestador.getId())
            .executeUpdate();
        System.out.println("🔧 UPDATE via EntityManager executado");
        
        // 5. AGORA VERIFICAR O QUE ESTÁ NO BANCO
        System.out.println("🔍 VERIFICANDO BANCO DE DADOS...");
        
        String sqlVerificacao = "SELECT senha FROM prestadores WHERE id = ?";
        String hashNoBanco = jdbcTemplate.queryForObject(sqlVerificacao, String.class, prestador.getId());
        
        System.out.println("🔍 Hash no banco APÓS update: " + hashNoBanco);
        System.out.println("🔍 Hash gerado: " + novoHash);
        System.out.println("🔍 São iguais? " + hashNoBanco.equals(novoHash));
        
        // 6. Testar se a senha funciona
        boolean senhaFunciona = passwordEncoder.matches(novaSenha, hashNoBanco);
        System.out.println("🔍 Senha '" + novaSenha + "' funciona? " + senhaFunciona);
        
        // 7. Testar com senha ANTIGA (ver se ainda funciona)
        boolean senhaAntigaFunciona = false;
        try {
            // Precisamos saber a senha antiga - vamos verificar
            System.out.println("🔍 Testando login com senha antiga...");
            // Isso é apenas para debug
        } catch (Exception e) {
            // Ignorar
        }
        
        if (senhaFunciona) {
            System.out.println("✅ RESET BEM-SUCEDIDO!");
            // Marcar token como utilizado
            tokenRecuperacao.setUtilizado(true);
            tokenRepository.save(tokenRecuperacao);
            
            debugInfo.put("sucesso", true);
            debugInfo.put("prestador_email", prestador.getEmail());
            debugInfo.put("hash_no_banco", hashNoBanco.substring(0, 30) + "...");
            debugInfo.put("senha_funciona", true);
            
            return ResponseEntity.ok(
                ApiResponse.success(debugInfo, "Senha redefinida com sucesso!")
            );
        } else {
            System.out.println("❌ RESET FALHOU - Hash não corresponde");
            debugInfo.put("erro", "Hash não corresponde");
            debugInfo.put("hash_no_banco", hashNoBanco);
            debugInfo.put("hash_gerado", novoHash);
            
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Falha ao atualizar senha", 400, debugInfo)
            );
        }
        
    } catch (Exception e) {
        System.out.println("🔥 ERRO CRÍTICO: " + e.getMessage());
        e.printStackTrace();
        debugInfo.put("erro_detalhado", e.getMessage());
        return ResponseEntity.badRequest().body(
            ApiResponse.error("Erro: " + e.getMessage(), 400, debugInfo)
        );
    } finally {
        System.out.println("=".repeat(60));
    }
}
    
    /**
     * Método SIMPLES que funciona (alternativa)
     * POST /api/recuperacao-senha/resetar-simples
     */
    @PostMapping("/resetar-simples")
    public ResponseEntity<ApiResponse<Void>> resetarSenhaSimples(
            @Valid @RequestBody ResetSenhaRequest request) {
        
        try {
            log.info("🔄 Reset simples - Token: {}", request.getToken());
            
            // Buscar token
            TokenRecuperacao tokenRecuperacao = tokenRepository
                    .findByTokenAndUtilizadoFalseAndDataExpiracaoAfter(
                        request.getToken(), LocalDateTime.now())
                    .orElseThrow(() -> new RuntimeException("Token inválido"));
            
            Prestador prestador = tokenRecuperacao.getPrestador();
            
            // Atualizar senha
            String hash = passwordEncoder.encode(request.getNovaSenha());
            
            // Native query garantida
            String sql = "UPDATE prestadores SET senha = ? WHERE id = ?";
            jdbcTemplate.update(sql, hash, prestador.getId());
            
            // Marcar token como usado
            tokenRecuperacao.setUtilizado(true);
            tokenRepository.save(tokenRecuperacao);
            
            // Verificar
            String verifySql = "SELECT senha FROM prestadores WHERE id = ?";
            String hashNoBanco = jdbcTemplate.queryForObject(verifySql, String.class, prestador.getId());
            
            if (passwordEncoder.matches(request.getNovaSenha(), hashNoBanco)) {
                log.info("✅ Reset simples BEM-SUCEDIDO para {}", prestador.getEmail());
                return ResponseEntity.ok(
                    ApiResponse.success("Senha redefinida com sucesso!")
                );
            } else {
                throw new RuntimeException("Falha ao verificar senha atualizada");
            }
            
        } catch (Exception e) {
            log.error("❌ Erro no reset simples: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Erro: " + e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Endpoint de emergência: força reset sem token
     * POST /api/recuperacao-senha/resetar-emergencia
     */
    @PostMapping("/resetar-emergencia")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetarEmergencia(
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> resultado = new HashMap<>();
        
        String email = request.get("email");
        String novaSenha = request.get("novaSenha");
        
        if (email == null || novaSenha == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Email e novaSenha são obrigatórios", 400)
            );
        }
        
        try {
            log.info("🚨 RESET DE EMERGÊNCIA para: {}", email);
            
            // Buscar prestador
            Optional<Prestador> prestadorOpt = prestadorRepository.findByEmail(email);
            if (prestadorOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Usuário não encontrado", 400)
                );
            }
            
            Prestador prestador = prestadorOpt.get();
            resultado.put("prestador_id", prestador.getId());
            resultado.put("prestador_email", prestador.getEmail());
            
            // Gerar hash
            String hash = passwordEncoder.encode(novaSenha);
            resultado.put("hash_gerado", hash.substring(0, 30) + "...");
            
            // Atualizar
            String sql = "UPDATE prestadores SET senha = ?, data_atualizacao = NOW() WHERE id = ?";
            int linhas = jdbcTemplate.update(sql, hash, prestador.getId());
            resultado.put("linhas_afetadas", linhas);
            
            // Verificar
            String verifySql = "SELECT senha FROM prestadores WHERE id = ?";
            String hashNoBanco = jdbcTemplate.queryForObject(verifySql, String.class, prestador.getId());
            
            boolean funciona = passwordEncoder.matches(novaSenha, hashNoBanco);
            resultado.put("funciona", funciona);
            
            if (funciona) {
                log.info("✅ RESET DE EMERGÊNCIA BEM-SUCEDIDO para {}", email);
                return ResponseEntity.ok(
                    ApiResponse.success(resultado, "Senha resetada com sucesso!")
                );
            } else {
                log.error("❌ RESET DE EMERGÊNCIA FALHOU para {}", email);
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Reset falhou - hash não corresponde", 400, resultado)
                );
            }
            
        } catch (Exception e) {
            log.error("🔥 ERRO NO RESET DE EMERGÊNCIA: ", e);
            resultado.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Erro: " + e.getMessage(), 400, resultado)
            );
        }
    }

    /**
 * RESET DEFINITIVO - Este método FUNCIONA 100%
 * POST /api/recuperacao-senha/resetar-definitivo
 */
@PostMapping("/resetar-definitivo")
public ResponseEntity<Map<String, Object>> resetarDefinitivo(
        @RequestBody Map<String, String> request) {
    
    Map<String, Object> resultado = new HashMap<>();
    
    try {
        String token = request.get("token");
        String novaSenha = request.get("novaSenha");
        
        System.out.println("🎯 RESET DEFINITIVO - Token: " + token);
        
        if (token == null || novaSenha == null) {
            resultado.put("success", false);
            resultado.put("message", "Token e novaSenha obrigatórios");
            return ResponseEntity.badRequest().body(resultado);
        }
        
        // 1. Buscar token
        var tokenOpt = tokenRepository.findByTokenAndUtilizadoFalseAndDataExpiracaoAfter(
            token, LocalDateTime.now());
        
        if (tokenOpt.isEmpty()) {
            resultado.put("success", false);
            resultado.put("message", "Token inválido");
            return ResponseEntity.badRequest().body(resultado);
        }
        
        TokenRecuperacao tokenRecuperacao = tokenOpt.get();
        Prestador prestador = tokenRecuperacao.getPrestador();
        
        System.out.println("🔧 Prestador: " + prestador.getEmail());
        System.out.println("🔧 ID: " + prestador.getId());
        System.out.println("🔧 Senha atual no BD: " + prestador.getSenha().substring(0, 30) + "...");
        
        // 2. Gerar NOVO hash
        String hash = passwordEncoder.encode(novaSenha);
        System.out.println("🔧 NOVO hash gerado: " + hash);
        
        // 3. ATUALIZAR DIRETO NO BANCO
        String sql = "UPDATE prestadores SET senha = ?, data_atualizacao = NOW() WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql, hash, prestador.getId());
        
        System.out.println("🔧 Linhas atualizadas no BD: " + rowsUpdated);
        
        // 4. VERIFICAR IMEDIATAMENTE
        String verifySql = "SELECT senha FROM prestadores WHERE id = ?";
        String hashNoBanco = jdbcTemplate.queryForObject(verifySql, String.class, prestador.getId());
        
        System.out.println("🔧 Hash agora no BD: " + hashNoBanco.substring(0, 30) + "...");
        
        // 5. Testar se funciona
        boolean funciona = passwordEncoder.matches(novaSenha, hashNoBanco);
        System.out.println("🔧 A senha '" + novaSenha + "' funciona? " + funciona);
        
        // 6. Testar se senha ANTIGA ainda funciona
        boolean senhaAntigaFunciona = passwordEncoder.matches("SenhaAntiga123", hashNoBanco);
        System.out.println("🔧 A senha ANTIGA ainda funciona? " + senhaAntigaFunciona);
        
        if (funciona) {
            // Marcar token como usado
            tokenRecuperacao.setUtilizado(true);
            tokenRepository.save(tokenRecuperacao);
            
            resultado.put("success", true);
            resultado.put("message", "Senha resetada COM SUCESSO!");
            resultado.put("email", prestador.getEmail());
            resultado.put("senha_funciona", true);
            resultado.put("senha_antiga_funciona", senhaAntigaFunciona);
            resultado.put("linhas_atualizadas", rowsUpdated);
            
            System.out.println("✅✅✅ RESET DEFINITIVO - SUCESSO TOTAL!");
            
            return ResponseEntity.ok(resultado);
        } else {
            resultado.put("success", false);
            resultado.put("message", "Falha - hash não corresponde");
            resultado.put("hash_gerado", hash.substring(0, 50) + "...");
            resultado.put("hash_no_banco", hashNoBanco.substring(0, 50) + "...");
            
            System.out.println("❌❌❌ RESET DEFINITIVO - FALHA CRÍTICA!");
            
            return ResponseEntity.badRequest().body(resultado);
        }
        
    } catch (Exception e) {
        System.out.println("🔥🔥🔥 ERRO NO RESET DEFINITIVO: " + e.getMessage());
        e.printStackTrace();
        
        resultado.put("success", false);
        resultado.put("message", "Erro: " + e.getMessage());
        
        return ResponseEntity.badRequest().body(resultado);
    }
}
    
    /**
     * Lista tokens ativos (apenas para debug)
     * GET /api/recuperacao-senha/tokens-ativos
     */
    @GetMapping("/tokens-ativos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> listarTokensAtivos() {
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Buscar tokens não expirados e não utilizados
            var tokens = tokenRepository.findAll().stream()
                    .filter(t -> !t.isExpirado() && !t.getUtilizado())
                    .map(t -> {
                        Map<String, Object> tokenInfo = new HashMap<>();
                        tokenInfo.put("token", t.getToken());
                        tokenInfo.put("prestador_email", t.getPrestador().getEmail());
                        tokenInfo.put("prestador_nome", t.getPrestador().getNomeCompleto());
                        tokenInfo.put("expira_em", t.getDataExpiracao());
                        tokenInfo.put("criado_em", t.getDataCriacao());
                        return tokenInfo;
                    })
                    .toList();
            
            resultado.put("tokens_ativos", tokens);
            resultado.put("quantidade", tokens.size());
            
            return ResponseEntity.ok(
                ApiResponse.success(resultado, "Tokens ativos recuperados")
            );
            
        } catch (Exception e) {
            log.error("Erro ao listar tokens: ", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("Erro ao listar tokens", 500)
            );
        }
    }
}