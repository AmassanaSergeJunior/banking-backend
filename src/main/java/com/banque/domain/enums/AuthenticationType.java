package com.banque.domain.enums;

/**
 * Énumération des différentes méthodes d'authentification supportées.
 *
 * OBJECTIF 1: Le système doit supporter plusieurs méthodes d'authentification
 * sélectionnées à l'exécution selon la configuration de l'opérateur et les
 * préférences du client.
 */
public enum AuthenticationType {

    PASSWORD("Authentification par mot de passe"),
    FINGERPRINT("Authentification par empreinte digitale"),
    FACIAL_RECOGNITION("Authentification par reconnaissance faciale"),
    OTP("Authentification par code OTP"),
    AUGMENTED_REALITY("Authentification par réalité augmentée");

    private final String description;

    AuthenticationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
