package com.servicepro.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicepro.backend.dto.request.PrestadorRequest;
import com.servicepro.backend.dto.response.PrestadorResponse;
import com.servicepro.backend.model.Prestador;
import com.servicepro.backend.repository.PrestadorRepository;
import com.servicepro.backend.util.Validators;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrestadorService {
    
    private final PrestadorRepository prestadorRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Cria um novo prestador
     */
    @Transactional
    public PrestadorResponse criar(PrestadorRequest request) {
        // Validar CPF/CNPJ
        if (!Validators.isValidCPFouCNPJ(request.getCpfCnpj())) {
            throw new RuntimeException("CPF/CNPJ inválido");
        }
        
        // Validar telefone
        if (!Validators.isValidTelefone(request.getTelefone())) {
            throw new RuntimeException("Telefone inválido");
        }
        
        // Verificar unicidade
        if (prestadorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + request.getEmail());
        }
        
        if (prestadorRepository.existsByTelefone(request.getTelefone())) {
            throw new RuntimeException("Telefone já cadastrado: " + request.getTelefone());
        }
        
        if (prestadorRepository.existsByCpfCnpj(request.getCpfCnpj())) {
            throw new RuntimeException("CPF/CNPJ já cadastrado: " + request.getCpfCnpj());
        }
        
        // Criar entidade
        Prestador prestador = new Prestador();
        prestador.setNomeCompleto(request.getNomeCompleto());
        prestador.setCpfCnpj(request.getCpfCnpj());
        prestador.setEmail(request.getEmail());
        prestador.setTelefone(request.getTelefone());
        prestador.setSenha(passwordEncoder.encode(request.getSenha()));
        
        // Salvar
        Prestador prestadorSalvo = prestadorRepository.save(prestador);
        
        return toResponse(prestadorSalvo);
    }
    
    /**
     * Busca prestador por ID
     */
    public PrestadorResponse buscarPorId(Long id) {
        Prestador prestador = prestadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado com ID: " + id));
        
        return toResponse(prestador);
    }
    
    /**
     * Lista todos os prestadores ativos
     */
    public List<PrestadorResponse> listarTodos() {
        return prestadorRepository.findByAtivoTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Atualiza dados do prestador
     */
    @Transactional
    public PrestadorResponse atualizar(Long id, PrestadorRequest request) {
        Prestador prestador = prestadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado com ID: " + id));
        
        // Validar telefone
        if (!Validators.isValidTelefone(request.getTelefone())) {
            throw new RuntimeException("Telefone inválido");
        }
        
        // Verificar se telefone já pertence a outro prestador
        if (!request.getTelefone().equals(prestador.getTelefone()) && 
            prestadorRepository.existsByTelefone(request.getTelefone())) {
            throw new RuntimeException("Telefone já cadastrado: " + request.getTelefone());
        }
        
        // Atualizar campos permitidos
        prestador.setNomeCompleto(request.getNomeCompleto());
        prestador.setTelefone(request.getTelefone());
        prestador.setDataAtualizacao(LocalDateTime.now());
        
        Prestador prestadorAtualizado = prestadorRepository.save(prestador);
        
        return toResponse(prestadorAtualizado);
    }
    
    /**
     * Altera senha do prestador
     */
    @Transactional
    public void alterarSenha(Long id, String senhaAtual, String novaSenha) {
        Prestador prestador = prestadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado com ID: " + id));
        
        // Verificar senha atual
        if (!passwordEncoder.matches(senhaAtual, prestador.getSenha())) {
            throw new RuntimeException("Senha atual incorreta");
        }
        
        // Atualizar senha
        prestador.setSenha(passwordEncoder.encode(novaSenha));
        prestador.setDataAtualizacao(LocalDateTime.now());
        
        prestadorRepository.save(prestador);
    }

    /**
 * Reseta senha (para recuperação - não precisa da senha atual)
 */
@Transactional
public void resetarSenha(Long id, String novaSenha) {
    System.out.println("🔧 [DEBUG] resetarSenha chamado");
    System.out.println("🔧 ID do prestador: " + id);
    System.out.println("🔧 Nova senha (texto): " + novaSenha);
    
    Prestador prestador = prestadorRepository.findById(id)
            .orElseThrow(() -> {
                System.out.println("❌ Prestador não encontrado com ID: " + id);
                return new RuntimeException("Prestador não encontrado com ID: " + id);
            });
    
    System.out.println("✅ Prestador encontrado: " + prestador.getEmail());
    System.out.println("🔧 Senha ANTES: " + prestador.getSenha());
    
    // Gerar hash da nova senha
    String hash = passwordEncoder.encode(novaSenha);
    System.out.println("🔧 Hash gerado: " + hash);
    
    // Atualizar senha
    prestador.setSenha(hash);
    prestador.setDataAtualizacao(LocalDateTime.now());
    
    // Salvar
    prestadorRepository.save(prestador);
    
    // FORÇAR o save imediato
    prestadorRepository.flush();
    
    System.out.println("✅ Prestador salvo");
    
    // VERIFICAR se realmente atualizou
    Prestador verificado = prestadorRepository.findById(id).orElseThrow();
    System.out.println("🔧 Senha DEPOIS no banco: " + verificado.getSenha());
    
    // Testar se a senha funciona
    boolean senhaCorreta = passwordEncoder.matches(novaSenha, verificado.getSenha());
    System.out.println("🔧 Teste de senha: " + (senhaCorreta ? "✅ CORRETO" : "❌ ERRADO"));
    
    if (!senhaCorreta) {
        throw new RuntimeException("ERRO CRÍTICO: Senha não foi atualizada corretamente no banco!");
    }
    
    System.out.println("✅ resetarSenha concluído com SUCESSO!");
}
    
    /**
     * Desativa um prestador (exclusão lógica)
     */
    @Transactional
    public void desativar(Long id) {
        Prestador prestador = prestadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado com ID: " + id));
        
        prestador.setAtivo(false);
        prestador.setDataAtualizacao(LocalDateTime.now());
        
        prestadorRepository.save(prestador);
    }
    
    /**
     * Ativa um prestador
     */
    @Transactional
    public void ativar(Long id) {
        Prestador prestador = prestadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado com ID: " + id));
        
        prestador.setAtivo(true);
        prestador.setDataAtualizacao(LocalDateTime.now());
        
        prestadorRepository.save(prestador);
    }
    
    /**
     * Busca prestador por email ou telefone (para login sem JWT)
     */
    public Optional<PrestadorResponse> buscarPorEmailOuTelefone(String emailOuTelefone, String senha) {
        Optional<Prestador> prestadorOpt = prestadorRepository
                .findByEmailOrTelefone(emailOuTelefone, emailOuTelefone);
        
        if (prestadorOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Prestador prestador = prestadorOpt.get();
        
        if (!prestador.getAtivo()) {
            throw new RuntimeException("Prestador desativado");
        }
        
        if (!passwordEncoder.matches(senha, prestador.getSenha())) {
            return Optional.empty();
        }
        
        return Optional.of(toResponse(prestador));
    }
    
    /**
     * Converte entidade para DTO
     */
    private PrestadorResponse toResponse(Prestador prestador) {
        return new PrestadorResponse(
            prestador.getId(),
            prestador.getNomeCompleto(),
            prestador.getCpfCnpj(),
            prestador.getEmail(),
            prestador.getTelefone(),
            prestador.getFotoPerfil(),
            prestador.getDataCriacao(),
            prestador.getAtivo(),
            prestador.getEmailVerificado()
        );
    }
}