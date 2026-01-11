package com.banque.security.strategy;

import com.banque.domain.enums.AuthenticationType;
import lombok.Builder;
import lombok.Data;

/**
 * Classe représentant les informations d'authentification fournies par l'utilisateur.
 *
 * Cette classe est flexible et peut contenir différents types de credentials
 * selon la méthode d'authentification choisie.
 */
@Data
@Builder
public class AuthenticationCredentials {

    /**
     * Identifiant de l'utilisateur (email, téléphone, username)
     */
    private String userId;

    /**
     * Type d'authentification demandé
     */
    private AuthenticationType authenticationType;

    /**
     * Mot de passe (pour authentification PASSWORD)
     */
    private String password;

    /**
     * Code OTP (pour authentification OTP)
     */
    private String otpCode;

    /**
     * Données biométriques encodées en Base64 (pour FINGERPRINT)
     */
    private String fingerprintData;

    /**
     * Données de reconnaissance faciale encodées en Base64 (pour FACIAL_RECOGNITION)
     */
    private String facialData;

    /**
     * Données de réalité augmentée (pour AUGMENTED_REALITY)
     */
    private String arData;

    /**
     * Token de session pour les authentifications en plusieurs étapes
     */
    private String sessionToken;
}
