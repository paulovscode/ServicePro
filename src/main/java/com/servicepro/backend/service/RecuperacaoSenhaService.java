package com.servicepro.backend.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicepro.backend.model.Prestador;
import com.servicepro.backend.model.TokenRecuperacao;
import com.servicepro.backend.repository.PrestadorRepository;
import com.servicepro.backend.repository.TokenRecuperacaoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecuperacaoSenhaService {
    
    private final PrestadorRepository prestadorRepository;
    private final TokenRecuperacaoRepository tokenRepository;
    private final EmailService emailService;
    private final PrestadorService prestadorService;
    private final PasswordEncoder passwordEncoder;
    
    private static final int TEMPO_EXPIRACAO_HORAS = 24;
    
    /**
     * Solicita recuperação de senha
     */
    @Transactional
    public void solicitarRecuperacaoSenha(String email) {
        // Buscar prestador pelo email
        Optional<Prestador> prestadorOpt = prestadorRepository.findByEmail(email);
        
        // Por segurança, não revelar se email existe ou não
        if (prestadorOpt.isEmpty()) {
            // Log apenas, não informar ao usuário
            System.out.println("Tentativa de recuperação para email não cadastrado: " + email);
            return;
        }
        
        Prestador prestador = prestadorOpt.get();
        
        // Invalidar tokens anteriores
        tokenRepository.invalidarTokensAnteriores(prestador);
        
        // Gerar novo token
        String token = gerarTokenUnico();
        
        // Criar token de recuperação
        TokenRecuperacao tokenRecuperacao = new TokenRecuperacao();
        tokenRecuperacao.setToken(token);
        tokenRecuperacao.setPrestador(prestador);
        tokenRecuperacao.setDataExpiracao(LocalDateTime.now().plusHours(TEMPO_EXPIRACAO_HORAS));
        tokenRecuperacao.setUtilizado(false);
        
        tokenRepository.save(tokenRecuperacao);
        
        // Enviar email (assíncrono)
        emailService.enviarEmailRecuperacaoSenha(
            prestador.getEmail(),
            prestador.getNomeCompleto(),
            token
        );
    }
    
    /**
     * Valida token de recuperação
     */
    public boolean validarToken(String token) {
        Optional<TokenRecuperacao> tokenOpt = tokenRepository
                .findByTokenAndUtilizadoFalseAndDataExpiracaoAfter(token, LocalDateTime.now());
        
        return tokenOpt.isPresent() && tokenOpt.get().isValido();
    }
    
    /**
     * Reseta a senha usando o token
     */

    @Transactional
public void resetarSenha(String token, String novaSenha) {
    log.info("🔄 [1/6] resetarSenha INICIADO - Token: {}", token);
    
    // 1. Buscar token válido
    TokenRecuperacao tokenRecuperacao = tokenRepository
            .findByTokenAndUtilizadoFalseAndDataExpiracaoAfter(token, LocalDateTime.now())
            .orElseThrow(() -> {
                log.error("❌ [2/6] Token não encontrado ou inválido: {}", token);
                return new RuntimeException("Token inválido ou expirado");
            });
    
    log.info("✅ [2/6] Token válido encontrado - ID: {}", tokenRecuperacao.getId());
    
    // 2. Buscar prestador
    Prestador prestador = tokenRecuperacao.getPrestador();
    log.info("✅ [3/6] Prestador encontrado - ID: {}, Email: {}",
            prestador.getId(), prestador.getEmail());
    
    // 3. Gerar hash da nova senha
    String hash = passwordEncoder.encode(novaSenha);
    log.info("🔑 [4/6] Hash gerado para '{}': {}", novaSenha, hash);
    
    // 4. Atualizar senha NO OBJETO
    prestador.setSenha(hash);
    log.info("📝 [5/6] Senha atualizada no objeto Prestador");
    
    // 5. Salvar - MAS PRECISAMOS SALVAR O PRESTADOR, não só o token!
    prestadorRepository.save(prestador);  // ⚠️ ESTÁ FALTANDO ESTA LINHA?
    prestadorRepository.flush();  // Força o save imediato
    log.info("💾 [5/6] Prestador salvo no banco");
    
    // 6. Marcar token como utilizado
    tokenRecuperacao.setUtilizado(true);
    tokenRepository.save(tokenRecuperacao);
    log.info("🏁 [6/6] Token marcado como utilizado - PROCESSO FINALIZADO");
    
    // 7. VERIFICAÇÃO EXTRA
    Prestador verificado = prestadorRepository.findById(prestador.getId()).orElseThrow();
    boolean senhaOk = passwordEncoder.matches(novaSenha, verificado.getSenha());
    log.info("🧪 VERIFICAÇÃO: Senha '{}' funciona? {}", novaSenha, senhaOk ? "✅ SIM" : "❌ NÃO");
    
    if (!senhaOk) {
        log.error("🔥 ERRO CRÍTICO: Senha NÃO foi atualizada no banco!");
        throw new RuntimeException("Falha crítica ao atualizar senha no banco");
    }
}
    
    /**
     * Obtém prestador associado ao token
     */
    public Prestador getPrestadorPorToken(String token) {
        TokenRecuperacao tokenRecuperacao = tokenRepository
                .findByTokenAndUtilizadoFalseAndDataExpiracaoAfter(token, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Token inválido ou expirado"));
        
        return tokenRecuperacao.getPrestador();
    }
    
    /**
     * Gera token único
     */
    private String gerarTokenUnico() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        } while (tokenRepository.findByToken(token).isPresent());
        
        return token;
    }
    
    /**
     * Limpa tokens expirados (executado diariamente)
     */
    @Scheduled(cron = "0 0 2 * * ?") // 2 AM diariamente
    @Transactional
    public void limparTokensExpirados() {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(7);
        tokenRepository.deletarExpirados(dataLimite);
    }
}
