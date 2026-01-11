package com.banque.security.strategy.impl;

import com.banque.domain.enums.AuthenticationType;
import com.banque.security.strategy.AuthenticationCredentials;
import com.banque.security.strategy.AuthenticationResult;
import com.banque.security.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * STRATEGY CONCRETE: Authentification par Réalité Augmentée
 *
 * OBJECTIF 1 - EXTENSION: Cette classe démontre comment ajouter une nouvelle
 * méthode d'authentification SANS MODIFIER le code existant (Factory, Context, etc.)
 *
 * DEMONSTRATION DU PRINCIPE OPEN/CLOSED:
 * - On ajoute simplement une nouvelle classe qui implémente AuthenticationStrategy
 * - Grâce à @Component, Spring la détecte automatiquement
 * - La Factory l'enregistre automatiquement via l'injection de dépendances
 * - Aucune modification nécessaire dans les autres fichiers!
 *
 * CONCEPT:
 * L'authentification AR utilise des marqueurs visuels ou des patterns 3D
 * que l'utilisateur doit scanner avec son appareil. Le pattern peut être
 * personnalisé pour chaque utilisateur (ex: un objet 3D secret).
 */
@Component
public class AugmentedRealityAuthenticationStrategy implements AuthenticationStrategy {

    // Seuil de correspondance pour la validation AR
    private static final double AR_MATCH_THRESHOLD = 0.80;

    @Override
    public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
        // Validation des données requises
        if (credentials.getUserId() == null || credentials.getUserId().isEmpty()) {
            return AuthenticationResult.failure(
                "Identifiant utilisateur requis",
                AuthenticationType.AUGMENTED_REALITY
            );
        }

        if (credentials.getArData() == null || credentials.getArData().isEmpty()) {
            return AuthenticationResult.failure(
                "Données de réalité augmentée requises",
                AuthenticationType.AUGMENTED_REALITY
            );
        }

        // Simulation de validation AR
        // En production: analyser le pattern 3D capturé et le comparer
        ARValidationResult arResult = performARValidation(
            credentials.getUserId(),
            credentials.getArData()
        );

        if (arResult.isValid) {
            String token = generateToken(credentials.getUserId());
            AuthenticationResult result = AuthenticationResult.success(
                credentials.getUserId(),
                AuthenticationType.AUGMENTED_REALITY,
                token
            );
            result.setMessage(String.format(
                "Authentification AR réussie - Pattern '%s' reconnu (confiance: %.1f%%)",
                arResult.patternName,
                arResult.confidence * 100
            ));
            return result;
        } else {
            return AuthenticationResult.failure(
                String.format(
                    "Pattern AR non reconnu (confiance: %.1f%% - minimum requis: %.1f%%)",
                    arResult.confidence * 100,
                    AR_MATCH_THRESHOLD * 100
                ),
                AuthenticationType.AUGMENTED_REALITY
            );
        }
    }

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.AUGMENTED_REALITY;
    }

    @Override
    public boolean supports(AuthenticationCredentials credentials) {
        return credentials.getAuthenticationType() == AuthenticationType.AUGMENTED_REALITY
            && credentials.getArData() != null;
    }

    /**
     * Simule la validation du pattern de réalité augmentée.
     * En production: utiliser un SDK AR (ARCore, ARKit, Vuforia, etc.)
     *
     * @param userId L'identifiant de l'utilisateur
     * @param arData Les données AR capturées (encodées)
     * @return Résultat de la validation AR
     */
    private ARValidationResult performARValidation(String userId, String arData) {
        // Simulation basée sur la longueur des données
        // En réalité: analyser les coordonnées 3D, textures, marqueurs
        double confidence;
        String patternName;

        if (arData.length() >= 200) {
            confidence = 0.95;
            patternName = "Cube Holographique Personnel";
        } else if (arData.length() >= 100) {
            confidence = 0.85;
            patternName = "Marqueur QR 3D";
        } else if (arData.length() >= 50) {
            confidence = 0.75;
            patternName = "Pattern Géométrique";
        } else {
            confidence = 0.50;
            patternName = "Inconnu";
        }

        return new ARValidationResult(
            confidence >= AR_MATCH_THRESHOLD,
            confidence,
            patternName
        );
    }

    /**
     * Génère un token d'accès.
     */
    private String generateToken(String userId) {
        return "AR-" + UUID.randomUUID().toString();
    }

    /**
     * Classe interne pour le résultat de validation AR.
     */
    private static class ARValidationResult {
        final boolean isValid;
        final double confidence;
        final String patternName;

        ARValidationResult(boolean isValid, double confidence, String patternName) {
            this.isValid = isValid;
            this.confidence = confidence;
            this.patternName = patternName;
        }
    }
}
