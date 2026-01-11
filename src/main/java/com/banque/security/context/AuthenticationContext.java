package com.banque.security.context;

import com.banque.domain.enums.AuthenticationType;
import com.banque.security.factory.AuthenticationFactory;
import com.banque.security.strategy.AuthenticationCredentials;
import com.banque.security.strategy.AuthenticationResult;
import com.banque.security.strategy.AuthenticationStrategy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * PATTERN STRATEGY - Contexte d'authentification
 *
 * OBJECTIF 1: Le contexte utilise la Factory pour obtenir la bonne stratégie
 * à l'exécution. Il ne connaît pas les implémentations concrètes à la compilation.
 *
 * Ce service est le point d'entrée unique pour l'authentification dans l'application.
 * Il délègue le travail à la stratégie appropriée selon le type demandé.
 *
 * AVANTAGES:
 * - Le code client (Controller) n'a besoin que du contexte
 * - La sélection de la méthode se fait à l'exécution
 * - Facile d'ajouter de nouvelles méthodes sans modifier ce code
 */
@Service
public class AuthenticationContext {

    private final AuthenticationFactory authenticationFactory;

    // Stratégie actuellement sélectionnée (peut changer dynamiquement)
    private AuthenticationStrategy currentStrategy;

    public AuthenticationContext(AuthenticationFactory authenticationFactory) {
        this.authenticationFactory = authenticationFactory;
    }

    /**
     * Définit la stratégie d'authentification à utiliser.
     *
     * @param type Le type d'authentification souhaité
     */
    public void setStrategy(AuthenticationType type) {
        this.currentStrategy = authenticationFactory.getStrategy(type);
    }

    /**
     * Authentifie un utilisateur avec les credentials fournis.
     * La méthode d'authentification est déterminée par le type dans les credentials.
     *
     * @param credentials Les informations d'authentification
     * @return Le résultat de l'authentification
     */
    public AuthenticationResult authenticate(AuthenticationCredentials credentials) {
        // Obtenir la stratégie basée sur le type dans les credentials
        AuthenticationStrategy strategy = authenticationFactory.getStrategy(
            credentials.getAuthenticationType()
        );

        // Vérifier que la stratégie supporte ces credentials
        if (!strategy.supports(credentials)) {
            return AuthenticationResult.failure(
                "Les credentials fournis ne sont pas valides pour cette méthode",
                credentials.getAuthenticationType()
            );
        }

        // Déléguer l'authentification à la stratégie
        return strategy.authenticate(credentials);
    }

    /**
     * Authentifie avec la stratégie actuellement définie.
     *
     * @param credentials Les informations d'authentification
     * @return Le résultat de l'authentification
     * @throws IllegalStateException si aucune stratégie n'est définie
     */
    public AuthenticationResult authenticateWithCurrentStrategy(AuthenticationCredentials credentials) {
        if (currentStrategy == null) {
            throw new IllegalStateException(
                "Aucune stratégie d'authentification définie. Appelez setStrategy() d'abord."
            );
        }

        return currentStrategy.authenticate(credentials);
    }

    /**
     * Retourne la liste des méthodes d'authentification disponibles.
     *
     * @return Liste des types supportés
     */
    public List<AuthenticationType> getAvailableMethods() {
        return authenticationFactory.getAvailableTypes();
    }

    /**
     * Vérifie si une méthode d'authentification est disponible.
     *
     * @param type Le type à vérifier
     * @return true si disponible
     */
    public boolean isMethodAvailable(AuthenticationType type) {
        return authenticationFactory.isSupported(type);
    }

    /**
     * Retourne la stratégie actuellement sélectionnée.
     *
     * @return La stratégie courante ou null
     */
    public AuthenticationStrategy getCurrentStrategy() {
        return currentStrategy;
    }
}
