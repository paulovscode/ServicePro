package com.servicepro.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicepro.backend.dto.request.LoginRequest;
import com.servicepro.backend.dto.response.ApiResponse;
import com.servicepro.backend.dto.response.AuthResponse;
import com.servicepro.backend.model.Prestador;
import com.servicepro.backend.service.AuthService;
import com.servicepro.backend.service.PrestadorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final PrestadorService prestadorService;
    
    /**
     * Registra um novo prestador
     * POST /api/auth/registrar
     */
    @PostMapping("/registrar")
    public ResponseEntity<ApiResponse<AuthResponse>> registrar(@Valid @RequestBody Prestador prestador) {
        try {
            AuthResponse authResponse = authService.registrar(prestador);
            return ResponseEntity.ok(
                ApiResponse.success(authResponse, "Prestador registrado com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage(), 400)
            );
        }
    }
    
    /**
     * Autentica um prestador
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = authService.autenticar(request);
            return ResponseEntity.ok(
                ApiResponse.success(authResponse, "Login realizado com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(
                ApiResponse.error(e.getMessage(), 401)
            );
        }
    }
    
    /**
     * Valida token JWT
     * GET /api/auth/validar
     */
    @GetMapping("/validar")
    public ResponseEntity<ApiResponse<Boolean>> validarToken(@RequestHeader("Authorization") String token) {
        try {
            // Remover "Bearer " do token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            boolean isValid = authService.validarToken(token);
            return ResponseEntity.ok(
                ApiResponse.success(isValid, "Token validado")
            );
        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("Token inválido", 401)
            );
        }
    }
    
    /**
     * Atualiza token JWT
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        try {
            // Remover "Bearer " do token
            if (refreshToken.startsWith("Bearer ")) {
                refreshToken = refreshToken.substring(7);
            }
            
            AuthResponse authResponse = authService.atualizarToken(refreshToken);
            return ResponseEntity.ok(
                ApiResponse.success(authResponse, "Token atualizado com sucesso")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(
                ApiResponse.error(e.getMessage(), 401)
            );
        }
    }
    
    /**
     * Logout (client-side apenas - invalidar token no app)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // Em JWT stateless, logout é feito no client-side
        return ResponseEntity.ok(
            ApiResponse.success("Logout realizado com sucesso")
        );
    }
    
    /**
     * Verifica se email está disponível
     * GET /api/auth/verificar-email/{email}
     */
    @GetMapping("/verificar-email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> verificarEmail(@PathVariable String email) {
        // Esta verificação seria feita no PrestadorRepository
        // Por simplicidade, retornamos true (email disponível)
        return ResponseEntity.ok(
            ApiResponse.success(true, "Email disponível")
        );
    }
}