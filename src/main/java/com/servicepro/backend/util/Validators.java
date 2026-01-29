package com.servicepro.backend.util;

import java.util.regex.Pattern;

public class Validators {
    
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private static final Pattern TELEFONE_PATTERN =
        Pattern.compile("^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$");
    
    private static final Pattern CPF_PATTERN =
        Pattern.compile("^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$");
    
    private static final Pattern CNPJ_PATTERN =
        Pattern.compile("^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$");
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidTelefone(String telefone) {
        return telefone != null && TELEFONE_PATTERN.matcher(telefone).matches();
    }
    
    public static boolean isValidCPF(String cpf) {
        if (cpf == null || !CPF_PATTERN.matcher(cpf).matches()) {
            return false;
        }
        
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("\\D", "");
        
        // Verifica se tem 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }
        
        // Verifica dígitos repetidos
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        // Validação dos dígitos verificadores
        try {
            int soma = 0;
            int peso = 10;
            
            for (int i = 0; i < 9; i++) {
                soma += Integer.parseInt(cpf.substring(i, i + 1)) * peso--;
            }
            
            int resto = 11 - (soma % 11);
            if (resto == 10 || resto == 11) {
                resto = 0;
            }
            
            if (resto != Integer.parseInt(cpf.substring(9, 10))) {
                return false;
            }
            
            soma = 0;
            peso = 11;
            
            for (int i = 0; i < 10; i++) {
                soma += Integer.parseInt(cpf.substring(i, i + 1)) * peso--;
            }
            
            resto = 11 - (soma % 11);
            if (resto == 10 || resto == 11) {
                resto = 0;
            }
            
            return resto == Integer.parseInt(cpf.substring(10, 11));
            
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidCNPJ(String cnpj) {
        if (cnpj == null || !CNPJ_PATTERN.matcher(cnpj).matches()) {
            return false;
        }
        
        // Remove caracteres não numéricos
        cnpj = cnpj.replaceAll("\\D", "");
        
        // Verifica se tem 14 dígitos
        if (cnpj.length() != 14) {
            return false;
        }
        
        // Validação dos dígitos verificadores
        try {
            int soma = 0;
            int peso = 2;
            
            for (int i = 11; i >= 0; i--) {
                soma += Integer.parseInt(cnpj.substring(i, i + 1)) * peso;
                peso = (peso == 9) ? 2 : peso + 1;
            }
            
            int resto = soma % 11;
            int digito1 = (resto < 2) ? 0 : 11 - resto;
            
            if (digito1 != Integer.parseInt(cnpj.substring(12, 13))) {
                return false;
            }
            
            soma = 0;
            peso = 2;
            
            for (int i = 12; i >= 0; i--) {
                soma += Integer.parseInt(cnpj.substring(i, i + 1)) * peso;
                peso = (peso == 9) ? 2 : peso + 1;
            }
            
            resto = soma % 11;
            int digito2 = (resto < 2) ? 0 : 11 - resto;
            
            return digito2 == Integer.parseInt(cnpj.substring(13, 14));
            
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidCPFouCNPJ(String documento) {
        if (documento == null) return false;
        
        documento = documento.replaceAll("\\D", "");
        
        if (documento.length() == 11) {
            return isValidCPF(documento);
        } else if (documento.length() == 14) {
            return isValidCNPJ(documento);
        }
        
        return false;
    }
    
    public static String formatarTelefone(String telefone) {
        if (telefone == null) return null;
        
        telefone = telefone.replaceAll("\\D", "");
        
        if (telefone.length() == 11) {
            return String.format("(%s) %s-%s",
                telefone.substring(0, 2),
                telefone.substring(2, 7),
                telefone.substring(7));
        } else if (telefone.length() == 10) {
            return String.format("(%s) %s-%s",
                telefone.substring(0, 2),
                telefone.substring(2, 6),
                telefone.substring(6));
        }
        
        return telefone;
    }
}