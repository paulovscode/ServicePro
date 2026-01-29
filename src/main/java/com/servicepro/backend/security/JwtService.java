package com.servicepro.backend.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.servicepro.backend.model.Prestador;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long expirationTime;
    
    /**
     * Extrai username do token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extrai claim específico
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Gera token para UserDetails
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    
    /**
     * Gera token para Prestador (ADICIONE ESTE MÉTODO!)
     */
    public String generateToken(Prestador prestador) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", prestador.getId());
        claims.put("nome", prestador.getNomeCompleto());
        claims.put("email", prestador.getEmail());
        
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(prestador.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Gera token com claims extras
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts
        .builder()
        .claims(extraClaims)  // ← .claims() em vez de .setClaims()
        .subject(userDetails.getUsername())  // ← .subject() em vez de .setSubject()
        .issuedAt(new Date())  // ← .issuedAt() em vez of .setIssuedAt()
        .expiration(new Date(System.currentTimeMillis() + expirationTime))  // ← .expiration()
        .signWith(getSignInKey())  // ← Não precisa mais do SignatureAlgorithm
        .compact();
    }
    
    /**
     * Valida token
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    
    /**
     * Valida token (sem UserDetails)
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica se token está expirado
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Extrai data de expiração
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extrai todos os claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * Obtém chave de assinatura
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Obtém tempo de expiração
     */
    public long getExpirationTime() {
        return expirationTime;
    }
}