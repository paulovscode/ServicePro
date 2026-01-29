package com.servicepro.backend.service;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.servicepro.backend.dto.request.LoginRequest;
import com.servicepro.backend.dto.response.AuthResponse;
import com.servicepro.backend.model.Prestador;
import com.servicepro.backend.repository.PrestadorRepository;
import com.servicepro.backend.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final PrestadorRepository prestadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Registra um novo prestador
     */
    public AuthResponse registrar(Prestador prestador) {
        // Verificar se email já existe
        if (prestadorRepository.existsByEmail(prestador.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }
        
        // Verificar se telefone já existe
        if (prestadorRepository.existsByTelefone(prestador.getTelefone())) {
            throw new RuntimeException("Telefone já cadastrado");
        }
        
        // Verificar se CPF/CNPJ já existe
        if (prestadorRepository.existsByCpfCnpj(prestador.getCpfCnpj())) {
            throw new RuntimeException("CPF/CNPJ já cadastrado");
        }
        
        // Codificar senha
        prestador.setSenha(passwordEncoder.encode(prestador.getSenha()));
        
        // Salvar prestador
        Prestador prestadorSalvo = prestadorRepository.save(prestador);
        
        // Gerar token JWT
        String jwtToken = jwtService.generateToken(prestadorSalvo);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .prestadorId(prestadorSalvo.getId())
                .nomeCompleto(prestadorSalvo.getNomeCompleto())
                .email(prestadorSalvo.getEmail())
                .telefone(prestadorSalvo.getTelefone())
                .expiraEm(jwtService.getExpirationTime())
                .build();
    }
    
    /**
     * Autentica um prestador
     */
    public AuthResponse autenticar(LoginRequest request) {
        // Buscar prestador por email ou telefone
        Optional<Prestador> prestadorOpt = prestadorRepository
                .findByEmailOrTelefone(request.getEmailOuTelefone()); // ← CORRIGIDO
        
        if (prestadorOpt.isEmpty()) {
            throw new RuntimeException("Credenciais inválidas");
        }
        
        Prestador prestador = prestadorOpt.get();
        
        // Verificar se está ativo
        if (!prestador.getAtivo()) {
            throw new RuntimeException("Prestador desativado");
        }
        
        // Autenticar com Spring Security
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmailOuTelefone(),
                request.getSenha()
            )
        );
        
        // Gerar token JWT
        String jwtToken = jwtService.generateToken(prestador);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .prestadorId(prestador.getId())
                .nomeCompleto(prestador.getNomeCompleto())
                .email(prestador.getEmail())
                .telefone(prestador.getTelefone())
                .expiraEm(jwtService.getExpirationTime())
                .build();
    }
    
    /**
     * Valida token JWT
     */
    public boolean validarToken(String token) {
        return jwtService.isTokenValid(token);
    }
    
    /**
     * Atualiza token JWT
     */
    public AuthResponse atualizarToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Token inválido");
        }
        
        String email = jwtService.extractUsername(refreshToken);
        Prestador prestador = prestadorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));
        
        String novoToken = jwtService.generateToken(prestador);
        
        return AuthResponse.builder()
                .token(novoToken)
                .prestadorId(prestador.getId())
                .nomeCompleto(prestador.getNomeCompleto())
                .email(prestador.getEmail())
                .telefone(prestador.getTelefone())
                .expiraEm(jwtService.getExpirationTime())
                .build();
    }
}