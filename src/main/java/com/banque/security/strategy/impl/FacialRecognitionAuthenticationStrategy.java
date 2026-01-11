package com.banque.security.strategy.impl;

import com.banque.domain.enums.AuthenticationType;
import com.banque.security.strategy.AuthenticationCredentials;
import com.banque.security.strategy.AuthenticationResult;
import com.banque.security.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * STRATEGY CONCRETE: Authentification par reconnaissance faciale
 *
 * Implémente la stratégie d'authentification biométrique par le visage.
 * L'image faciale est capturée côté client et envoyée encodée en Base64.
 */
@Component
public class FacialRecognitionAuthenticationStrategy implements AuthenticationStrategy {

    // Seuil de confiance pour la reconnaissance (0.0 à 1.0)
    private static final double CONFIDENCE_THRESHOLD = 0.85;

    @Override
    public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
        // Validation des données requises
        if (credentials.getUserId() == null || credentials.getUserId().isEmpty()) {
            return AuthenticationResult.failure(
                "Identifiant utilisateur requis",
                AuthenticationType.FACIAL_RECOGNITION
            );
        }

        if (credentials.getFacialData() == null || credentials.getFacialData().isEmpty()) {
            return AuthenticationResult.failure(
                "Données de reconnaissance faciale requises",
                AuthenticationType.FACIAL_RECOGNITION
            );
        }

        // Simulation de reconnaissance faciale
        // En production: utiliser un service de reconnaissance faciale (AWS Rekognition, etc.)
        double confidence = performFacialRecognition(
            credentials.getUserId(),
            credentials.getFacialData()
        );

        if (confidence >= CONFIDENCE_THRESHOLD) {
            String token = generateToken(credentials.getUserId());
            AuthenticationResult result = AuthenticationResult.success(
                credentials.getUserId(),
                AuthenticationType.FACIAL_RECOGNITION,
                token
            );
            result.setMessage("Reconnaissance faciale réussie (confiance: " +
                String.format("%.1f%%", confidence * 100) + ")");
            return result;
        } else {
            return AuthenticationResult.failure(
                "Visage non reconnu (confiance insuffisante: " +
                    String.format("%.1f%%", confidence * 100) + ")",
                AuthenticationType.FACIAL_RECOGNITION
            );
        }
    }

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.FACIAL_RECOGNITION;
    }

    @Override
    public boolean supports(AuthenticationCredentials credentials) {
        return credentials.getAuthenticationType() == AuthenticationType.FACIAL_RECOGNITION
            && credentials.getFacialData() != null;
    }

    /**
     * Simule la reconnaissance faciale et retourne un score de confiance.
     * En production: appeler un service de reconnaissance faciale.
     *
     * @param userId L'identifiant de l'utilisateur
     * @param facialData Les données d'image faciale encodées
     * @return Score de confiance entre 0.0 et 1.0
     */
    private double performFacialRecognition(String userId, String facialData) {
        // Simulation: retourne un score basé sur la taille des données
        // TODO: Implémenter une vraie reconnaissance faciale via API
        if (facialData.length() >= 100) {
            return 0.92; // Haute confiance
        } else if (facialData.length() >= 50) {
            return 0.87; // Confiance moyenne
        } else {
            return 0.60; // Faible confiance
        }
    }

    /**
     * Génère un token d'accès.
     */
    private String generateToken(String userId) {
        return "FACE-" + UUID.randomUUID().toString();
    }
}
