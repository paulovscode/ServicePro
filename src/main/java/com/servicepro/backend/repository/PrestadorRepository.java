package com.servicepro.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.servicepro.backend.model.Prestador;

@Repository
public interface PrestadorRepository extends JpaRepository<Prestador, Long> {
    
    // Busca por email (para login)
    Optional<Prestador> findByEmail(String email);
    
    // Busca por telefone (para login alternativo)
    Optional<Prestador> findByTelefone(String telefone);
    
    // Busca por email OU telefone
    Optional<Prestador> findByEmailOrTelefone(String email, String telefone);
    
    // Verificações de existência
    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);
    boolean existsByCpfCnpj(String cpfCnpj);
    
    // Busca prestadores ativos
    List<Prestador> findByAtivoTrue();
    
    // Busca por nome (para autocomplete)
    List<Prestador> findByNomeCompletoContainingIgnoreCaseAndAtivoTrue(String nome);
    
    // Contagem de prestadores ativos
    Long countByAtivoTrue();
    
    // Busca com filtros avançados
    @Query("SELECT p FROM Prestador p WHERE " +
        "(:nome IS NULL OR p.nomeCompleto LIKE %:nome%) AND " +
        "(:email IS NULL OR p.email LIKE %:email%) AND " +
        "p.ativo = true")
    List<Prestador> findWithFilters(@Param("nome") String nome,
                                @Param("email") String email);

    @Query("SELECT p FROM Prestador p WHERE p.email = :emailOuTelefone OR p.telefone = :emailOuTelefone")
    Optional<Prestador> findByEmailOrTelefone(@Param("emailOuTelefone") String emailOuTelefone);
}