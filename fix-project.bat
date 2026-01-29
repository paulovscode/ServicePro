@echo off
echo Corrigindo estrutura do projeto...
echo.

echo 1. Criando estrutura de pastas...
mkdir src\main\java\com\servicepro\backend 2>nul

echo 2. Verificando BackendApplication.java...
if exist src\main\java\com\servicepro\backend\BackendApplication.java (
    echo ✅ BackendApplication no lugar certo
) else (
    echo ❌ Movendo BackendApplication...
    move BackendApplication.java src\main\java\com\servicepro\backend\ 2>nul
)

echo 3. Criando Controller básico...
echo package com.servicepro.backend;> src\main\java\com\servicepro\backend\SimpleController.java
echo.>> src\main\java\com\servicepro\backend\SimpleController.java
echo import org.springframework.web.bind.annotation.*;>> src\main\java\com\servicepro\backend\SimpleController.java
echo.>> src\main\java\com\servicepro\backend\SimpleController.java
echo @RestController>> src\main\java\com\servicepro\backend\SimpleController.java
echo @RequestMapping("/api")>> src\main\java\com\servicepro\backend\SimpleController.java
echo public class SimpleController {>> src\main\java\com\servicepro\backend\SimpleController.java
echo.>> src\main\java\com\servicepro\backend\SimpleController.java
echo     @GetMapping("/status")>> src\main\java\com\servicepro\backend\SimpleController.java
echo     public String status() {>> src\main\java\com\servicepro\backend\SimpleController.java
echo         return "🚀 API FUNCIONANDO!";>> src\main\java\com\servicepro\backend\SimpleController.java
echo     }>> src\main\java\com\servicepro\backend\SimpleController.java
echo.>> src\main\java\com\servicepro\backend\SimpleController.java
echo     @GetMapping("/ping")>> src\main\java\com\servicepro\backend\SimpleController.java
echo     public String ping() {>> src\main\java\com\servicepro\backend\SimpleController.java
echo         return "pong";>> src\main\java\com\servicepro\backend\SimpleController.java
echo     }>> src\main\java\com\servicepro\backend\SimpleController.java
echo }>> src\main\java\com\servicepro\backend\SimpleController.java

echo 4. Limpando e compilando...
call mvnw.cmd clean compile

echo.
echo ✅ Estrutura corrigida!
echo Teste: http://localhost:8082/api/status
echo.
pause