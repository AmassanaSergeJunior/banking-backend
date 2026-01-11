package com.banque.security.strategy.impl;

import com.banque.domain.enums.AuthenticationType;
import com.banque.security.strategy.AuthenticationCredentials;
import com.banque.security.strategy.AuthenticationResult;
import com.banque.security.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * STRATEGY CONCRETE: Authentification par mot de passe
 *
 * Implémente la stratégie d'authentification classique par mot de passe.
 * Dans un système réel, le mot de passe serait hashé et comparé avec
 * celui stocké en base de données.
 */
@Component
public class PasswordAuthenticationStrategy implements AuthenticationStrategy {

    @Override
    public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
        // Validation des données requises
        if (credentials.getUserId() == null || credentials.getUserId().isEmpty()) {
            return AuthenticationResult.failure(
                "Identifiant utilisateur requis",
                AuthenticationType.PASSWORD
            );
        }

        if (credentials.getPassword() == null || credentials.getPassword().isEmpty()) {
            return AuthenticationResult.failure(
                "Mot de passe requis",
                AuthenticationType.PASSWORD
            );
        }

        // Simulation de vérification du mot de passe
        // Dans un vrai système: comparer avec le hash en base de données
        boolean isValid = validatePassword(credentials.getUserId(), credentials.getPassword());

        if (isValid) {
            String token = generateToken(credentials.getUserId());
            return AuthenticationResult.success(
                credentials.getUserId(),
                AuthenticationType.PASSWORD,
                token
            );
        } else {
            return AuthenticationResult.failure(
                "Mot de passe incorrect",
                AuthenticationType.PASSWORD
            );
        }
    }

    @Override
    public AuthenticationType getType() {
        return AuthenticationType.PASSWORD;
    }

    @Override
    public boolean supports(AuthenticationCredentials credentials) {
        return credentials.getAuthenticationType() == AuthenticationType.PASSWORD
            && credentials.getPassword() != null;
    }

    /**
     * Simule la validation du mot de passe.
     * Dans un vrai système, on comparerait avec le hash BCrypt en BDD.
     */
    private boolean validatePassword(String userId, String password) {
        // Simulation: accepte si le mot de passe a au moins 6 caractères
        // TODO: Remplacer par une vraie vérification en base de données
        return password.length() >= 6;
    }

    /**
     * Génère un token d'accès simple.
     * Dans un vrai système, utiliser JWT.
     */
    private String generateToken(String userId) {
        return "PWD-" + UUID.randomUUID().toString();
    }
}
