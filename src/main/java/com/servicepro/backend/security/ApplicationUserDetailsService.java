package com.servicepro.backend.security;

import com.servicepro.backend.model.Prestador;
import com.servicepro.backend.repository.PrestadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ApplicationUserDetailsService implements UserDetailsService {
    
    private final PrestadorRepository prestadorRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Prestador prestador = prestadorRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Prestador não encontrado: " + username));
        
        if (!prestador.getAtivo()) {
            throw new UsernameNotFoundException("Prestador desativado: " + username);
        }
        
        return new User(
            prestador.getEmail(),
            prestador.getSenha(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_PRESTADOR"))
        );
    }
}
