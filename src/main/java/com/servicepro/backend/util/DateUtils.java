package com.servicepro.backend.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

public class DateUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }
    
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "";
    }
    
    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMATTER) : "";
    }
    
    public static Map<String, LocalDateTime> calcularPeriodo(String periodo) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicio;
        LocalDateTime fim = agora;
        
        switch (periodo.toUpperCase()) {
            case "HOJE":
                inicio = agora.toLocalDate().atStartOfDay();
                break;
                
            case "ONTEM":
                inicio = agora.minusDays(1).toLocalDate().atStartOfDay();
                fim = inicio.plusDays(1).minusSeconds(1);
                break;
                
            case "SEMANA":
                inicio = agora.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
                break;
                
            case "MES":
                inicio = agora.toLocalDate().withDayOfMonth(1).atStartOfDay();
                break;
                
            case "TRIMESTRE":
                int mesAtual = agora.getMonthValue();
                int mesInicioTrimestre = ((mesAtual - 1) / 3) * 3 + 1;
                inicio = LocalDate.of(agora.getYear(), mesInicioTrimestre, 1).atStartOfDay();
                break;
                
            case "SEMESTRE":
                int mesInicioSemestre = agora.getMonthValue() <= 6 ? 1 : 7;
                inicio = LocalDate.of(agora.getYear(), mesInicioSemestre, 1).atStartOfDay();
                break;
                
            case "ANO":
                inicio = LocalDate.of(agora.getYear(), 1, 1).atStartOfDay();
                break;
                
            case "ULTIMOS_7_DIAS":
                inicio = agora.minusDays(7);
                break;
                
            case "ULTIMOS_30_DIAS":
                inicio = agora.minusDays(30);
                break;
                
            case "ULTIMOS_90_DIAS":
                inicio = agora.minusDays(90);
                break;
                
            case "ULTIMOS_365_DIAS":
                inicio = agora.minusDays(365);
                break;
                
            default:
                inicio = agora.toLocalDate().withDayOfMonth(1).atStartOfDay(); // Mês atual
        }
        
        Map<String, LocalDateTime> resultado = new HashMap<>();
        resultado.put("inicio", inicio);
        resultado.put("fim", fim);
        
        return resultado;
    }
    
    public static String getNomePeriodo(String periodo) {
        Map<String, String> periodos = new HashMap<>();
        periodos.put("HOJE", "Hoje");
        periodos.put("ONTEM", "Ontem");
        periodos.put("SEMANA", "Esta Semana");
        periodos.put("MES", "Este Mês");
        periodos.put("TRIMESTRE", "Este Trimestre");
        periodos.put("SEMESTRE", "Este Semestre");
        periodos.put("ANO", "Este Ano");
        periodos.put("ULTIMOS_7_DIAS", "Últimos 7 Dias");
        periodos.put("ULTIMOS_30_DIAS", "Últimos 30 Dias");
        periodos.put("ULTIMOS_90_DIAS", "Últimos 90 Dias");
        periodos.put("ULTIMOS_365_DIAS", "Últimos 365 Dias");
        
        return periodos.getOrDefault(periodo.toUpperCase(), "Período");
    }
    
    public static boolean isDataValida(String data) {
        try {
            LocalDate.parse(data, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean isDataHoraValida(String dataHora) {
        try {
            LocalDateTime.parse(dataHora, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}