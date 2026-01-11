package com.banque.security.strategy.impl;

import com.banque.domain.enums.AuthenticationType;
import com.banque.security.strategy.AuthenticationCredentials;
import com.banque.security.strategy.AuthenticationResult;
import com.banque.security.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * STRATEGY CONCRETE: Authentification par empreinte digitale
 *
 * Implémente la stratégie d'authentification biométrique par empreinte.
 * Les données biométriques sont généralement capturées côté client et
 * envoyées encodées en Base64.
 */
@Component
public class FingerprintAuthenticationStrategy implements AuthenticationStrategy {

    @Override
    public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
        // Validation des données requises
        if (credentials.getUserId() == null || credentials.getUserId().isEmpty()) {
            return AuthenticationResult.failure(
                "Identifiant utilisateur requis",
                AuthenticationType.FINGERPRINT
            );
        }

        if (credentials.getFingerprintData() == null || credentials.getFingerprintData().isEmpty()) {
            return AuthenticationResult.failure(
                "Données d'empreinte digitale requises",
                AuthenticationType.FINGERPRINT
            );
        }

        // Simulation de vérification biométrique
        // En production: comparer avec le template biométrique stocké
        boolean isValid = validateFingerprint(
            credentials.getUserId(),
            credentials.getFingerprintData()
        );

        if (isValid) {
            String token = generateToken(credentials.getUserId());
            return AuthenticationResult.success(
                credentials.getUserId(),
                AuthenticationType.FINGERPRINT,
                token
            );
        } else {
            return AuthenticationResult.failure(
                "Empreinte digitale non reconnue",
                AuthenticationType.FINGERPRINT
            );
        }
    }

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.FINGERPRINT;
    }

    @Override
    public boolean supports(AuthenticationCredentials credentials) {
        return credentials.getAuthenticationType() == AuthenticationType.FINGERPRINT
            && credentials.getFingerprintData() != null;
    }

    /**
     * Simule la validation de l'empreinte digitale.
     * En production: utiliser un SDK biométrique pour comparer les templates.
     *
     * @param userId L'identifiant de l'utilisateur
     * @param fingerprintData Les données biométriques encodées
     * @return true si l'empreinte correspond
     */
    private boolean validateFingerprint(String userId, String fingerprintData) {
        // Simulation: accepte si les données ne sont pas vides et ont une taille minimale
        // TODO: Implémenter une vraie comparaison biométrique
        return fingerprintData.length() >= 10;
    }

    /**
     * Génère un token d'accès.
     */
    private String generateToken(String userId) {
        return "FP-" + UUID.randomUUID().toString();
    }
}
