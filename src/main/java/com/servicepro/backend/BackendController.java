package com.servicepro.backend;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableAsync
@EnableScheduling
@RestController
public class BackendController {
    
    @GetMapping("/")
    public String home() {
        return "🏠 ServicePro API RODANDO na porta 8082!";
    }
    
    @GetMapping("/test")
    public String test() {
        return "✅ Teste OK!";
    }

    @GetMapping("/status")
    public String status() {
        return "🚀 Status: ONLINE";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}