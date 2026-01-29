package com.servicepro.backend.service;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String remetente;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email.simulate:true}")  // ← Pega do properties, padrão true
    private boolean simulateEmail;
    
    /**
     * Envia email de recuperação de senha
     */
    @Async
    public void enviarEmailRecuperacaoSenha(String destinatario, String nome, String token) {

               // 🎪 MODO SIMULAÇÃO - NÃO ENVIA EMAIL REAL
        if (simulateEmail) {
            log.info("=".repeat(70));
            log.info("📧 [MODO SIMULAÇÃO] RECUPERAÇÃO DE SENHA");
            log.info("📧 Para: {}", destinatario);
            log.info("📧 Nome: {}", nome);
            log.info("📧 Token: {}", token);
            log.info("📧 Link reset: http://localhost:3000/resetar-senha?token={}", token);
            log.info("📧 Para testar: POST /api/recuperacao-senha/resetar");
            log.info("📧 Body JSON: {\"token\": \"{}\", \"novaSenha\": \"sua_nova_senha\"}", token);
            log.info("=".repeat(70));
            return;  // ⚠️ PARA AQUI, NÃO TENTA ENVIAR EMAIL REAL
        }
    }
    
    /**
     * Envia email de confirmação de serviço
     */
    @Async
    public void enviarEmailConfirmacaoServico(String destinatario, String nomeCliente, 
                                            String dataServico, String endereco, 
                                            BigDecimal valor, String prestadorNome) {
        String assunto = "Confirmação de Serviço - ServicePro";
        
        Context context = new Context(new Locale("pt", "BR"));
        context.setVariable("nomeCliente", nomeCliente);
        context.setVariable("prestadorNome", prestadorNome);
        context.setVariable("dataServico", dataServico);
        context.setVariable("endereco", endereco);
        context.setVariable("valor", String.format("R$ %.2f", valor));
        
        String conteudo = templateEngine.process("email/confirmacao-servico", context);
        
        enviarEmail(destinatario, assunto, conteudo, true);
    }
    
    /**
     * Envia email de boas-vindas
     */
    @Async
    public void enviarEmailBoasVindas(String destinatario, String nome) {
        String assunto = "Bem-vindo ao ServicePro!";
        
        Context context = new Context(new Locale("pt", "BR"));
        context.setVariable("nome", nome);
        context.setVariable("appUrl", frontendUrl);
        
        String conteudo = templateEngine.process("email/boas-vindas", context);
        
        enviarEmail(destinatario, assunto, conteudo, true);
    }
    
    /**
     * Envia email genérico
     */
    private void enviarEmail(String destinatario, String assunto, String conteudo, boolean isHtml) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            
            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(conteudo, isHtml);
            
            mailSender.send(mimeMessage);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao enviar email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica se email é válido (simulação)
     */
    public boolean isEmailValido(String email) {
        // Implementação básica - na prática usar validação de DNS
        return email != null && email.contains("@") && email.contains(".");
    }
}
