package com.banque.controller;

import com.banque.controller.dto.AuthenticationRequest;
import com.banque.controller.dto.AuthenticationResponse;
import com.banque.domain.enums.AuthenticationType;
import com.banque.security.strategy.AuthenticationCredentials;
import com.banque.security.strategy.AuthenticationResult;
import com.banque.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller REST pour l'authentification.
 *
 * OBJECTIF 1: Ce controller démontre l'utilisation du pattern Strategy
 * pour l'authentification. Le type d'authentification est sélectionné
 * à l'exécution selon la requête du client.
 *
 * Endpoints:
 * - POST /api/auth/login - Authentification
 * - POST /api/auth/otp/generate - Génération d'OTP
 * - GET /api/auth/methods - Liste des méthodes disponibles
 * - GET /api/auth/preferred/{userId} - Méthode préférée d'un utilisateur
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Pour le développement avec React/Flutter
public class AuthenticationController {

    private final AuthenticationService authService;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint principal d'authentification.
     *
     * La méthode d'authentification est déterminée par le champ authenticationType
     * dans la requête. Le pattern Strategy permet de gérer plusieurs méthodes
     * sans modifier ce controller.
     *
     * @param request Les credentials d'authentification
     * @param httpRequest La requête HTTP (pour IP et User-Agent)
     * @return Résultat de l'authentification
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest) {

        // Convertir le DTO en credentials
        AuthenticationCredentials credentials = AuthenticationCredentials.builder()
            .userId(request.getUserId())
            .authenticationType(request.getAuthenticationType())
            .password(request.getPassword())
            .otpCode(request.getOtpCode())
            .fingerprintData(request.getFingerprintData())
            .facialData(request.getFacialData())
            .arData(request.getArData())
            .build();

        // Récupérer les infos de la requête pour le logging
        String ipAddress = getClientIP(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // Effectuer l'authentification (délégué au service qui utilise Strategy)
        AuthenticationResult result = authService.authenticate(
            credentials,
            ipAddress,
            userAgent
        );

        // Convertir le résultat en réponse DTO
        AuthenticationResponse response = AuthenticationResponse.builder()
            .success(result.isSuccess())
            .message(result.getMessage())
            .userId(result.getUserId())
            .authenticationType(result.getAuthenticationType())
            .accessToken(result.getAccessToken())
            .timestamp(result.getTimestamp())
            .tokenValiditySeconds(result.getTokenValidity())
            .build();

        if (result.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    /**
     * Génère un code OTP pour l'authentification par OTP.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return Le code OTP (en développement seulement)
     */
    @PostMapping("/otp/generate")
    public ResponseEntity<Map<String, String>> generateOTP(@RequestParam String userId) {
        String otp = authService.generateAndSendOTP(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Code OTP généré et envoyé");
        response.put("userId", userId);
        // En développement seulement - ne pas exposer en production !
        response.put("otp_dev_only", otp);

        return ResponseEntity.ok(response);
    }

    /**
     * Retourne la liste des méthodes d'authentification disponibles.
     *
     * Cet endpoint permet au frontend de proposer les options à l'utilisateur.
     *
     * @return Liste des méthodes avec leurs descriptions
     */
    @GetMapping("/methods")
    public ResponseEntity<List<Map<String, String>>> getAvailableMethods() {
        List<AuthenticationType> methods = authService.getAvailableMethods();

        List<Map<String, String>> response = methods.stream()
            .map(type -> {
                Map<String, String> methodInfo = new HashMap<>();
                methodInfo.put("type", type.name());
                methodInfo.put("description", type.getDescription());
                return methodInfo;
            })
            .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Retourne la méthode d'authentification préférée d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @return La méthode préférée
     */
    @GetMapping("/preferred/{userId}")
    public ResponseEntity<Map<String, String>> getPreferredMethod(@PathVariable String userId) {
        AuthenticationType preferred = authService.getPreferredMethod(userId);

        Map<String, String> response = new HashMap<>();
        response.put("userId", userId);
        response.put("preferredMethod", preferred.name());
        response.put("description", preferred.getDescription());

        return ResponseEntity.ok(response);
    }

    /**
     * Met à jour la méthode d'authentification préférée d'un utilisateur.
     *
     * @param userId L'identifiant de l'utilisateur
     * @param method La nouvelle méthode préférée
     * @return Confirmation
     */
    @PutMapping("/preferred/{userId}")
    public ResponseEntity<Map<String, String>> updatePreferredMethod(
            @PathVariable String userId,
            @RequestParam AuthenticationType method) {

        authService.updatePreferredMethod(userId, method);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Méthode préférée mise à jour");
        response.put("userId", userId);
        response.put("newMethod", method.name());

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère l'adresse IP du client (gère les proxies).
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
